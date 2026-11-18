package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.ChangePasswordRequest;
import com.equipo.tambo.dto.LoginRequest;
import com.equipo.tambo.dto.RegisterRequest;
import com.equipo.tambo.entity.Role;
import com.equipo.tambo.entity.User;
import com.equipo.tambo.repository.RoleRepository;
import com.equipo.tambo.repository.UserRepository;
import com.equipo.tambo.security.CustomUserDetails;
import com.equipo.tambo.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(authenticationManager, userRepository, roleRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginReturnsTokenForAuthenticatedUser() {
        User user = user();
        CustomUserDetails details = new CustomUserDetails(user);
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        when(jwtService.generateToken(details)).thenReturn("signed-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var response = service.login(new LoginRequest("paolo", "password"));

        assertEquals("signed-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600, response.expiresIn());
        verify(authenticationManager).authenticate(argThat(auth ->
                auth.getPrincipal().equals("paolo") && auth.getCredentials().equals("password")));
    }

    @Test
    void loginRejectsInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("paolo", "wrong")));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerSavesEncodedPasswordAndUserRole() {
        Role role = new Role(2L, "USER");
        when(roleRepository.findByRole("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        var response = service.register(new RegisterRequest("Paolo", "Pelaez", "paolo", "password"));

        assertEquals(7L, response.getId());
        assertEquals("Paolo", response.getName());
        assertEquals("Pelaez", response.getLastname());
        assertEquals("paolo", response.getUser());
        assertSame(role, response.getRole());
        verify(userRepository).save(argThat(saved ->
                saved.getPassword().equals("encoded") && saved.getRole() == role));
    }

    @Test
    void registerRejectsExistingUsername() {
        when(userRepository.existsByUser("paolo")).thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.register(new RegisterRequest("Paolo", "Pelaez", "paolo", "password")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRequiresConfiguredUserRole() {
        when(roleRepository.findByRole("USER")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.register(new RegisterRequest("Paolo", "Pelaez", "paolo", "password")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getCurrentUserReturnsRepositoryUser() {
        User user = user();
        when(userRepository.findByUser("paolo")).thenReturn(Optional.of(user));

        var response = service.getCurrentUser("paolo");

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getLastname(), response.getLastname());
        assertSame(user.getRole(), response.getRole());
    }

    @Test
    void getCurrentUserRejectsUnknownUsername() {
        when(userRepository.findByUser("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.getCurrentUser("missing"));
    }

    @Test
    void changePasswordSavesNewEncodedPassword() {
        User user = user();
        when(userRepository.findByUser("paolo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "stored-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changePassword("paolo", new ChangePasswordRequest("old", "new-password", "new-password"));

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        assertPasswordChangeRejected(new ChangePasswordRequest("wrong", "new-password", "new-password"));
    }

    @Test
    void changePasswordRejectsMismatchedConfirmation() {
        when(passwordEncoder.matches("old", "stored-hash")).thenReturn(true);
        assertPasswordChangeRejected(new ChangePasswordRequest("old", "new-password", "different"));
    }

    @Test
    void changePasswordRejectsReusedPassword() {
        when(passwordEncoder.matches("old", "stored-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "stored-hash")).thenReturn(true);
        assertPasswordChangeRejected(new ChangePasswordRequest("old", "new-password", "new-password"));
    }

    private void assertPasswordChangeRejected(ChangePasswordRequest request) {
        User user = user();
        when(userRepository.findByUser("paolo")).thenReturn(Optional.of(user));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.changePassword("paolo", request));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("stored-hash", user.getPassword());
        verify(userRepository, never()).save(any());
    }

    private User user() {
        return new User(7L, "Paolo", "Pelaez", "paolo", "stored-hash", new Role(2L, "USER"));
    }
}
