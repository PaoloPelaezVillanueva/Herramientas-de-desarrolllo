package com.equipo.tambo.repository;

import com.equipo.tambo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUser(String user);
    boolean existsByUserAndIdNot(String user, Long id);
}
