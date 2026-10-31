package com.dian.prep.domain.service;

import com.dian.prep.domain.model.Concurso;
import com.dian.prep.domain.model.GeneracionJob;
import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.in.GenerarPreguntasUseCase;
import com.dian.prep.domain.port.out.ConcursoRepository;
import com.dian.prep.domain.port.out.GeneracionJobRepository;
import com.dian.prep.domain.port.out.IaGeneradorPort;
import com.dian.prep.domain.port.out.ItemRepository;
import com.dian.prep.domain.port.out.PreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GeneracionPreguntasService implements GenerarPreguntasUseCase {

    /**
     * Preguntas que, como mínimo, se esperan de cada enunciado (caso de lectura).
     * Es el factor para estimar el total de preguntas del job a partir de los
     * enunciados solicitados. El redactor puede producir 3-4 por enunciado; se usa
     * el mínimo (3) para el objetivo, de modo que el job no quede corto.
     */
    private static final int PREGUNTAS_POR_ENUNCIADO = 3;

    private final ItemRepository itemRepository;
    private final ConcursoRepository concursoRepository;
    private final PreguntaRepository preguntaRepository;
    private final GeneracionJobRepository jobRepository;
    private final GeneracionPreguntasRunner runner;

    public GeneracionPreguntasService(ItemRepository itemRepository,
                                      ConcursoRepository concursoRepository,
                                      PreguntaRepository preguntaRepository,
                                      GeneracionJobRepository jobRepository,
                                      GeneracionPreguntasRunner runner) {
        this.itemRepository     = itemRepository;
        this.concursoRepository = concursoRepository;
        this.preguntaRepository = preguntaRepository;
        this.jobRepository      = jobRepository;
        this.runner             = runner;
    }

    @Override
    public GeneracionJob iniciarGeneracion(UUID itemId) {
        return iniciarGeneracion(itemId, null);
    }

    @Override
    public GeneracionJob iniciarGeneracion(UUID itemId, Integer cantidad) {
        Item item = itemRepository.buscarPorId(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Ítem no encontrado: " + itemId));

        // No permitir una generación simultánea sobre el mismo ítem.
        jobRepository.buscarUltimoPorItemId(itemId).ifPresent(job -> {
            if (job.getEstado() == GeneracionJob.EstadoGeneracion.PENDIENTE
                    || job.getEstado() == GeneracionJob.EstadoGeneracion.EN_PROCESO) {
                throw new IllegalArgumentException("Ya hay una generación en curso para este ítem");
            }
        });

        // Si el cliente envía una cantidad, prevalece sobre la del ítem y se
        // persiste, de modo que lo editado en pantalla sea la fuente de verdad
        // sin requerir un guardado previo por separado.
        if (cantidad != null) {
            if (cantidad < 1 || cantidad > 200) {
                throw new IllegalArgumentException("La cantidad de enunciados debe estar entre 1 y 200");
            }
            if (!cantidad.equals(item.getPreguntasSolicitadas())) {
                item.setPreguntasSolicitadas(cantidad);
                item = itemRepository.guardar(item);
            }
        }

        // La cantidad solicitada del ítem se interpreta como ENUNCIADOS (casos de lectura).
        // Cada enunciado produce varias preguntas (ver PREGUNTAS_POR_ENUNCIADO), de modo que
        // el total esperado del job —que alimenta la barra de progreso— va en preguntas.
        int enunciados = item.getPreguntasSolicitadas() != null ? item.getPreguntasSolicitadas() : 0;
        if (enunciados <= 0) {
            throw new IllegalArgumentException("El ítem no tiene una cantidad de enunciados solicitada válida");
        }
        int total = enunciados * PREGUNTAS_POR_ENUNCIADO;

        Concurso concurso = concursoRepository.buscarPorId(item.getConcursoId()).orElse(null);
        String nombreConcurso = concurso != null ? concurso.getNombre() : "Concurso de méritos";
        String denominacion   = concurso != null ? concurso.getDenominacion() : null;

        GeneracionJob job = jobRepository.guardar(GeneracionJob.crear(itemId, total));

        var contexto = new IaGeneradorPort.ContextoGeneracion(
                item.getTipoPrueba(),
                item.getNombre(),
                item.getNivelCompetencia(),
                nombreConcurso,
                denominacion,
                item.getDescripcion());

        runner.ejecutar(job, item, contexto);
        return job;
    }

    @Override
    public GeneracionJob consultarEstado(UUID itemId) {
        return jobRepository.buscarUltimoPorItemId(itemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay generación registrada para el ítem: " + itemId));
    }

    @Override
    public List<Pregunta> listarPreguntas(UUID itemId) {
        return preguntaRepository.buscarPorItemId(itemId);
    }
}
