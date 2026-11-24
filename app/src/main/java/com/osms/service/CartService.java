package com.osms.service;

import com.osms.model.CartItemView;
import com.osms.model.ProductView;
import com.osms.repository.CartRepository;
import com.osms.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public List<CartItemView> getItems(int customerId) {
        return cartRepository.findActiveCartId(customerId)
                .map(cartRepository::findItems)
                .orElse(List.of());
    }

    public BigDecimal getTotal(List<CartItemView> items) {
        return items.stream().map(CartItemView::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public String addItem(int customerId, int productId, int quantity) {
        checkQuantity(quantity);
        ProductView product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new CartException("This product is no longer available"));
        if (product.quantityInStock() == 0) {
            throw new CartException(product.productName() + " is out of stock");
        }

        int cartId = cartRepository.lockActiveCartId(customerId)
                .orElseGet(() -> cartRepository.createCart(customerId));
        int quantityInCart = cartRepository.findQuantity(cartId, productId).orElse(0);

        if (quantity > product.quantityInStock() - quantityInCart) {
            throw new CartException("Not enough stock for " + product.productName() + ". In stock: "
                    + product.quantityInStock() + ", already in your cart: " + quantityInCart);
        }

        if (quantityInCart == 0) {
            cartRepository.insertItem(cartId, productId, quantity);
        } else {
            cartRepository.updateProductQuantity(cartId, productId, quantityInCart + quantity);
        }
        return product.productName();
    }

    @Transactional
    public void updateQuantity(int customerId, int cartItemId, int quantity) {
        checkQuantity(quantity);
        int cartId = lockExistingCart(customerId);
        CartItemView item = cartRepository.findItem(cartId, cartItemId)
                .orElseThrow(() -> new CartException("This item is not in your cart"));

        if (quantity > item.quantityInStock()) {
            throw new CartException("Not enough stock for " + item.productName() + ". In stock: "
                    + item.quantityInStock());
        }
        cartRepository.updateItemQuantity(cartId, cartItemId, quantity);
    }

    @Transactional
    public void removeItem(int customerId, int cartItemId) {
        int cartId = lockExistingCart(customerId);
        if (cartRepository.deleteItem(cartId, cartItemId) == 0) {
            throw new CartException("This item is not in your cart");
        }
    }

    private int lockExistingCart(int customerId) {
        return cartRepository.lockActiveCartId(customerId)
                .orElseThrow(() -> new CartException("Your cart is empty"));
    }

    private void checkQuantity(int quantity) {
        if (quantity < 1) {
            throw new CartException("Quantity must be at least 1");
        }
    }
}
