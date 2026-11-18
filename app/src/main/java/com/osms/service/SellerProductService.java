package com.osms.service;

import com.osms.model.ProductForm;
import com.osms.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Negative prices and negative stock are not checked here on purpose: the CHECK constraints
 * CK_Products_Price and CK_Inventory_Quantity in the database reject them.
 */
@Service
public class SellerProductService {

    private static final int MAX_NAME_LENGTH = 150;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");

    private final SellerRepository sellerRepository;

    public SellerProductService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    /** Inserts the product and its inventory row in one transaction. */
    @Transactional
    public void addProduct(int sellerId, ProductForm form) {
        int categoryId = parseCategory(form.getCategoryId());
        String name = form.getProductName().trim();
        if (name.isEmpty()) {
            throw new ProductFormException("Product name is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new ProductFormException("Product name can have at most 150 characters");
        }
        String description = parseDescription(form.getDescription());
        BigDecimal price = parsePrice(form.getPrice());
        int stock = parseStock(form.getStock());

        int productId = sellerRepository.insertProduct(sellerId, categoryId, name, description, price);
        sellerRepository.insertInventory(productId, stock);
    }

    /** Updates the product and its stock in one transaction. */
    @Transactional
    public void updateProduct(int sellerId, int productId, ProductForm form) {
        String description = parseDescription(form.getDescription());
        BigDecimal price = parsePrice(form.getPrice());
        int stock = parseStock(form.getStock());

        int updatedProducts = sellerRepository.updateProduct(sellerId, productId, price, description, form.isActive());
        int updatedStock = sellerRepository.updateStock(sellerId, productId, stock);
        if (updatedProducts == 0 || updatedStock == 0) {
            throw new ProductFormException("This product was not found");
        }
    }

    private int parseCategory(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ProductFormException("Please choose a category");
        }
    }

    private String parseDescription(String value) {
        String description = value.trim();
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ProductFormException("Description can have at most 1000 characters");
        }
        return description.isEmpty() ? null : description;
    }

    private BigDecimal parsePrice(String value) {
        try {
            BigDecimal price = new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
            if (price.abs().compareTo(MAX_PRICE) > 0) {
                throw new ProductFormException("Price is too large");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new ProductFormException("Price must be a number");
        }
    }

    private int parseStock(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ProductFormException("Stock must be a whole number");
        }
    }
}
