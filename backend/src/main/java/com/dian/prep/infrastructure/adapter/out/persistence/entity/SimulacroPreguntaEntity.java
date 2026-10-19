package com.dian.prep.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "simulacro_preguntas")
public class SimulacroPreguntaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulacro_id", nullable = false)
    private SimulacroEntity simulacro;

    @Column(name = "pregunta_id", nullable = false)
    private UUID preguntaId;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "respuesta_dada", length = 1)
    private String respuestaDada;

    @Column(name = "es_correcta")
    private Boolean esCorrecta;

    @Column(name = "tiempo_respuesta_segundos")
    private int tiempoRespuestaSegundos = 0;

    @Column(name = "marcada_para_revision", nullable = false)
    private boolean marcadaParaRevision = false;

    @Column(nullable = false)
    private int orden = 0;

    public UUID getId()                           { return id; }
    public void setId(UUID id)                    { this.id = id; }
    public SimulacroEntity getSimulacro()         { return simulacro; }
    public void setSimulacro(SimulacroEntity s)   { this.simulacro = s; }
    public UUID getPreguntaId()                   { return preguntaId; }
    public void setPreguntaId(UUID preguntaId)    { this.preguntaId = preguntaId; }
    public UUID getItemId()                       { return itemId; }
    public void setItemId(UUID itemId)            { this.itemId = itemId; }
    public String getRespuestaDada()              { return respuestaDada; }
    public void setRespuestaDada(String r)        { this.respuestaDada = r; }
    public Boolean getEsCorrecta()                { return esCorrecta; }
    public void setEsCorrecta(Boolean b)          { this.esCorrecta = b; }
    public int getTiempoRespuestaSegundos()       { return tiempoRespuestaSegundos; }
    public void setTiempoRespuestaSegundos(int t) { this.tiempoRespuestaSegundos = t; }
    public boolean isMarcadaParaRevision()        { return marcadaParaRevision; }
    public void setMarcadaParaRevision(boolean m) { this.marcadaParaRevision = m; }
    public int getOrden()                         { return orden; }
    public void setOrden(int orden)               { this.orden = orden; }
}
