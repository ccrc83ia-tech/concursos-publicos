package com.dian.prep.domain.service;

import com.dian.prep.domain.model.*;
import com.dian.prep.domain.model.Pregunta.OpcionRespuesta;
import com.dian.prep.domain.port.in.GestionarSimulacrosUseCase;
import com.dian.prep.domain.port.out.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimulacroService implements GestionarSimulacrosUseCase {

    private final ConcursoRepository concursoRepository;
    private final ItemRepository itemRepository;
    private final PreguntaRepository preguntaRepository;
    private final SimulacroRepository simulacroRepository;
    private final DesempenoRepository desempenoRepository;

    public SimulacroService(ConcursoRepository concursoRepository,
                            ItemRepository itemRepository,
                            PreguntaRepository preguntaRepository,
                            SimulacroRepository simulacroRepository,
                            DesempenoRepository desempenoRepository) {
        this.concursoRepository  = concursoRepository;
        this.itemRepository      = itemRepository;
        this.preguntaRepository  = preguntaRepository;
        this.simulacroRepository = simulacroRepository;
        this.desempenoRepository = desempenoRepository;
    }

    @Override
    public Simulacro crear(CrearSimulacroCommand cmd) {
        if (!concursoRepository.existePorIdYUsuarioId(cmd.concursoId(), cmd.usuarioId())) {
            throw new IllegalArgumentException("Concurso no encontrado para el usuario");
        }

        // Determina los ítems a incluir.
        List<Item> items = itemRepository.buscarPorConcursoId(cmd.concursoId());
        if (cmd.itemIds() != null && !cmd.itemIds().isEmpty()) {
            Set<UUID> filtro = new HashSet<>(cmd.itemIds());
            items = items.stream().filter(i -> filtro.contains(i.getId())).toList();
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("No hay ítems seleccionados para el simulacro");
        }

        int porItem = Math.max(1, cmd.preguntasPorItem());
        Simulacro simulacro = Simulacro.crear(
                cmd.usuarioId(), cmd.concursoId(), cmd.tipo(), cmd.tiempoLimiteSegundos());

        int orden = 0;
        for (Item item : items) {
            List<Pregunta> preguntas = preguntaRepository.buscarAleatoriasPorItemId(item.getId(), porItem);
            for (Pregunta p : preguntas) {
                simulacro.getPreguntas().add(
                        SimulacroPregunta.crear(p.getId(), item.getId(), orden++));
            }
        }

        if (simulacro.getPreguntas().isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay preguntas generadas para estos ítems. Genere preguntas antes de crear un simulacro.");
        }

        return simulacroRepository.guardar(simulacro);
    }

    @Override
    public Simulacro obtener(UUID simulacroId, UUID usuarioId) {
        Simulacro s = cargarYValidar(simulacroId, usuarioId);
        return s;
    }

    @Override
    public void responder(UUID simulacroId, UUID usuarioId, RespuestaCommand cmd) {
        Simulacro s = cargarYValidar(simulacroId, usuarioId);
        if (s.getEstado() != Simulacro.EstadoSimulacro.EN_CURSO) {
            throw new IllegalArgumentException("El simulacro ya fue finalizado");
        }

        SimulacroPregunta sp = simulacroRepository.buscarPreguntaPorId(cmd.simulacroPreguntaId())
                .orElseThrow(() -> new IllegalArgumentException("Pregunta de simulacro no encontrada"));
        if (!sp.getSimulacroId().equals(simulacroId)) {
            throw new IllegalArgumentException("La pregunta no pertenece a este simulacro");
        }

        OpcionRespuesta respuesta = parseOpcion(cmd.respuesta());
        sp.setRespuestaDada(respuesta);
        sp.setMarcadaParaRevision(cmd.marcadaParaRevision());
        sp.setTiempoRespuestaSegundos(cmd.tiempoRespuestaSegundos());

        // Califica contra la respuesta correcta real de la pregunta.
        if (respuesta != null) {
            OpcionRespuesta correcta = preguntaRepository.buscarPorId(sp.getPreguntaId())
                    .map(Pregunta::getRespuestaCorrecta)
                    .orElse(null);
            sp.setEsCorrecta(correcta != null && correcta == respuesta);
        } else {
            sp.setEsCorrecta(false);
        }

        simulacroRepository.guardarPregunta(sp);
    }

    @Override
    public Simulacro finalizar(UUID simulacroId, UUID usuarioId, int tiempoUsadoSegundos) {
        Simulacro s = cargarYValidar(simulacroId, usuarioId);
        if (s.getEstado() == Simulacro.EstadoSimulacro.COMPLETADO) {
            return s; // idempotente
        }

        s.finalizar(tiempoUsadoSegundos);
        Simulacro guardado = simulacroRepository.guardar(s);

        // Calcula desempeño por ítem usando el umbral configurado de cada ítem.
        Map<UUID, Double> umbralPorItem = itemRepository.buscarPorConcursoId(s.getConcursoId())
                .stream().collect(Collectors.toMap(Item::getId,
                        i -> i.getUmbralBrecha() != null ? i.getUmbralBrecha() : 70.0));

        Map<UUID, List<SimulacroPregunta>> porItem = guardado.getPreguntas().stream()
                .collect(Collectors.groupingBy(SimulacroPregunta::getItemId));

        List<DesempenoItem> desempenos = new ArrayList<>();
        for (var entrada : porItem.entrySet()) {
            UUID itemId = entrada.getKey();
            List<SimulacroPregunta> preguntas = entrada.getValue();
            int total = preguntas.size();
            int correctas = (int) preguntas.stream().filter(SimulacroPregunta::esCorrecta).count();
            double umbral = umbralPorItem.getOrDefault(itemId, 70.0);
            desempenos.add(DesempenoItem.calcular(
                    usuarioId, itemId, guardado.getId(), correctas, total, umbral));
        }
        desempenoRepository.guardarTodos(desempenos);

        return guardado;
    }

    @Override
    public List<Pregunta> contenidoPreguntas(UUID simulacroId, UUID usuarioId) {
        Simulacro s = cargarYValidar(simulacroId, usuarioId);
        return s.getPreguntas().stream()
                .sorted(Comparator.comparingInt(SimulacroPregunta::getOrden))
                .map(sp -> preguntaRepository.buscarPorId(sp.getPreguntaId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<DesempenoItem> desempeno(UUID simulacroId, UUID usuarioId) {
        cargarYValidar(simulacroId, usuarioId);
        return desempenoRepository.buscarPorSimulacroId(simulacroId);
    }

    @Override
    public List<DesempenoItem> brechasPorConcurso(UUID concursoId, UUID usuarioId) {
        if (!concursoRepository.existePorIdYUsuarioId(concursoId, usuarioId)) {
            throw new IllegalArgumentException("Concurso no encontrado para el usuario");
        }
        return desempenoRepository.buscarUltimoPorConcurso(concursoId, usuarioId);
    }

    // --- Helpers ---

    private Simulacro cargarYValidar(UUID simulacroId, UUID usuarioId) {
        Simulacro s = simulacroRepository.buscarPorId(simulacroId)
                .orElseThrow(() -> new IllegalArgumentException("Simulacro no encontrado: " + simulacroId));
        if (!s.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("El simulacro no pertenece al usuario");
        }
        return s;
    }

    private OpcionRespuesta parseOpcion(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return OpcionRespuesta.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
