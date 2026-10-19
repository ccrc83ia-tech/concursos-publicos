package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Recomendacion;

import java.util.List;
import java.util.UUID;

public interface RecomendacionRepository {

    Recomendacion guardar(Recomendacion recomendacion);

    List<Recomendacion> guardarTodas(List<Recomendacion> recomendaciones);

    /** Recomendaciones de un simulacro concreto. */
    List<Recomendacion> buscarPorSimulacroId(UUID simulacroId);

    /**
     * Última recomendación por cada ítem de un concurso para un usuario
     * (la más reciente de cada ítem). Alimenta el panel de plan de estudio.
     */
    List<Recomendacion> buscarUltimoPorConcurso(UUID concursoId, UUID usuarioId);
}
