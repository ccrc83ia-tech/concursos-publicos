package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "preguntas")
@Getter
@Setter
public class PreguntaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(name = "opcion_a", nullable = false, columnDefinition = "TEXT")
    private String opcionA;

    @Column(name = "opcion_b", nullable = false, columnDefinition = "TEXT")
    private String opcionB;

    @Column(name = "opcion_c", nullable = false, columnDefinition = "TEXT")
    private String opcionC;

    @Column(name = "opcion_d", nullable = false, columnDefinition = "TEXT")
    private String opcionD;

    @Column(name = "respuesta_correcta", nullable = false, length = 1)
    private String respuestaCorrecta;

    @Column(name = "justificacion_correcta", nullable = false, columnDefinition = "TEXT")
    private String justificacionCorrecta;

    @Column(name = "justificacion_incorrectas", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String justificacionIncorrectas; // JSON serializado {"A":"...","B":"..."}

    @Column(name = "referencia_normativa", length = 500)
    private String referenciaNormativa;

    @Column(name = "nivel_dificultad", nullable = false, length = 20)
    private String nivelDificultad = "INTERMEDIO";

    @Column(name = "proceso_cognitivo", nullable = false, length = 20)
    private String procesoCognitivo = "APLICACION";

    @Column(name = "generada_por_ia", nullable = false)
    private boolean generadaPorIa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- Calidad del banco (doble IA) ---

    @Column(name = "estado_calidad", nullable = false, length = 25)
    private String estadoCalidad = "PENDIENTE_VALIDACION";

    @Column(name = "origen_modelo", nullable = false, length = 15)
    private String origenModelo = "PROVISIONAL";

    @Column(name = "hash_contenido", length = 64)
    private String hashContenido;

    @Column(name = "intentos_generacion", nullable = false)
    private int intentosGeneracion = 0;

    @Column(name = "version_generacion", nullable = false)
    private int versionGeneracion = 1;

    @Column(name = "validada_at")
    private LocalDateTime validadaAt;
}
