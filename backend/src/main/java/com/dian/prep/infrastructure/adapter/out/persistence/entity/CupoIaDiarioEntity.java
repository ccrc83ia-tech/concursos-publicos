package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contador persistente de peticiones a un modelo de IA en un día concreto.
 * Único por (fecha, modelo) — ver índice en la migración V7.
 */
@Entity
@Table(name = "cupo_ia_diario")
@Getter
@Setter
public class CupoIaDiarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(name = "requests_usadas", nullable = false)
    private int requestsUsadas = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
