package com.equipo.tambo.repository;

import com.equipo.tambo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUser(String user);
    boolean existsByUserAndIdNot(String user, Long id);
}
