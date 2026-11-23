package com.osms.service;

import com.osms.model.CouponForm;
import com.osms.model.ReturnRecord;
import com.osms.repository.AdminRepository;
import com.osms.repository.CategoryRepository;
import com.osms.repository.CouponRepository;
import com.osms.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Service
public class AdminService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");
    private static final String CANCELLED = "Cancelled";
    private static final String DELIVERED = "Delivered";

    private final AdminRepository adminRepository;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;
    private final ShipmentRepository shipmentRepository;

    public AdminService(AdminRepository adminRepository, CategoryRepository categoryRepository,
                        CouponRepository couponRepository, ShipmentRepository shipmentRepository) {
        this.adminRepository = adminRepository;
        this.categoryRepository = categoryRepository;
        this.couponRepository = couponRepository;
        this.shipmentRepository = shipmentRepository;
    }

    public void setUserActive(int adminUserId, int userId, boolean active) {
        if (userId == adminUserId && !active) {
            throw new AdminRequestException("You cannot deactivate your own account");
        }
        if (adminRepository.setUserActive(userId, active) == 0) {
            throw new AdminRequestException("This user was not found");
        }
    }

    public void approveSeller(int sellerId) {
        if (adminRepository.approveSeller(sellerId) == 0) {
            throw new AdminRequestException("This seller was not found");
        }
    }

    public void addCategory(String categoryName, String description) {
        categoryRepository.insert(cleanCategoryName(categoryName), cleanCategoryDescription(description));
    }

    public void updateCategory(int categoryId, String categoryName, String description) {
        int updated = categoryRepository.update(categoryId, cleanCategoryName(categoryName),
                cleanCategoryDescription(description));
        if (updated == 0) {
            throw new AdminRequestException("This category was not found");
        }
    }

    /** The discount rules are checked by the CHECK constraints of the Coupons table. */
    public void addCoupon(CouponForm form) {
        String code = form.getCode().trim().toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            throw new AdminRequestException("Coupon code is required");
        }
        if (code.length() > 30) {
            throw new AdminRequestException("Coupon code can have at most 30 characters");
        }
        couponRepository.insert(code, form.getDiscountType(),
                parseAmount(form.getDiscountValue(), "Discount value"),
                parseAmount(form.getMinOrderAmount(), "Minimum order amount"),
                parseDate(form.getStartDate(), "Start date"),
                parseDate(form.getEndDate(), "End date"),
                parseUsageLimit(form.getUsageLimit()));
    }

    /**
     * Changes the order status in one transaction. Cancelling an order puts its items back into stock,
     * gives the coupon use back, refunds the payment and marks a shipment on the way as Returned.
     * A delivered order is returned with a return request instead, and a cancelled order stays cancelled,
     * so stock can never be added back twice.
     */
    /** Locks the order row until the transaction ends and returns its status. */
    public String lockOrderStatus(int orderId) {
        return adminRepository.lockOrderStatus(orderId)
                .orElseThrow(() -> new AdminRequestException("This order was not found"));
    }

    /**
     * Cancels an order in one transaction: stock, coupon use and payment are given back.
     * The admin cannot set other statuses by hand: an order becomes Shipped and Delivered only
     * through the shipment, so a paid order can never be moved back to Pending.
     */
    @Transactional
    public void cancelOrder(int orderId) {
        String currentStatus = adminRepository.lockOrderStatus(orderId)
                .orElseThrow(() -> new AdminRequestException("This order was not found"));

        if (CANCELLED.equals(currentStatus)) {
            throw new AdminRequestException("This order is already cancelled");
        }
        if (DELIVERED.equals(currentStatus)) {
            throw new AdminRequestException("A delivered order cannot be cancelled. Use a return request instead");
        }
        adminRepository.restoreStock(orderId);
        adminRepository.releaseCoupon(orderId);
        adminRepository.cancelPayments(orderId);
        shipmentRepository.markReturned(orderId);
        adminRepository.updateOrderStatus(orderId, CANCELLED);
    }

    /** Creates the shipment and marks the order as shipped in one transaction. */
    @Transactional
    public void createShipment(int orderId, int shippingCompanyId, String trackingNumber) {
        String tracking = trackingNumber.trim();
        if (tracking.isEmpty()) {
            throw new AdminRequestException("Tracking number is required");
        }
        if (!tracking.matches("\\d{12}")) {
            throw new AdminRequestException("Tracking number must be exactly 12 digits");
        }
        String status = adminRepository.lockOrderStatus(orderId)
                .orElseThrow(() -> new AdminRequestException("This order was not found"));
        if ("Cancelled".equals(status) || "Delivered".equals(status)) {
            throw new AdminRequestException("A " + status.toLowerCase(Locale.ROOT) + " order cannot be shipped");
        }
        // The customer paid the fee of the company chosen at checkout, so the order is shipped with that company
        int chosenCompany = adminRepository.findChosenShippingCompany(orderId).orElse(shippingCompanyId);
        if (chosenCompany != shippingCompanyId) {
            throw new AdminRequestException("This order must be shipped with the company the customer chose");
        }

        shipmentRepository.insertInTransit(orderId, shippingCompanyId, tracking);
        adminRepository.updateOrderStatus(orderId, "Shipped");
    }

    /** Marks the shipment and the order as delivered in one transaction. Cash is collected on delivery. */
    @Transactional
    public void markDelivered(int orderId) {
        String currentStatus = adminRepository.lockOrderStatus(orderId)
                .orElseThrow(() -> new AdminRequestException("This order was not found"));
        if (CANCELLED.equals(currentStatus)) {
            throw new AdminRequestException("A cancelled order cannot be delivered");
        }
        if (shipmentRepository.markDelivered(orderId) == 0) {
            throw new AdminRequestException("This order has no shipment on the way");
        }
        adminRepository.updateOrderStatus(orderId, "Delivered");
        adminRepository.completeCashPayment(orderId);
    }

    @Transactional
    public void approveReturn(int returnId) {
        decideReturn(returnId, "Approved");
    }

    /** The admin must say why; the customer sees the reason on the order page. */
    @Transactional
    public void rejectReturn(int returnId, String rejectReason) {
        String reason = rejectReason.trim();
        if (reason.isEmpty()) {
            throw new AdminRequestException("Please write why the return is rejected");
        }
        if (reason.length() > 255) {
            throw new AdminRequestException("The reason can have at most 255 characters");
        }
        ReturnRecord record = lockReturn(returnId);
        if (!"Requested".equals(record.status())) {
            throw new AdminRequestException("This return was already processed");
        }
        adminRepository.rejectReturn(returnId, reason);
    }

    /** Refunds the return and puts the returned quantity back into stock in one transaction. */
    @Transactional
    public void refundReturn(int returnId) {
        ReturnRecord record = lockReturn(returnId);
        if (!"Approved".equals(record.status())) {
            throw new AdminRequestException("Only approved returns can be refunded");
        }
        adminRepository.markReturnRefunded(returnId, record.refundAmount());
        adminRepository.increaseStock(record.productId(), record.quantity());
    }

    private void decideReturn(int returnId, String newStatus) {
        ReturnRecord record = lockReturn(returnId);
        if (!"Requested".equals(record.status())) {
            throw new AdminRequestException("This return was already processed");
        }
        adminRepository.updateReturnStatus(returnId, newStatus);
    }

    private ReturnRecord lockReturn(int returnId) {
        return adminRepository.lockReturn(returnId)
                .orElseThrow(() -> new AdminRequestException("This return was not found"));
    }

    private String cleanCategoryName(String value) {
        String name = value.trim();
        if (name.isEmpty()) {
            throw new AdminRequestException("Category name is required");
        }
        if (name.length() > 100) {
            throw new AdminRequestException("Category name can have at most 100 characters");
        }
        return name;
    }

    private String cleanCategoryDescription(String value) {
        String description = value.trim();
        if (description.length() > 255) {
            throw new AdminRequestException("Description can have at most 255 characters");
        }
        return description.isEmpty() ? null : description;
    }

    private BigDecimal parseAmount(String value, String label) {
        try {
            BigDecimal amount = new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
            if (amount.abs().compareTo(MAX_AMOUNT) > 0) {
                throw new AdminRequestException(label + " is too large");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new AdminRequestException(label + " must be a number");
        }
    }

    private LocalDate parseDate(String value, String label) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new AdminRequestException(label + " is not a valid date");
        }
    }

    private int parseUsageLimit(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new AdminRequestException("Usage limit must be a whole number");
        }
    }
}
