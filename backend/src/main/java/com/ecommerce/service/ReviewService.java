package com.ecommerce.service;

import com.ecommerce.domain.Order;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.Review;
import com.ecommerce.domain.User;
import com.ecommerce.dto.ReviewRequest;
import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ForbiddenException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findByProductId(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ReviewResponse create(Long userId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        boolean purchased = order.getUser().getId().equals(userId)
                && order.getOrderItems().stream()
                        .anyMatch(oi -> oi.getProduct().getId().equals(request.getProductId()));

        if (!purchased) {
            throw new ForbiddenException("You can only review products you have purchased");
        }

        if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new ConflictException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getEmail(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getOrder() != null ? review.getOrder().getId() : null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
