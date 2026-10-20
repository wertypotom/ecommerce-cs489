package com.ecommerce.security;

import com.ecommerce.domain.Role;
import com.ecommerce.domain.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private CustomUserDetailsService service;

    @Test
    void loadUserByUsernameMapsEntityToUserDetails() {
        User seller = new User("seller@test.com", "hashed-password", Role.SELLER, null);
        seller.setId(7L);
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));

        UserDetails result = service.loadUserByUsername("seller@test.com");

        assertEquals("seller@test.com", result.getUsername());
        assertEquals("hashed-password", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());

        CustomUserDetails custom = assertInstanceOf(CustomUserDetails.class, result);
        assertEquals(7L, custom.getUserId());
        assertEquals("SELLER", custom.getRole());
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void authoritiesArePrefixedWithRoleSoHasRoleWorks(Role role) {
        User user = new User("user@test.com", "hash", role, null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("user@test.com");

        assertEquals(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())), result.getAuthorities());
    }

    @Test
    void loadUserByUsernameThrowsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("ghost@test.com"));

        assertTrue(ex.getMessage().contains("ghost@test.com"));
    }
}
