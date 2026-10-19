package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "desempeno_item")
@Getter
@Setter
public class DesempenoItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "simulacro_id", nullable = false)
    private UUID simulacroId;

    @Column(name = "preguntas_correctas", nullable = false)
    private int preguntasCorrectas = 0;

    @Column(name = "total_preguntas", nullable = false)
    private int totalPreguntas = 0;

    @Column(name = "puntaje_porcentual", nullable = false)
    private double puntajePorcentual = 0;

    @Column(name = "nivel_dominio", nullable = false, length = 20)
    private String nivelDominio = "ROJO";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
