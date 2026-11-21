package com.osms.service;

import com.osms.model.OrderView;
import com.osms.repository.OrderRepository;
import com.osms.repository.ReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnService {

    private static final int MAX_REASON_LENGTH = 255;

    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;

    public ReturnService(OrderRepository orderRepository, ReturnRepository returnRepository) {
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
    }

    @Transactional
    public void requestReturn(int customerId, int orderId, int productId, int quantity, String reason) {
        OrderView order = orderRepository.findForCustomer(orderId, customerId)
                .orElseThrow(() -> new CustomerRequestException("This order was not found"));
        if (!"Delivered".equals(order.status())) {
            throw new CustomerRequestException("Only delivered orders can be returned");
        }

        String text = reason.trim();
        if (text.isEmpty()) {
            throw new CustomerRequestException("Please write the reason for the return");
        }
        if (text.length() > MAX_REASON_LENGTH) {
            throw new CustomerRequestException("The reason can have at most 255 characters");
        }
        if (quantity < 1) {
            throw new CustomerRequestException("Quantity must be at least 1");
        }

        int orderedQuantity = returnRepository.lockOrderedQuantity(orderId, productId)
                .orElseThrow(() -> new CustomerRequestException("This product is not in the order"));
        if (returnRepository.hasRejectedReturn(orderId, productId)) {
            throw new CustomerRequestException("Your return request for this product was rejected, so you cannot ask again");
        }
        int remainingQuantity = orderedQuantity - returnRepository.sumActiveReturns(orderId, productId);
        if (quantity > remainingQuantity) {
            throw new CustomerRequestException("You can return at most " + remainingQuantity + " of this product");
        }

        returnRepository.insert(orderId, productId, quantity, text);
    }
}
