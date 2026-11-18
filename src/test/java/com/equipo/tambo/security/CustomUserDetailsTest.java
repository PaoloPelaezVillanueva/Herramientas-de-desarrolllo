package com.equipo.tambo.security;

import com.equipo.tambo.entity.Role;
import com.equipo.tambo.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {
    @Test
    void exposesUserFieldsAndNormalizesRole() {
        User user = new User(7L, "Paolo", "Pelaez", "paolo", "hash", new Role(2L, " user "));
        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
        assertEquals("hash", details.getPassword());
        assertEquals("paolo", details.getUsername());
        assertEquals(7L, details.getId());
        assertEquals("Paolo", details.getName());
        assertEquals("Pelaez", details.getLastname());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }

    @Test
    void doesNotDuplicateRolePrefix() {
        User user = new User(7L, "Paolo", "Pelaez", "paolo", "hash", new Role(2L, "role_admin"));

        assertEquals("ROLE_ADMIN", new CustomUserDetails(user).getAuthorities().iterator().next().getAuthority());
    }
}
