package com.dian.prep.infrastructure.adapter.in.web.dto;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.model.Simulacro;
import com.dian.prep.domain.model.SimulacroPregunta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record SimulacroResponse(
        UUID    id,
        UUID    concursoId,
        String  tipo,
        String  estado,
        Integer tiempoLimiteSegundos,
        Integer tiempoUsadoSegundos,
        Double  puntajeFinal,
        List<PreguntaSimulacroDto> preguntas
) {

    /**
     * Pregunta tal como se envía al cliente. Mientras el simulacro está EN_CURSO
     * NO se incluyen la respuesta correcta ni las justificaciones (anti-trampa);
     * al COMPLETARSE se revelan para la retroalimentación.
     */
    public record PreguntaSimulacroDto(
            UUID    simulacroPreguntaId,
            UUID    preguntaId,
            int     orden,
            String  enunciado,
            String  opcionA,
            String  opcionB,
            String  opcionC,
            String  opcionD,
            String  respuestaDada,
            boolean marcadaParaRevision,
            // Solo presentes cuando el simulacro está COMPLETADO:
            String  respuestaCorrecta,
            Boolean esCorrecta,
            String  justificacionCorrecta,
            String  referenciaNormativa
    ) {}

    public static SimulacroResponse from(Simulacro s, List<Pregunta> contenido) {
        boolean completado = s.getEstado() == Simulacro.EstadoSimulacro.COMPLETADO;

        Map<UUID, Pregunta> porId = contenido.stream()
                .collect(Collectors.toMap(Pregunta::getId, p -> p, (a, b) -> a));

        List<PreguntaSimulacroDto> dtos = new ArrayList<>();
        s.getPreguntas().stream()
                .sorted(Comparator.comparingInt(SimulacroPregunta::getOrden))
                .forEach(sp -> {
                    Pregunta p = porId.get(sp.getPreguntaId());
                    if (p == null) return;
                    dtos.add(new PreguntaSimulacroDto(
                            sp.getId(),
                            sp.getPreguntaId(),
                            sp.getOrden(),
                            p.getEnunciado(),
                            p.getOpcionA(), p.getOpcionB(), p.getOpcionC(), p.getOpcionD(),
                            sp.getRespuestaDada() != null ? sp.getRespuestaDada().name() : null,
                            sp.isMarcadaParaRevision(),
                            completado && p.getRespuestaCorrecta() != null ? p.getRespuestaCorrecta().name() : null,
                            completado ? sp.getEsCorrecta() : null,
                            completado ? p.getJustificacionCorrecta() : null,
                            completado ? p.getReferenciaNormativa() : null));
                });

        return new SimulacroResponse(
                s.getId(),
                s.getConcursoId(),
                s.getTipo().name(),
                s.getEstado().name(),
                s.getTiempoLimiteSegundos(),
                s.getTiempoUsadoSegundos(),
                s.getPuntajeFinal(),
                dtos);
    }
}
