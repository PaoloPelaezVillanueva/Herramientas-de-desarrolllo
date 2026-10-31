package com.equipo.tambo.repository;

import com.equipo.tambo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUser(String user);
    boolean existsByUser(String user);
    boolean existsByUserAndIdNot(String user, Long id);
}
