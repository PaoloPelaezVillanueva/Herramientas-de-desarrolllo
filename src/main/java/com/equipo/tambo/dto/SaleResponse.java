package com.equipo.tambo.dto;

import com.equipo.tambo.entity.Client;
import com.equipo.tambo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SaleResponse {
    
    private Long id;
    private Client client;
    private User user;
    private LocalDateTime date;
    private BigDecimal total;
    private List<SaleDetailResponse> details;
}