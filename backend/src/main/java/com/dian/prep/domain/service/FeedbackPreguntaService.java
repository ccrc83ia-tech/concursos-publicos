package com.dian.prep.domain.service;

import com.dian.prep.domain.model.FeedbackPregunta;
import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.model.Simulacro;
import com.dian.prep.domain.port.in.ReportarPreguntaUseCase;
import com.dian.prep.domain.port.out.FeedbackPreguntaRepository;
import com.dian.prep.domain.port.out.PreguntaRepository;
import com.dian.prep.domain.port.out.SimulacroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Gestiona los reportes de los aspirantes sobre preguntas. Cada reporte queda
 * registrado y, al acumular un número de reportes de usuarios distintos igual o
 * superior al umbral, la pregunta se degrada a RECHAZADA: deja de servirse en
 * simulacros y el job de limpieza la elimina. Así el usuario actúa como un
 * segundo filtro de calidad sobre lo que el validador IA dejó pasar.
 */
@Service
public class FeedbackPreguntaService implements ReportarPreguntaUseCase {

    private static final Logger log = LoggerFactory.getLogger(FeedbackPreguntaService.class);

    /** Reportes distintos que retiran una pregunta del banco. */
    private static final int UMBRAL_RETIRO = 3;

    private final SimulacroRepository simulacroRepository;
    private final PreguntaRepository preguntaRepository;
    private final FeedbackPreguntaRepository feedbackRepository;

    public FeedbackPreguntaService(SimulacroRepository simulacroRepository,
                                   PreguntaRepository preguntaRepository,
                                   FeedbackPreguntaRepository feedbackRepository) {
        this.simulacroRepository = simulacroRepository;
        this.preguntaRepository  = preguntaRepository;
        this.feedbackRepository  = feedbackRepository;
    }

    @Override
    public ResultadoReporte reportar(UUID simulacroId, UUID usuarioId, FeedbackCommand cmd) {
        if (cmd.preguntaId() == null) {
            throw new IllegalArgumentException("La pregunta a reportar es obligatoria");
        }
        if (cmd.tipo() == null) {
            throw new IllegalArgumentException("El tipo de reporte es obligatorio");
        }

        // 1) El simulacro debe existir y pertenecer al usuario.
        Simulacro simulacro = simulacroRepository.buscarPorId(simulacroId)
                .orElseThrow(() -> new IllegalArgumentException("Simulacro no encontrado: " + simulacroId));
        if (!simulacro.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("El simulacro no pertenece al usuario");
        }

        // 2) La pregunta reportada debe ser una de las del simulacro.
        boolean perteneceAlSimulacro = simulacro.getPreguntas().stream()
                .anyMatch(sp -> cmd.preguntaId().equals(sp.getPreguntaId()));
        if (!perteneceAlSimulacro) {
            throw new IllegalArgumentException("La pregunta no pertenece a este simulacro");
        }

        // 3) Un usuario solo reporta una vez la misma pregunta (idempotente).
        if (feedbackRepository.yaReporto(cmd.preguntaId(), usuarioId)) {
            long acumulados = feedbackRepository.contarPorPregunta(cmd.preguntaId());
            return new ResultadoReporte(acumulados, false);
        }

        feedbackRepository.guardar(
                FeedbackPregunta.crear(cmd.preguntaId(), usuarioId, cmd.tipo(), cmd.comentario()));
        long acumulados = feedbackRepository.contarPorPregunta(cmd.preguntaId());

        // 4) Si se alcanza el umbral, retirar la pregunta del banco (RECHAZADA).
        boolean retirada = false;
        if (acumulados >= UMBRAL_RETIRO) {
            retirada = preguntaRepository.buscarPorId(cmd.preguntaId())
                    .filter(p -> p.getEstadoCalidad() != Pregunta.EstadoCalidad.RECHAZADA
                              && p.getEstadoCalidad() != Pregunta.EstadoCalidad.AGOTADA)
                    .map(p -> {
                        p.setEstadoCalidad(Pregunta.EstadoCalidad.RECHAZADA);
                        preguntaRepository.guardar(p);
                        log.info("Pregunta {} retirada del banco por {} reportes de usuarios",
                                p.getId(), acumulados);
                        return true;
                    })
                    .orElse(false);
        }

        return new ResultadoReporte(acumulados, retirada);
    }
}
