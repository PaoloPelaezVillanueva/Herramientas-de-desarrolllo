package com.equipo.tambo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sale_submissions")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleSubmission {
    @Id
    @Column(name = "submission_id", length = 57)
    private String id;

    @Column(nullable = false, length = 64)
    private String fingerprint;

    // The key remains reserved if the sale is deleted.
    @Column(name = "sale_id", nullable = false)
    private Long saleId;
}
