package com.dian.prep.domain.port.in;

import com.dian.prep.domain.model.Recomendacion;

import java.util.List;
import java.util.UUID;

public interface GenerarRecomendacionesUseCase {

    /**
     * Genera (con IA) recomendaciones de estudio para los ítems con brecha del
     * simulacro indicado y las persiste. Idempotente por simulacro: si ya existen
     * recomendaciones para ese simulacro, las devuelve sin volver a generarlas.
     */
    List<Recomendacion> generarParaSimulacro(UUID simulacroId, UUID usuarioId);

    /** Última recomendación por ítem del concurso (plan de estudio consolidado). */
    List<Recomendacion> listarPorConcurso(UUID concursoId, UUID usuarioId);
}
