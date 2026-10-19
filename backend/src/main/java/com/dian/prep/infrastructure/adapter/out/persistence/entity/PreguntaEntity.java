package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "preguntas")
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

    @Column(name = "generada_por_ia", nullable = false)
    private boolean generadaPorIa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId()                            { return id; }
    public void setId(UUID id)                     { this.id = id; }
    public UUID getItemId()                        { return itemId; }
    public void setItemId(UUID itemId)             { this.itemId = itemId; }
    public String getEnunciado()                   { return enunciado; }
    public void setEnunciado(String e)             { this.enunciado = e; }
    public String getOpcionA()                     { return opcionA; }
    public void setOpcionA(String o)               { this.opcionA = o; }
    public String getOpcionB()                     { return opcionB; }
    public void setOpcionB(String o)               { this.opcionB = o; }
    public String getOpcionC()                     { return opcionC; }
    public void setOpcionC(String o)               { this.opcionC = o; }
    public String getOpcionD()                     { return opcionD; }
    public void setOpcionD(String o)               { this.opcionD = o; }
    public String getRespuestaCorrecta()           { return respuestaCorrecta; }
    public void setRespuestaCorrecta(String r)     { this.respuestaCorrecta = r; }
    public String getJustificacionCorrecta()       { return justificacionCorrecta; }
    public void setJustificacionCorrecta(String j) { this.justificacionCorrecta = j; }
    public String getJustificacionIncorrectas()    { return justificacionIncorrectas; }
    public void setJustificacionIncorrectas(String j) { this.justificacionIncorrectas = j; }
    public String getReferenciaNormativa()         { return referenciaNormativa; }
    public void setReferenciaNormativa(String r)   { this.referenciaNormativa = r; }
    public String getNivelDificultad()             { return nivelDificultad; }
    public void setNivelDificultad(String n)       { this.nivelDificultad = n; }
    public boolean isGeneradaPorIa()               { return generadaPorIa; }
    public void setGeneradaPorIa(boolean g)        { this.generadaPorIa = g; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }
}
