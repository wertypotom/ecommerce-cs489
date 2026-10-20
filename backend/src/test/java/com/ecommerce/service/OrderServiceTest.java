package com.ecommerce.service;

import com.ecommerce.domain.Cart;
import com.ecommerce.domain.CartItem;
import com.ecommerce.domain.OrderStatus;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.Role;
import com.ecommerce.domain.User;
import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ForbiddenException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private EntityManager entityManager;

    private User customer;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        customer = userRepository.save(new User("customer@test.com", "hash", Role.CUSTOMER, null));

        product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(5);
        product.setUser(customer);
        product = productRepository.save(product);

        cart = cartRepository.save(new Cart(customer));
        cartItemRepository.save(new CartItem(cart, product, 2, new BigDecimal("10.00")));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void checkoutPersistsOrderItemsDecrementsStockAndClearsCart() {
        OrderResponse response = orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St"));

        assertEquals(OrderStatus.PENDING.name(), response.getStatus());
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getTotal()));
        assertEquals(1, response.getItems().size());
        assertEquals(product.getId(), response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getItems().get(0).getSubtotal()));

        entityManager.flush();
        entityManager.clear();

        assertEquals(3, productRepository.findById(product.getId()).orElseThrow().getStock());
        assertEquals(1, orderItemRepository.findByOrderId(response.getId()).size());
        assertTrue(cartItemRepository.findByCartId(cart.getId()).isEmpty());
    }

    @Test
    void checkoutComputesTotalServerSideAcrossMultipleItems() {
        Product second = new Product();
        second.setName("Second Product");
        second.setPrice(new BigDecimal("5.50"));
        second.setStock(10);
        second.setUser(customer);
        second = productRepository.save(second);
        cartItemRepository.save(new CartItem(cart, second, 3, new BigDecimal("5.50")));

        entityManager.flush();
        entityManager.clear();

        OrderResponse response = orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St"));

        assertEquals(0, new BigDecimal("36.50").compareTo(response.getTotal()));
        assertEquals(2, response.getItems().size());

        entityManager.flush();
        entityManager.clear();

        assertEquals(7, productRepository.findById(second.getId()).orElseThrow().getStock());
    }

    @Test
    void checkoutFailsWhenStockInsufficientAndLeavesStockUntouched() {
        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseThrow();
        item.setQuantity(10);
        cartItemRepository.save(item);
        entityManager.flush();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St")));

        assertTrue(ex.getMessage().contains("Insufficient stock"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(5, productRepository.findById(product.getId()).orElseThrow().getStock());
        assertEquals(0, orderRepository.count());
    }

    @Test
    void checkoutFailsWhenCartIsEmpty() {
        cartItemRepository.deleteByCartId(cart.getId());
        entityManager.flush();
        entityManager.clear();

        assertThrows(BadRequestException.class,
                () -> orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St")));
    }

    @Test
    void cancelByOwnerRestocksItems() {
        OrderResponse response = orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St"));
        entityManager.flush();
        entityManager.clear();

        orderService.cancel(response.getId(), customer.getId(), false);

        entityManager.flush();
        entityManager.clear();

        assertEquals(5, productRepository.findById(product.getId()).orElseThrow().getStock());
        assertEquals(OrderStatus.CANCELLED,
                orderRepository.findById(response.getId()).orElseThrow().getStatus());
    }

    @Test
    void cancelByOtherUserIsForbidden() {
        OrderResponse response = orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St"));
        User other = userRepository.save(new User("other@test.com", "hash", Role.CUSTOMER, null));
        entityManager.flush();
        entityManager.clear();

        assertThrows(ForbiddenException.class,
                () -> orderService.cancel(response.getId(), other.getId(), false));
    }

    @Test
    void cancelByAdminIsAllowedAndRestocksItems() {
        OrderResponse response = orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St"));
        User admin = userRepository.save(new User("admin@test.com", "hash", Role.ADMIN, null));
        entityManager.flush();
        entityManager.clear();

        orderService.cancel(response.getId(), admin.getId(), true);

        entityManager.flush();
        entityManager.clear();

        assertEquals(5, productRepository.findById(product.getId()).orElseThrow().getStock());
        assertEquals(OrderStatus.CANCELLED,
                orderRepository.findById(response.getId()).orElseThrow().getStatus());
    }

    @Test
    void updateStatusToCancelledRestocksItems() {
        OrderResponse response = orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St"));
        entityManager.flush();
        entityManager.clear();

        orderService.updateStatus(response.getId(), OrderStatus.CANCELLED);

        entityManager.flush();
        entityManager.clear();

        assertEquals(5, productRepository.findById(product.getId()).orElseThrow().getStock());
    }

    @Test
    void findByIdEnforcesOwnership() {
        OrderResponse response = orderService.checkout(customer.getId(), new CheckoutRequest("123 Main St"));
        User other = userRepository.save(new User("other2@test.com", "hash", Role.CUSTOMER, null));
        entityManager.flush();
        entityManager.clear();

        assertThrows(ForbiddenException.class,
                () -> orderService.findById(response.getId(), other.getId(), false));
        assertDoesNotThrow(() -> orderService.findById(response.getId(), other.getId(), true));
        assertDoesNotThrow(() -> orderService.findById(response.getId(), customer.getId(), false));
    }

    @Test
    void findByUserIdEnforcesOwnership() {
        User other = userRepository.save(new User("other3@test.com", "hash", Role.CUSTOMER, null));

        assertThrows(ForbiddenException.class,
                () -> orderService.findByUserId(customer.getId(), other.getId(), false));
        assertDoesNotThrow(
                () -> orderService.findByUserId(customer.getId(), customer.getId(), false));
    }
}
