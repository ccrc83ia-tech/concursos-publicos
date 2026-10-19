package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.Concurso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConcursoResponse(
    UUID          id,
    String        nombre,
    String        procesoSeleccion,
    String        numeroInscripcion,
    String        numeroEmpleo,
    String        nivelCargo,
    String        denominacion,
    LocalDate     fechaExamen,
    Long          diasRestantes,
    boolean       activo,
    LocalDateTime createdAt
) {
    public static ConcursoResponse from(Concurso c) {
        return new ConcursoResponse(
            c.getId(),
            c.getNombre(),
            c.getProcesoSeleccion(),
            c.getNumeroInscripcion(),
            c.getNumeroEmpleo(),
            c.getNivelCargo(),
            c.getDenominacion(),
            c.getFechaExamen(),
            c.diasRestantes(),
            c.isActivo(),
            c.getCreatedAt()
        );
    }
}
