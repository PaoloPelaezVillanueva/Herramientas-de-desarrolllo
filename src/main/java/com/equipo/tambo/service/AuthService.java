package com.equipo.tambo.service;

import com.equipo.tambo.dto.AuthResponse;
import com.equipo.tambo.dto.LoginRequest;
import com.equipo.tambo.dto.RegisterRequest;
import com.equipo.tambo.dto.UserResponse;
import com.equipo.tambo.entity.Role;
import com.equipo.tambo.entity.User;
import com.equipo.tambo.repository.RoleRepository;
import com.equipo.tambo.repository.UserRepository;
import com.equipo.tambo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.user(),
                            request.password()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            return toResponse(userDetails);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if(userRepository.existsByUser(request.user())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya se encuentra registrado");
        }

        Role role = roleRepository.findByRole("USER").orElseThrow(() -> new IllegalStateException("No existe el rol USER en la base de datos"));

        User user = new User();
        user.setName(request.name());
        user.setLastname(request.lastname());
        user.setUser(request.user());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    private AuthResponse toResponse(CustomUserDetails userDetails) {
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getUser(),
                user.getRole()
        );
    }
}
