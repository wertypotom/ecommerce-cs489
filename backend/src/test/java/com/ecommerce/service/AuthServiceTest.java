package com.ecommerce.service;

import com.ecommerce.domain.Cart;
import com.ecommerce.domain.User;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ConflictException;
import com.ecommerce.exception.ForbiddenException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "admin", " Admin ", "AdMiN"})
    void registerNeverCreatesAnAdministrator(String requestedRole) {
        when(userRepository.existsByEmail("attacker@test.com")).thenReturn(false);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> authService.register(new RegisterRequest("attacker@test.com", "pw", requestedRole)));

        assertTrue(ex.getMessage().contains("cannot be created through registration"));
        verify(userRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void registerAllowsSellers() {
        stubSuccessfulSave();

        AuthResponse response = authService.register(
                new RegisterRequest("seller@store.com", "pw", "SELLER"));

        assertEquals("SELLER", response.getUser().getRole());
        assertEquals("token", response.getToken());
        verify(userRepository).save(any(User.class));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void registerAllowsCustomers() {
        stubSuccessfulSave();

        AuthResponse response = authService.register(
                new RegisterRequest("customer@store.com", "pw", "CUSTOMER"));

        assertEquals("CUSTOMER", response.getUser().getRole());
    }

    @Test
    void registerNormalizesRoleCaseAndSurroundingWhitespace() {
        stubSuccessfulSave();

        AuthResponse response = authService.register(
                new RegisterRequest("customer@store.com", "pw", "  customer  "));

        assertEquals("CUSTOMER", response.getUser().getRole());
    }

    @Test
    void registerRejectsAnUnknownRoleWithABadRequest() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> authService.register(new RegisterRequest("user@test.com", "pw", "SUPERUSER")));

        assertTrue(ex.getMessage().contains("Invalid role"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsABlankRoleWithABadRequest() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> authService.register(new RegisterRequest("user@test.com", "pw", "   ")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsADuplicateEmail() {
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> authService.register(new RegisterRequest("taken@test.com", "pw", "CUSTOMER")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerHashesThePasswordBeforePersisting() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token");

        authService.register(new RegisterRequest("user@test.com", "plaintext", "CUSTOMER"));

        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(argThat((User u) -> "bcrypt-hash".equals(u.getPasswordHash())));
    }

    private void stubSuccessfulSave() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token");
    }
}
