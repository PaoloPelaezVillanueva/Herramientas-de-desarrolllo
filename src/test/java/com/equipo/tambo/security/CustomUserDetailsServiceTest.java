package com.equipo.tambo.security;

import com.equipo.tambo.entity.Role;
import com.equipo.tambo.entity.User;
import com.equipo.tambo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {
    @Test
    void loadsExistingUser() {
        UserRepository repository = mock(UserRepository.class);
        User user = new User(7L, "Paolo", "Pelaez", "paolo", "hash", new Role(2L, "USER"));
        when(repository.findByUser("paolo")).thenReturn(Optional.of(user));

        var details = new CustomUserDetailsService(repository).loadUserByUsername("paolo");

        assertEquals("paolo", details.getUsername());
        assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void rejectsUnknownUser() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByUser("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> new CustomUserDetailsService(repository).loadUserByUsername("missing"));
    }
}
