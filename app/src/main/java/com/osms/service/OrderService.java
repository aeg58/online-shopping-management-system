package com.osms.service;

import com.osms.model.CartItemView;
import com.osms.model.CheckoutSummary;
import com.osms.model.Coupon;
import com.osms.model.ShippingCompany;
import com.osms.repository.AddressRepository;
import com.osms.repository.CartRepository;
import com.osms.repository.CouponRepository;
import com.osms.repository.InventoryRepository;
import com.osms.repository.OrderRepository;
import com.osms.repository.PaymentRepository;
import com.osms.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final String CASH_ON_DELIVERY = "CashOnDelivery";
    private static final List<String> PAYMENT_METHODS =
            List.of("CreditCard", "DebitCard", "BankTransfer", CASH_ON_DELIVERY);

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final InventoryRepository inventoryRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final AdminService adminService;

    public OrderService(CartRepository cartRepository, AddressRepository addressRepository,
                        InventoryRepository inventoryRepository, CouponRepository couponRepository,
                        OrderRepository orderRepository, PaymentRepository paymentRepository,
                        ShipmentRepository shipmentRepository, AdminService adminService) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.inventoryRepository = inventoryRepository;
        this.couponRepository = couponRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
        this.adminService = adminService;
    }

    /**
     * The customer cancels an order that is not shipped yet (Pending or Paid).
     * It runs the same cancellation as the admin: stock, coupon use and payment are given back in one transaction.
     */
    @Transactional
    public void cancelByCustomer(int customerId, int orderId) {
        orderRepository.findForCustomer(orderId, customerId)
                .orElseThrow(() -> new CustomerRequestException("This order was not found"));
        String status = adminService.lockOrderStatus(orderId);
        if (!"Pending".equals(status) && !"Paid".equals(status)) {
            throw new CustomerRequestException("Only orders that are not shipped yet can be cancelled");
        }
        adminService.changeOrderStatus(orderId, "Cancelled");
    }

    /**
     * Places an order as ONE database transaction. If any step fails, every step is rolled back.
     *
     * 1. Lock and re-check the stock of every cart item.
     * 2. Validate the coupon (optional) and calculate the discount, then add the shipping fee.
     * 3. Insert the order with status Pending and the chosen shipping company.
     * 4. Insert the order items with the current product price (business rule BR7).
     * 5. Decrease the stock.
     * 6. Increase the used count of the coupon.
     * 7. Insert the payment. Paid methods also change the order status to Paid.
     * 8. Mark the cart as CheckedOut.
     */
    @Transactional
    public int placeOrder(int customerId, int addressId, int shippingCompanyId, String couponCode,
                          String paymentMethod) {
        if (!PAYMENT_METHODS.contains(paymentMethod)) {
            throw new CheckoutException("Please choose a payment method");
        }
        ShippingCompany company = findCompany(shippingCompanyId);
        if (!addressRepository.belongsToCustomer(addressId, customerId)) {
            throw new CheckoutException("Please choose one of your addresses");
        }

        int cartId = cartRepository.lockActiveCartId(customerId)
                .orElseThrow(() -> new CheckoutException("Your cart is empty"));
        // Rows are always locked in the same order, so two orders cannot block each other forever
        List<CartItemView> items = cartRepository.findItems(cartId).stream()
                .sorted(Comparator.comparingInt(CartItemView::productId))
                .toList();
        if (items.isEmpty()) {
            throw new CheckoutException("Your cart is empty");
        }

        // Step 1
        for (CartItemView item : items) {
            int stock = inventoryRepository.lockStock(item.productId());
            if (!item.productActive()) {
                throw new CheckoutException(item.productName() + " is no longer available");
            }
            if (item.quantity() > stock) {
                throw new CheckoutException("Not enough stock for " + item.productName());
            }
        }

        // Step 2
        BigDecimal subtotal = items.stream().map(CartItemView::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Coupon coupon = findValidCoupon(couponRepository.lockByCode(couponCode.trim()), couponCode, subtotal);
        BigDecimal discount = coupon == null ? BigDecimal.ZERO : calculateDiscount(coupon, subtotal);
        BigDecimal total = subtotal.subtract(discount).add(company.baseFee());
        if (total.signum() <= 0) {
            throw new CheckoutException("The order total must be greater than 0 after the discount");
        }

        // Step 3
        Integer couponId = coupon == null ? null : coupon.couponId();
        int orderId = orderRepository.insertOrder(customerId, addressId, couponId, company.shippingCompanyId(),
                discount, total);

        for (CartItemView item : items) {
            // Step 4
            orderRepository.insertItem(orderId, item.productId(), item.quantity(), item.price());
            // Step 5
            inventoryRepository.decreaseStock(item.productId(), item.quantity());
        }

        // Step 6
        if (coupon != null) {
            couponRepository.increaseUsedCount(coupon.couponId());
        }

        // Step 7
        if (CASH_ON_DELIVERY.equals(paymentMethod)) {
            paymentRepository.insertPending(orderId, paymentMethod, total);
        } else {
            paymentRepository.insertCompleted(orderId, paymentMethod, total);
            orderRepository.markPaid(orderId);
        }

        // Step 8
        cartRepository.markCheckedOut(cartId);
        return orderId;
    }

    /**
     * Calculates the order summary for the checkout page while the customer types a coupon or picks a company.
     * Nothing is saved; placeOrder checks everything again inside its transaction.
     */
    public CheckoutSummary previewOrder(List<CartItemView> items, int shippingCompanyId, String couponCode) {
        BigDecimal subtotal = items.stream().map(CartItemView::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fee = shipmentRepository.findCompany(shippingCompanyId)
                .map(ShippingCompany::baseFee)
                .orElse(BigDecimal.ZERO);
        BigDecimal discount = BigDecimal.ZERO;
        String couponError = null;
        try {
            Coupon coupon = findValidCoupon(couponRepository.findByCode(couponCode.trim()), couponCode, subtotal);
            if (coupon != null) {
                discount = calculateDiscount(coupon, subtotal);
            }
        } catch (CheckoutException e) {
            couponError = e.getMessage();
        }
        return new CheckoutSummary(subtotal, discount, fee, subtotal.subtract(discount).add(fee), couponError);
    }

    private ShippingCompany findCompany(int shippingCompanyId) {
        return shipmentRepository.findCompany(shippingCompanyId)
                .orElseThrow(() -> new CheckoutException("Please choose a shipping company"));
    }

    private Coupon findValidCoupon(Optional<Coupon> found, String couponCode, BigDecimal subtotal) {
        String code = couponCode.trim();
        if (code.isEmpty()) {
            return null;
        }
        Coupon coupon = found.orElseThrow(() -> new CheckoutException("Coupon " + code + " does not exist"));
        if (!coupon.active()) {
            throw new CheckoutException("Coupon " + coupon.code() + " is not active");
        }
        if (!coupon.inDateRange()) {
            throw new CheckoutException("Coupon " + coupon.code() + " is not valid today");
        }
        if (coupon.usedCount() >= coupon.usageLimit()) {
            throw new CheckoutException("Coupon " + coupon.code() + " has reached its usage limit");
        }
        if (subtotal.compareTo(coupon.minOrderAmount()) < 0) {
            throw new CheckoutException("Coupon " + coupon.code() + " needs a minimum order of "
                    + coupon.minOrderAmount() + " ₺");
        }
        return coupon;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        BigDecimal discount = "Percent".equals(coupon.discountType())
                ? subtotal.multiply(coupon.discountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.discountValue();
        return discount.min(subtotal);
    }
}
