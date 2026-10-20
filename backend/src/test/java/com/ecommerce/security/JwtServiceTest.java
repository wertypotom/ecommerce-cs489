package com.ecommerce.security;

import com.ecommerce.domain.Role;
import com.ecommerce.domain.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET =
            "Y29tLWVjb21tZXJjZS1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1hdC1sZWFzdC0yNTYtYml0cw==";

    private static final long ONE_DAY_MS = 86_400_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = newService(SECRET, ONE_DAY_MS);
    }

    @Test
    void generateTokenEmbedsUsernameRoleAndUserId() {
        String token = jwtService.generateToken(customUser(3L, "admin@test.com", Role.ADMIN));

        assertEquals("admin@test.com", jwtService.extractUsername(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
        assertEquals(3, jwtService.extractClaim(token, c -> c.get("userId", Number.class)).intValue());
    }

    @Test
    void generateTokenOmitsCustomClaimsForPlainUserDetails() {
        UserDetails plain = org.springframework.security.core.userdetails.User
                .withUsername("plain@test.com")
                .password("hash")
                .authorities("ROLE_CUSTOMER")
                .build();

        String token = jwtService.generateToken(plain);

        assertEquals("plain@test.com", jwtService.extractUsername(token));
        assertNull(jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token, plain));
    }

    @Test
    void isTokenValidOnlyForTheUserItWasIssuedTo() {
        CustomUserDetails alice = customUser(1L, "alice@test.com", Role.CUSTOMER);
        CustomUserDetails bob = customUser(2L, "bob@test.com", Role.CUSTOMER);

        String token = jwtService.generateToken(alice);

        assertTrue(jwtService.isTokenValid(token, alice));
        assertFalse(jwtService.isTokenValid(token, bob));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService alreadyExpired = newService(SECRET, -1000L);
        CustomUserDetails user = customUser(1L, "alice@test.com", Role.CUSTOMER);

        String token = alreadyExpired.generateToken(user);

        assertThrows(ExpiredJwtException.class, () -> alreadyExpired.isTokenValid(token, user));
    }

    @Test
    void tokenSignedWithADifferentSecretIsRejected() {
        // Must be >= 512 bits: the configured secret resolves to an HS512 key, and jjwt
        // rejects a shorter verification key with WeakKeyException before checking the signature.
        String otherSecret = Base64.getEncoder().encodeToString(
                "a-completely-different-unit-test-signing-key-long-enough-for-hmac-sha512"
                        .getBytes(StandardCharsets.UTF_8));
        JwtService otherService = newService(otherSecret, ONE_DAY_MS);

        String token = jwtService.generateToken(customUser(1L, "alice@test.com", Role.CUSTOMER));
        CustomUserDetails sameUser = customUser(1L, "alice@test.com", Role.CUSTOMER);

        assertThrows(SignatureException.class, () -> otherService.isTokenValid(token, sameUser));
    }

    private static JwtService newService(String secret, long expiration) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "expiration", expiration);
        return service;
    }

    private static CustomUserDetails customUser(Long id, String email, Role role) {
        User user = new User(email, "hash", role, null);
        user.setId(id);
        return new CustomUserDetails(user);
    }
}
