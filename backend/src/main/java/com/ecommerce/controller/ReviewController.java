package com.ecommerce.controller;

import com.ecommerce.dto.ReviewRequest;
import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(userDetails.getUserId(), request));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.findByProductId(productId));
    }
}
