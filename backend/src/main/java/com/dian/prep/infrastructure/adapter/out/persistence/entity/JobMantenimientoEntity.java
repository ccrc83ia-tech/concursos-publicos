package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Checkpoint de un job periódico: su última fecha de corrida y un resumen.
 * La clave primaria es el identificador lógico del job (p. ej. "banco-preguntas").
 */
@Entity
@Table(name = "job_mantenimiento")
@Getter
@Setter
public class JobMantenimientoEntity {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "ultima_corrida")
    private LocalDate ultimaCorrida;

    @Column(columnDefinition = "TEXT")
    private String resumen;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
