package com.dian.prep.domain.service;

import com.dian.prep.domain.model.GeneracionJob;
import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.GeneracionJobRepository;
import com.dian.prep.domain.port.out.IaGeneradorPort;
import com.dian.prep.domain.port.out.PreguntaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ejecutor asíncrono del trabajo de generación. Vive en un bean aparte del
 * servicio para que el proxy @Async de Spring funcione (la auto-invocación no
 * dispara @Async).
 *
 * <p>Genera en lotes para respetar los límites del proveedor gratuito y
 * persiste el progreso tras cada lote, de modo que el frontend pueda mostrarlo
 * en vivo y un fallo parcial no pierda lo ya generado.</p>
 */
@Component
public class GeneracionPreguntasRunner {

    private static final Logger log = LoggerFactory.getLogger(GeneracionPreguntasRunner.class);

    /** Preguntas por llamada a la IA. Lote pequeño para no exceder el límite de tokens/min del free tier. */
    private static final int TAMANO_LOTE = 5;
    /** Pausa entre lotes para respetar el rate limit (free tier ~8000 tokens/min). */
    private static final long PAUSA_ENTRE_LOTES_MS = 15_000;
    /** Reintentos por lote ante fallo transitorio. */
    private static final int MAX_REINTENTOS_LOTE = 3;

    private final IaGeneradorPort iaGenerador;
    private final PreguntaRepository preguntaRepository;
    private final GeneracionJobRepository jobRepository;

    public GeneracionPreguntasRunner(IaGeneradorPort iaGenerador,
                                     PreguntaRepository preguntaRepository,
                                     GeneracionJobRepository jobRepository) {
        this.iaGenerador        = iaGenerador;
        this.preguntaRepository = preguntaRepository;
        this.jobRepository      = jobRepository;
    }

    @Async("generacionPreguntasExecutor")
    public void ejecutar(GeneracionJob job, Item item, IaGeneradorPort.ContextoGeneracion contexto) {
        try {
            job.marcarEnProceso();
            jobRepository.guardar(job);

            int restantes = job.getTotalEsperadas();
            while (restantes > 0) {
                int cantidad = Math.min(TAMANO_LOTE, restantes);
                List<Pregunta> lote = generarLoteConReintentos(contexto, cantidad);

                if (!lote.isEmpty()) {
                    lote.forEach(p -> p.setItemId(item.getId()));
                    preguntaRepository.guardarTodas(lote);
                    job.sumarGeneradas(lote.size());
                    jobRepository.guardar(job);
                }

                restantes -= cantidad;
                if (restantes > 0) pausar(PAUSA_ENTRE_LOTES_MS);
            }

            job.marcarCompletado();
            jobRepository.guardar(job);
            log.info("Generación completada para ítem {}: {} preguntas",
                    item.getId(), job.getPreguntasGeneradas());

        } catch (Exception e) {
            log.error("Error en generación para ítem {}", item.getId(), e);
            job.marcarError(e.getMessage());
            jobRepository.guardar(job);
        }
    }

    private List<Pregunta> generarLoteConReintentos(IaGeneradorPort.ContextoGeneracion contexto, int cantidad) {
        RuntimeException ultimoError = null;
        for (int intento = 1; intento <= MAX_REINTENTOS_LOTE; intento++) {
            try {
                return iaGenerador.generarLote(contexto, cantidad);
            } catch (RuntimeException e) {
                ultimoError = e;
                log.warn("Fallo lote (intento {}/{}): {}", intento, MAX_REINTENTOS_LOTE, e.getMessage());
                pausar(PAUSA_ENTRE_LOTES_MS * intento); // backoff lineal
            }
        }
        throw ultimoError != null ? ultimoError
                : new IllegalStateException("No se pudo generar el lote");
    }

    private void pausar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Generación interrumpida", ie);
        }
    }
}
