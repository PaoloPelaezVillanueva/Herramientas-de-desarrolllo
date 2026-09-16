package com.equipo.tambo.service;

import com.equipo.tambo.dto.UserRequest;
import com.equipo.tambo.dto.UserResponse;
import com.equipo.tambo.entity.RoleEntity;
import com.equipo.tambo.entity.UserEntity;
import com.equipo.tambo.repository.RoleRepository;
import com.equipo.tambo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        return toResponse(getUser(id));
    }

    private UserEntity getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el usuario con ID " + id
                ));
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUser(request.getUser())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario '" + request.getUser() + "' ya existe.");
        }

        RoleEntity role = roleRepository.findById(request.getRole())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el rol con ID " + request.getRole()
                ));

        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setUser(request.getUser());
        user.setPassword(request.getPassword());
        user.setRole(role);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        RoleEntity role = roleRepository.findById(request.getRole())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el rol con ID " + request.getRole()
                ));

        UserEntity user = getUser(id);

        if (userRepository.existsByUserAndIdNot(request.getUser(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario '" + request.getUser() + "' ya existe.");
        }

        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setUser(request.getUser());
        user.setPassword(request.getPassword());
        user.setRole(role);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = getUser(id);
        userRepository.delete(user);
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getUser(),
                user.getRole()
        );
    }
}
