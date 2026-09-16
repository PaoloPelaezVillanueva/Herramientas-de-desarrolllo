package com.equipo.tambo.dto;

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
    /* TODO: Cambiar variable a ClientEntity */
    private Long client;
    private UserEntity user;
    private LocalDateTime date;
}