package com.ecommerce.service;

import com.ecommerce.domain.Cart;
import com.ecommerce.domain.CartItem;
import com.ecommerce.domain.Product;
import com.ecommerce.domain.User;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository,
                       EntityManager entityManager) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public CartResponse findById(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", id));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse findByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
        return toResponse(cart);
    }

    public CartResponse createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Optional<Cart> existing = cartRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Cart cart = new Cart(user);
        Cart saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    public CartItemResponse addItem(Long cartId, CartItemRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cartId, request.getProductId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setPriceSnapshot(product.getPrice());
        } else {
            cartItem = new CartItem(cart, product, request.getQuantity(), product.getPrice());
        }

        CartItem saved = cartItemRepository.save(cartItem);
        return toItemResponse(saved);
    }

    public CartItemResponse updateItem(Long cartId, Long itemId, CartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new ResourceNotFoundException("CartItem not found in this cart");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        cartItem.setQuantity(request.getQuantity());
        cartItem.setPriceSnapshot(product.getPrice());

        CartItem saved = cartItemRepository.save(cartItem);
        return toItemResponse(saved);
    }

    public void removeItem(Long cartId, Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new ResourceNotFoundException("CartItem not found in this cart");
        }

        cartItemRepository.delete(cartItem);
    }

    public void deleteCart(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", id));
        
        // Delete cart items first using native query to avoid Hibernate cascade issues
        entityManager.createNativeQuery("DELETE FROM cart_items WHERE cart_id = :cartId")
                .setParameter("cartId", id)
                .executeUpdate();
        
        // Flush and clear to ensure Hibernate session is clean
        entityManager.flush();
        entityManager.clear();
        
        // Reload cart (now with no items) and delete
        cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", id));
        
        cartRepository.delete(cart);
    }

    public Cart getCartEntityByUserId(Long userId) {
        return cartRepository.findByUserId(userId).orElse(null);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemDto> items = cart.getCartItems().stream()
                .map(this::toItemDto)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                items,
                total
        );
    }

    private CartItemDto toItemDto(CartItem item) {
        BigDecimal subtotal = item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceSnapshot(),
                subtotal
        );
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotal = item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceSnapshot(),
                subtotal
        );
    }
}
