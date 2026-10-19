package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.DesempenoItem;

import java.util.List;
import java.util.UUID;

public interface DesempenoRepository {

    List<DesempenoItem> guardarTodos(List<DesempenoItem> desempenos);

    List<DesempenoItem> buscarPorSimulacroId(UUID simulacroId);

    /**
     * Último desempeño registrado por cada ítem de un concurso para un usuario
     * (el más reciente de cada ítem). Alimenta el dashboard de brechas.
     */
    List<DesempenoItem> buscarUltimoPorConcurso(UUID concursoId, UUID usuarioId);
}
