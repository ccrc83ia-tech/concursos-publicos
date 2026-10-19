package com.dian.prep.domain.service;

import com.dian.prep.domain.model.Concurso;
import com.dian.prep.domain.model.DesempenoItem;
import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.model.Recomendacion;
import com.dian.prep.domain.model.Simulacro;
import com.dian.prep.domain.port.in.GenerarRecomendacionesUseCase;
import com.dian.prep.domain.port.out.ConcursoRepository;
import com.dian.prep.domain.port.out.DesempenoRepository;
import com.dian.prep.domain.port.out.GenerarRecomendacionPort;
import com.dian.prep.domain.port.out.GenerarRecomendacionPort.ContextoRecomendacion;
import com.dian.prep.domain.port.out.ItemRepository;
import com.dian.prep.domain.port.out.RecomendacionRepository;
import com.dian.prep.domain.port.out.SimulacroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecomendacionService implements GenerarRecomendacionesUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecomendacionService.class);

    private final SimulacroRepository simulacroRepository;
    private final ConcursoRepository concursoRepository;
    private final ItemRepository itemRepository;
    private final DesempenoRepository desempenoRepository;
    private final RecomendacionRepository recomendacionRepository;
    private final GenerarRecomendacionPort generadorIa;

    public RecomendacionService(SimulacroRepository simulacroRepository,
                                ConcursoRepository concursoRepository,
                                ItemRepository itemRepository,
                                DesempenoRepository desempenoRepository,
                                RecomendacionRepository recomendacionRepository,
                                GenerarRecomendacionPort generadorIa) {
        this.simulacroRepository     = simulacroRepository;
        this.concursoRepository      = concursoRepository;
        this.itemRepository          = itemRepository;
        this.desempenoRepository     = desempenoRepository;
        this.recomendacionRepository = recomendacionRepository;
        this.generadorIa             = generadorIa;
    }

    @Override
    public List<Recomendacion> generarParaSimulacro(UUID simulacroId, UUID usuarioId) {
        Simulacro simulacro = simulacroRepository.buscarPorId(simulacroId)
                .orElseThrow(() -> new IllegalArgumentException("Simulacro no encontrado: " + simulacroId));
        if (!simulacro.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("El simulacro no pertenece al usuario");
        }
        if (simulacro.getEstado() != Simulacro.EstadoSimulacro.COMPLETADO) {
            throw new IllegalArgumentException("Finalice el simulacro antes de generar recomendaciones");
        }

        // Idempotencia: si ya hay recomendaciones para este simulacro, devolverlas.
        List<Recomendacion> existentes = recomendacionRepository.buscarPorSimulacroId(simulacroId);
        if (!existentes.isEmpty()) {
            log.info("Recomendaciones ya existentes para simulacro={} ({}), se reutilizan",
                    simulacroId, existentes.size());
            return existentes;
        }

        List<DesempenoItem> desempenos = desempenoRepository.buscarPorSimulacroId(simulacroId);
        // Solo ítems con brecha (no VERDE): ahí es donde la recomendación aporta.
        List<DesempenoItem> conBrecha = desempenos.stream()
                .filter(d -> d.getNivelDominio() != DesempenoItem.NivelDominio.VERDE)
                .toList();
        if (conBrecha.isEmpty()) {
            log.info("Simulacro={} sin ítems con brecha: no se generan recomendaciones", simulacroId);
            return List.of();
        }
        log.info("Generando recomendaciones IA para simulacro={} usuario={} itemsConBrecha={}",
                simulacroId, usuarioId, conBrecha.size());

        Concurso concurso = concursoRepository.buscarPorId(simulacro.getConcursoId()).orElse(null);
        String nombreConcurso = concurso != null ? concurso.getNombre() : null;
        Map<UUID, Item> itemsPorId = itemRepository.buscarPorConcursoId(simulacro.getConcursoId())
                .stream().collect(Collectors.toMap(Item::getId, i -> i, (a, b) -> a));

        List<Recomendacion> generadas = new ArrayList<>();
        for (DesempenoItem d : conBrecha) {
            Item item = itemsPorId.get(d.getItemId());
            String nombreItem = item != null ? item.getNombre() : "Indicador";
            String tipoPrueba = item != null && item.getTipoPrueba() != null
                    ? item.getTipoPrueba().name() : "FUNCIONAL";

            Recomendacion r = generadorIa.generar(new ContextoRecomendacion(
                    nombreItem, tipoPrueba, nombreConcurso,
                    d.getPreguntasCorrectas(), d.getTotalPreguntas(),
                    d.getPuntajePorcentual(), d.getNivelDominio().name()));

            r.setUsuarioId(usuarioId);
            r.setItemId(d.getItemId());
            r.setSimulacroId(simulacroId);
            generadas.add(r);
        }

        List<Recomendacion> persistidas = recomendacionRepository.guardarTodas(generadas);
        log.info("Recomendaciones generadas y persistidas para simulacro={}: {}",
                simulacroId, persistidas.size());
        return persistidas;
    }

    @Override
    public List<Recomendacion> listarPorConcurso(UUID concursoId, UUID usuarioId) {
        if (!concursoRepository.existePorIdYUsuarioId(concursoId, usuarioId)) {
            throw new IllegalArgumentException("Concurso no encontrado para el usuario");
        }
        return recomendacionRepository.buscarUltimoPorConcurso(concursoId, usuarioId);
    }
}
