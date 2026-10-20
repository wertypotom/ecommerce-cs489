package com.ecommerce.service;

import com.ecommerce.domain.Role;
import com.ecommerce.domain.User;
import com.ecommerce.dto.UserRequest;
import com.ecommerce.dto.UserResponse;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        user.setRole(Role.valueOf(request.getRole()));

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
