package com.dian.prep.domain.port.in;

import com.dian.prep.domain.model.DesempenoItem;
import com.dian.prep.domain.model.Simulacro;

import java.util.List;
import java.util.UUID;

public interface GestionarSimulacrosUseCase {

    record CrearSimulacroCommand(
            UUID usuarioId,
            UUID concursoId,
            Simulacro.TipoSimulacro tipo,
            List<UUID> itemIds,         // null/vacío = todos los ítems del concurso
            int preguntasPorItem,       // cuántas preguntas tomar por ítem
            Integer tiempoLimiteSegundos // null = sin límite
    ) {}

    record RespuestaCommand(
            UUID simulacroPreguntaId,
            String respuesta,           // "A".."D" o null para dejar sin responder
            boolean marcadaParaRevision,
            int tiempoRespuestaSegundos
    ) {}

    /** Crea un simulacro armando las preguntas; falla si no hay preguntas disponibles. */
    Simulacro crear(CrearSimulacroCommand command);

    Simulacro obtener(UUID simulacroId, UUID usuarioId);

    /** Registra (o actualiza) la respuesta de una pregunta del simulacro. */
    void responder(UUID simulacroId, UUID usuarioId, RespuestaCommand command);

    /** Finaliza el simulacro, calcula puntaje y desempeño por ítem. */
    Simulacro finalizar(UUID simulacroId, UUID usuarioId, int tiempoUsadoSegundos);

    /** Contenido de las preguntas del simulacro, en orden (enunciado y opciones). */
    List<com.dian.prep.domain.model.Pregunta> contenidoPreguntas(UUID simulacroId, UUID usuarioId);

    /** Desempeño por ítem del simulacro (para la pantalla de resultado y brechas). */
    List<DesempenoItem> desempeno(UUID simulacroId, UUID usuarioId);

    /** Último desempeño por ítem del concurso (dashboard de brechas). */
    List<DesempenoItem> brechasPorConcurso(UUID concursoId, UUID usuarioId);
}
