package com.dian.prep.domain.port.in;

import com.dian.prep.domain.model.Item;

import java.util.List;
import java.util.UUID;

public interface GestionarItemsUseCase {

    record CrearItemCommand(
            UUID    concursoId,
            String  nombre,
            String  descripcion,
            Item.TipoPrueba tipoPrueba,
            Integer nivelCompetencia,
            Double  pesoPorcentual,
            Integer preguntasSolicitadas,
            Item.DominioPrevio dominioPrevio
    ) {}

    record ParsarIndicadoresCommand(
            UUID   concursoId,
            String textoIndicadores  // texto pegado por el usuario
    ) {}

    Item crear(CrearItemCommand command);

    /** Parsea el texto libre con los indicadores y crea los ítems automáticamente */
    List<Item> parsarYCrear(ParsarIndicadoresCommand command);

    List<Item> listarPorConcurso(UUID concursoId);

    Item actualizar(UUID itemId, CrearItemCommand command);

    void eliminar(UUID itemId);
}
