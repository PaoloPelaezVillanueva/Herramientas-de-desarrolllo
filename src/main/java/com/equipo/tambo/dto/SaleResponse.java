package com.equipo.tambo.dto;

import com.equipo.tambo.entity.ClientEntity;
import com.equipo.tambo.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SaleResponse {
    
    private Long id;
    private ClientEntity client;
    private UserEntity user;
    private LocalDateTime date;
}