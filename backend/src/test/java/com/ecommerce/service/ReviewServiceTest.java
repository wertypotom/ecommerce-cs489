package com.ecommerce.service;

import com.ecommerce.domain.Order;
import com.ecommerce.domain.OrderItem;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.Review;
import com.ecommerce.domain.Role;
import com.ecommerce.domain.User;
import com.ecommerce.dto.ReviewRequest;
import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ForbiddenException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private ReviewService reviewService;

    private User customer;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = user(1L, "reviewer@test.com", Role.CUSTOMER);
        product = product(10L, "Reviewed Product");
        order = order(100L, customer, product);
    }

    @Test
    void createSucceedsWhenUserPurchasedTheProduct() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponse response = reviewService.create(1L, new ReviewRequest(10L, 100L, 4, "Works well"));

        assertEquals(1L, response.getUserId());
        assertEquals("reviewer@test.com", response.getUserEmail());
        assertEquals(10L, response.getProductId());
        assertEquals("Reviewed Product", response.getProductName());
        assertEquals(100L, response.getOrderId());
        assertEquals(4, response.getRating());
        assertEquals("Works well", response.getComment());

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();
        assertSame(customer, saved.getUser());
        assertSame(product, saved.getProduct());
        assertSame(order, saved.getOrder());
        assertEquals(4, saved.getRating());
        assertEquals("Works well", saved.getComment());
    }

    @Test
    void createIsForbiddenWhenTheOrderDoesNotContainTheProduct() {
        Product notPurchased = product(11L, "Never Bought");
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(11L)).thenReturn(Optional.of(notPurchased));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> reviewService.create(1L, new ReviewRequest(11L, 100L, 5, "sneaky")));

        assertTrue(ex.getMessage().contains("purchased"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createIsForbiddenWhenTheOrderBelongsToAnotherUser() {
        User other = user(2L, "other@test.com", Role.CUSTOMER);
        Order otherOrder = order(200L, other, product);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.findById(200L)).thenReturn(Optional.of(otherOrder));

        assertThrows(ForbiddenException.class,
                () -> reviewService.create(1L, new ReviewRequest(10L, 200L, 5, "not mine")));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createIsRejectedWhenTheProductWasAlreadyReviewed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> reviewService.create(1L, new ReviewRequest(10L, 100L, 1, "second")));

        assertTrue(ex.getMessage().contains("already reviewed"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void findByProductIdMapsEntitiesToResponses() {
        Review review = new Review();
        review.setUser(customer);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(5);
        review.setComment("great");
        when(reviewRepository.findByProductId(10L)).thenReturn(List.of(review));

        List<ReviewResponse> result = reviewService.findByProductId(10L);

        assertEquals(1, result.size());
        ReviewResponse mapped = result.get(0);
        assertEquals(1L, mapped.getUserId());
        assertEquals("reviewer@test.com", mapped.getUserEmail());
        assertEquals(10L, mapped.getProductId());
        assertEquals("Reviewed Product", mapped.getProductName());
        assertEquals(100L, mapped.getOrderId());
        assertEquals(5, mapped.getRating());
        assertEquals("great", mapped.getComment());
        verify(reviewRepository).findByProductId(10L);
    }

    private static User user(Long id, String email, Role role) {
        User u = new User(email, "hash", role, null);
        u.setId(id);
        return u;
    }

    private static Product product(Long id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal("10.00"));
        p.setStock(5);
        return p;
    }

    private static Order order(Long id, User owner, Product... products) {
        Order o = new Order();
        o.setId(id);
        o.setUser(owner);
        for (Product p : products) {
            o.getOrderItems().add(new OrderItem(o, p, 1, p.getPrice()));
        }
        return o;
    }
}
