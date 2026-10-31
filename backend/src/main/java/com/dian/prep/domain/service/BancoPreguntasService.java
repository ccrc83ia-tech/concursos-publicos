package com.dian.prep.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.PreguntaRepository;

/**
 * Sirve preguntas del banco aplicando la "regla de oro" de calidad: se entregan
 * preferentemente las VALIDADAS (revisadas por el segundo modelo) y, solo si no
 * alcanzan, se completa con las PENDIENTES de validación. Las preguntas que el
 * validador descartó (RECHAZADA / AGOTADA) NUNCA se sirven en un simulacro; un job
 * de limpieza las elimina del banco aparte. Evita duplicados dentro de la tanda.
 *
 * <p>Pertenece al dominio: coordina lecturas del {@link PreguntaRepository} sin
 * conocer detalles de persistencia ni de IA.</p>
 */
@Service
public class BancoPreguntasService {

    private final PreguntaRepository preguntaRepository;

    public BancoPreguntasService(PreguntaRepository preguntaRepository) {
        this.preguntaRepository = preguntaRepository;
    }

    /**
     * Devuelve preguntas del ítem para un simulacro, AGRUPADAS POR ENUNCIADO: cada
     * enunciado (texto de lectura) se incluye con todas sus preguntas consecutivas,
     * de modo que el aspirante lea el texto una vez y responda las 3–4 preguntas que
     * se desprenden de él. Se toman enunciados completos hasta cubrir {@code cantidad}
     * preguntas (puede excederla un poco al cerrar el último enunciado, para no partir
     * un grupo). Prioriza validadas; completa con pendientes; nunca sirve
     * rechazadas ni agotadas.
     *
     * @param itemId   ítem del que extraer preguntas
     * @param cantidad número objetivo de preguntas (se respeta el agrupamiento por enunciado)
     * @return preguntas ordenadas por enunciado (las de un mismo enunciado, juntas)
     */
    public List<Pregunta> servirParaSimulacro(UUID itemId, int cantidad) {
        return servirParaSimulacro(itemId, cantidad, Set.of());
    }

    /**
     * Igual que {@link #servirParaSimulacro(UUID, int)} pero DESPRIORIZANDO las
     * preguntas que el usuario ya respondió correctamente ({@code acertadas}): los
     * enunciados cuyas preguntas aún no se han acertado entran primero, y los ya
     * dominados solo se usan si hacen falta para completar el cupo. El orden interno
     * de cada enunciado (sus 3–4 preguntas juntas) se conserva.
     *
     * @param acertadas IDs de preguntas ya acertadas por el usuario (puede ir vacío)
     */
    public List<Pregunta> servirParaSimulacro(UUID itemId, int cantidad, Set<UUID> acertadas) {
        if (cantidad <= 0) return List.of();

        // Se pide un conjunto amplio de candidatas (validadas primero, luego pendientes)
        // y se agrupan por enunciado. Pedimos más del cupo para tener enunciados completos.
        int holgura = cantidad * 4;
        List<Pregunta> candidatas = new ArrayList<>();
        Set<UUID> vistas = new HashSet<>();
        for (Pregunta p : preguntaRepository.buscarAleatoriasValidadasPorItemId(itemId, holgura)) {
            if (vistas.add(p.getId())) candidatas.add(p);
        }
        for (Pregunta p : preguntaRepository.buscarAleatoriasPendientesPorItemId(itemId, holgura)) {
            if (vistas.add(p.getId())) candidatas.add(p);
        }
        if (candidatas.isEmpty()) return List.of();

        // Agrupar por enunciado (el texto de lectura antes del marcador "PREGUNTA").
        Map<String, List<Pregunta>> porEnunciado = new LinkedHashMap<>();
        for (Pregunta p : candidatas) {
            porEnunciado.computeIfAbsent(claveEnunciado(p.getEnunciado()), k -> new ArrayList<>()).add(p);
        }

        // Despriorización: ordena los enunciados por cuántas de sus preguntas ya fueron
        // acertadas (ascendente). Así, los casos "frescos" (0 acertadas) van primero y
        // los ya dominados quedan al final, usándose solo si falta cupo. Es un orden
        // estable, de modo que dentro del mismo nivel de acierto se respeta el azar inicial.
        Set<UUID> ya = (acertadas == null) ? Set.of() : acertadas;
        List<List<Pregunta>> grupos = new ArrayList<>(porEnunciado.values());
        grupos.sort(Comparator.comparingInt(g ->
                (int) g.stream().filter(p -> ya.contains(p.getId())).count()));

        // Tomar enunciados completos hasta alcanzar el cupo de preguntas; no se parte
        // un enunciado por la mitad.
        List<Pregunta> seleccion = new ArrayList<>();
        for (List<Pregunta> grupo : grupos) {
            if (seleccion.size() >= cantidad) break;
            seleccion.addAll(grupo);
        }
        return seleccion;
    }

    /** Clave de agrupación: el texto del enunciado (lectura), sin la parte de la pregunta. */
    private String claveEnunciado(String enunciado) {
        if (enunciado == null) return "";
        int i = enunciado.indexOf("PREGUNTA");
        return (i >= 0 ? enunciado.substring(0, i) : enunciado).trim();
    }

    /** Cuántas preguntas VALIDADAS tiene el ítem (para decidir si hace falta generar más). */
    public long contarValidadas(UUID itemId) {
        return preguntaRepository.contarValidadasPorItemId(itemId);
    }

    /** Foto del banco de un ítem: conteos por estado de calidad y por origen de modelo. */
    public BancoEstado estadoBanco(UUID itemId) {
        return new BancoEstado(
                preguntaRepository.contarPorItemId(itemId),
                preguntaRepository.contarPorEstado(itemId, Pregunta.EstadoCalidad.VALIDADA),
                preguntaRepository.contarPorEstado(itemId, Pregunta.EstadoCalidad.PENDIENTE_VALIDACION),
                preguntaRepository.contarPorEstado(itemId, Pregunta.EstadoCalidad.RECHAZADA),
                preguntaRepository.contarPorEstado(itemId, Pregunta.EstadoCalidad.AGOTADA),
                preguntaRepository.contarPorOrigen(itemId, Pregunta.OrigenModelo.PROVISIONAL),
                preguntaRepository.contarPorOrigen(itemId, Pregunta.OrigenModelo.PREMIUM));
    }

    /** Resumen de observabilidad del banco de un ítem. */
    public record BancoEstado(
            long total,
            long validadas,
            long pendientes,
            long rechazadas,
            long agotadas,
            long provisionales,
            long premium) {}
}
