package com.ecommerce.service;

import com.ecommerce.domain.Cart;
import com.ecommerce.domain.CartItem;
import com.ecommerce.domain.Order;
import com.ecommerce.domain.OrderItem;
import com.ecommerce.domain.OrderStatus;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.User;
import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.OrderItemDto;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ForbiddenException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id, Long requestingUserId, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        checkAccess(order, requestingUserId, isAdmin);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUserId(Long userId, Long requestingUserId, boolean isAdmin) {
        if (!isAdmin && !userId.equals(requestingUserId)) {
            throw new ForbiddenException("You can only view your own orders");
        }
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            Product product = productRepository.findById(ci.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", ci.getProduct().getId()));

            int updated = productRepository.decrementStock(product.getId(), ci.getQuantity());
            if (updated == 0) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName()
                                + ". Available: " + product.getStock()
                                + ", Requested: " + ci.getQuantity());
            }

            OrderItem orderItem = new OrderItem(order, product, ci.getQuantity(), ci.getPriceSnapshot());
            order.getOrderItems().add(orderItem);

            total = total.add(ci.getPriceSnapshot().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteByCartId(cart.getId());

        return toResponse(savedOrder);
    }

    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateStatusTransition(order.getStatus(), status);
        order.setStatus(status);

        if (status == OrderStatus.CANCELLED) {
            restock(order);
        }

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public void cancel(Long id, Long requestingUserId, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        checkAccess(order, requestingUserId, isAdmin);

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ForbiddenException("Only PENDING or CONFIRMED orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        restock(order);
        orderRepository.save(order);
    }

    private void restock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    private void checkAccess(Order order, Long requestingUserId, boolean isAdmin) {
        if (!isAdmin && !order.getUser().getId().equals(requestingUserId)) {
            throw new ForbiddenException("You do not have access to this order");
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!valid) {
            throw new BadRequestException(
                    "Invalid status transition from " + current + " to " + next);
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemDto> items = order.getOrderItems().stream()
                .map(this::toItemDto)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getShippingAddress(),
                order.getTotal(),
                order.getUser().getId(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemDto toItemDto(OrderItem item) {
        BigDecimal subtotal = item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceSnapshot(),
                subtotal
        );
    }
}
