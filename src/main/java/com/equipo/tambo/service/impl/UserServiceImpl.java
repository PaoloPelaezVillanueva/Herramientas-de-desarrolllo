package com.equipo.tambo.service.impl;

import com.equipo.tambo.dto.UserRequest;
import com.equipo.tambo.dto.UserResponse;
import com.equipo.tambo.entity.Role;
import com.equipo.tambo.entity.User;
import com.equipo.tambo.repository.RoleRepository;
import com.equipo.tambo.repository.UserRepository;
import com.equipo.tambo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponse getById(Long id) {
        return toResponse(getUser(id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el usuario con ID " + id
                ));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        Role role = roleRepository.findById(request.getRole())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el rol con ID " + request.getRole()
                ));

        User user = getUser(id);

        if (userRepository.existsByUserAndIdNot(request.getUser(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario '" + request.getUser() + "' ya existe.");
        }

        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setUser(request.getUser());
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no puede superar los 72 bytes.");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUser(id);
        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el usuario porque tiene ventas asociadas.", ex);
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getUser(),
                user.getRole()
        );
    }
}
