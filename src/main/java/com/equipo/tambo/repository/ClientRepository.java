package com.equipo.tambo.repository;

import com.equipo.tambo.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByDni(String dni);
    boolean existsByDniAndIdNot(String dni, Long id);
}
