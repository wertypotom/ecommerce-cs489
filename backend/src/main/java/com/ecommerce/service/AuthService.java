package com.ecommerce.service;

import com.ecommerce.domain.Cart;
import com.ecommerce.domain.Role;
import com.ecommerce.domain.User;
import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ForbiddenException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, CartRepository cartRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(new CustomUserDetails(user));
        return new AuthResponse(token, toUserResponse(user));
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use: " + request.getEmail());
        }

        Role role = parseRole(request.getRole());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User saved = userRepository.save(user);

        Cart cart = new Cart(saved);
        cartRepository.save(cart);

        String token = jwtService.generateToken(new CustomUserDetails(saved));
        return new AuthResponse(token, toUserResponse(saved));
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            throw new BadRequestException("Role is required");
        }

        Role parsed;
        try {
            parsed = Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role: expected one of SELLER, CUSTOMER");
        }

        if (parsed == Role.ADMIN) {
            throw new ForbiddenException("ADMIN accounts cannot be created through registration");
        }

        return parsed;
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
