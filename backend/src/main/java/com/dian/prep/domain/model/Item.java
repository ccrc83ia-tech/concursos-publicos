package com.dian.prep.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Item {

    private UUID id;
    private UUID concursoId;
    private String nombre;
    private String descripcion;
    private TipoPrueba tipoPrueba;
    private Integer nivelCompetencia; // solo para comportamentales: 1,2,3,4
    private Double pesoPorcentual;
    private Integer preguntasSolicitadas;
    private DominioPrevio dominioPrevio;
    private Double umbralBrecha;
    private Integer orden;
    private LocalDateTime createdAt;

    public enum TipoPrueba {
        FUNCIONAL, COMPORTAMENTAL, INTEGRIDAD
    }

    public enum DominioPrevio {
        NO_SE, ALGO_SE, LO_DOMINO;

        /** Sugerencia automática de preguntas según dominio previo */
        public int sugerirCantidadPreguntas() {
            return switch (this) {
                case NO_SE     -> 100;
                case ALGO_SE   -> 60;
                case LO_DOMINO -> 30;
            };
        }
    }

    public static Item crear(UUID concursoId, String nombre, TipoPrueba tipo) {
        Item item = new Item();
        item.concursoId           = concursoId;
        item.nombre               = nombre;
        item.tipoPrueba           = tipo;
        item.preguntasSolicitadas = tipo == TipoPrueba.FUNCIONAL ? 100 : 50;
        item.dominioPrevio        = DominioPrevio.NO_SE;
        item.umbralBrecha         = tipo == TipoPrueba.FUNCIONAL ? 70.0 : 60.0;
        item.pesoPorcentual       = 0.0;
        item.orden                = 0;
        item.createdAt            = LocalDateTime.now();
        return item;
    }

    // Getters y setters
    public UUID getId()                                   { return id; }
    public void setId(UUID id)                            { this.id = id; }
    public UUID getConcursoId()                           { return concursoId; }
    public void setConcursoId(UUID concursoId)            { this.concursoId = concursoId; }
    public String getNombre()                             { return nombre; }
    public void setNombre(String nombre)                  { this.nombre = nombre; }
    public String getDescripcion()                        { return descripcion; }
    public void setDescripcion(String descripcion)        { this.descripcion = descripcion; }
    public TipoPrueba getTipoPrueba()                     { return tipoPrueba; }
    public void setTipoPrueba(TipoPrueba tipoPrueba)      { this.tipoPrueba = tipoPrueba; }
    public Integer getNivelCompetencia()                  { return nivelCompetencia; }
    public void setNivelCompetencia(Integer nivel)        { this.nivelCompetencia = nivel; }
    public Double getPesoPorcentual()                     { return pesoPorcentual; }
    public void setPesoPorcentual(Double peso)            { this.pesoPorcentual = peso; }
    public Integer getPreguntasSolicitadas()              { return preguntasSolicitadas; }
    public void setPreguntasSolicitadas(Integer p)        { this.preguntasSolicitadas = p; }
    public DominioPrevio getDominioPrevio()               { return dominioPrevio; }
    public void setDominioPrevio(DominioPrevio d)         { this.dominioPrevio = d; }
    public Double getUmbralBrecha()                       { return umbralBrecha; }
    public void setUmbralBrecha(Double umbral)            { this.umbralBrecha = umbral; }
    public Integer getOrden()                             { return orden; }
    public void setOrden(Integer orden)                   { this.orden = orden; }
    public LocalDateTime getCreatedAt()                   { return createdAt; }
    public void setCreatedAt(LocalDateTime t)             { this.createdAt = t; }
}
