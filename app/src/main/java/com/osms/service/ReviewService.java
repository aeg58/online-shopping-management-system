package com.osms.service;

import com.osms.repository.ReviewRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private static final int MAX_COMMENT_LENGTH = 1000;

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public boolean canReview(int customerId, int productId) {
        return reviewRepository.hasPurchased(customerId, productId)
                && !reviewRepository.hasReviewed(customerId, productId);
    }

    /** The rating range and the one review per customer rule are checked by the database (BR10). */
    public void addReview(int customerId, int productId, int rating, String comment) {
        if (!reviewRepository.hasPurchased(customerId, productId)) {
            throw new CustomerRequestException("You can only review products you have ordered");
        }
        String text = comment.trim();
        if (text.length() > MAX_COMMENT_LENGTH) {
            throw new CustomerRequestException("Your comment can have at most 1000 characters");
        }

        try {
            reviewRepository.insert(productId, customerId, rating, text.isEmpty() ? null : text);
        } catch (DataIntegrityViolationException e) {
            throw new CustomerRequestException(friendlyMessage(e));
        }
    }

    private String friendlyMessage(DataIntegrityViolationException exception) {
        String constraint = DatabaseErrors.constraintName(exception);
        return switch (constraint) {
            case "UQ_Reviews_Product_Customer" -> "You have already reviewed this product (" + constraint + ")";
            case "CK_Reviews_Rating" -> "Rating must be between 1 and 5 (" + constraint + ")";
            default -> DatabaseErrors.message(exception);
        };
    }
}
