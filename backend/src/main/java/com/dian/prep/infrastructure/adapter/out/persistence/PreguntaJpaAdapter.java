package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.PreguntaRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.PreguntaEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.PreguntaJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class PreguntaJpaAdapter implements PreguntaRepository {

    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

    private final PreguntaJpaRepository repo;
    private final ObjectMapper objectMapper;

    public PreguntaJpaAdapter(PreguntaJpaRepository repo, ObjectMapper objectMapper) {
        this.repo         = repo;
        this.objectMapper = objectMapper;
    }

    @Override
    public Pregunta guardar(Pregunta pregunta) {
        return toDomain(repo.save(toEntity(pregunta)));
    }

    /**
     * Persiste el lote deduplicando por {@code hash_contenido}: descarta las que ya
     * existen en el ítem y las colisiones dentro del mismo lote. Así el índice único
     * (item_id, hash_contenido) nunca llega a dispararse y el usuario no ve repetidos.
     */
    @Override
    public List<Pregunta> guardarTodas(List<Pregunta> preguntas) {
        if (preguntas == null || preguntas.isEmpty()) return List.of();

        List<PreguntaEntity> aInsertar = new ArrayList<>();
        Set<String> hashesLote = new HashSet<>();
        for (Pregunta p : preguntas) {
            String hash = p.getHashContenido() != null ? p.getHashContenido() : p.calcularHash();
            // Colisión dentro del lote.
            if (!hashesLote.add(hash)) continue;
            // Ya existe en el ítem.
            if (p.getItemId() != null && repo.findByItemIdAndHashContenido(p.getItemId(), hash).isPresent()) {
                continue;
            }
            aInsertar.add(toEntity(p));
        }
        if (aInsertar.isEmpty()) return List.of();
        return repo.saveAll(aInsertar).stream().map(this::toDomain).toList();
    }

    @Override
    public java.util.Optional<Pregunta> buscarPorId(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Pregunta> buscarPorItemId(UUID itemId) {
        return repo.findByItemId(itemId).stream().map(this::toDomain).toList();
    }

    @Override
    public long contarPorItemId(UUID itemId) {
        return repo.countByItemId(itemId);
    }

    @Override
    public long contarValidadasPorItemId(UUID itemId) {
        return repo.countByItemIdAndEstadoCalidad(itemId, Pregunta.EstadoCalidad.VALIDADA.name());
    }

    @Override
    public long contarPorEstado(UUID itemId, Pregunta.EstadoCalidad estado) {
        return repo.countByItemIdAndEstadoCalidad(itemId, estado.name());
    }

    @Override
    public long contarPorOrigen(UUID itemId, Pregunta.OrigenModelo origen) {
        return repo.countByItemIdAndOrigenModelo(itemId, origen.name());
    }

    @Override
    public List<Pregunta> buscarAleatoriasPorItemId(UUID itemId, int limite) {
        return repo.findAleatoriasByItemId(itemId, limite).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Pregunta> buscarAleatoriasValidadasPorItemId(UUID itemId, int limite) {
        return repo.findAleatoriasValidadasByItemId(itemId, limite).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Pregunta> buscarAleatoriasPendientesPorItemId(UUID itemId, int limite) {
        return repo.findAleatoriasPendientesByItemId(itemId, limite).stream().map(this::toDomain).toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public int eliminarRechazadasYAgotadas() {
        return repo.deleteRechazadasYAgotadas();
    }

    @Override
    public List<Pregunta> buscarPendientesPorItemId(UUID itemId) {
        return repo.findByItemIdAndEstadoCalidad(itemId, Pregunta.EstadoCalidad.PENDIENTE_VALIDACION.name())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public java.util.Optional<Pregunta> buscarPorHash(UUID itemId, String hashContenido) {
        return repo.findByItemIdAndHashContenido(itemId, hashContenido).map(this::toDomain);
    }

    @Override
    public void eliminarPorItemId(UUID itemId) {
        repo.deleteByItemId(itemId);
    }

    // --- Mappers ---

    private PreguntaEntity toEntity(Pregunta p) {
        PreguntaEntity e = new PreguntaEntity();
        if (p.getId() != null) e.setId(p.getId());
        e.setItemId(p.getItemId());
        e.setEnunciado(p.getEnunciado());
        e.setOpcionA(p.getOpcionA());
        e.setOpcionB(p.getOpcionB());
        e.setOpcionC(p.getOpcionC());
        e.setOpcionD(p.getOpcionD());
        e.setRespuestaCorrecta(p.getRespuestaCorrecta() != null ? p.getRespuestaCorrecta().name() : null);
        e.setJustificacionCorrecta(p.getJustificacionCorrecta());
        e.setJustificacionIncorrectas(serializar(p.getJustificacionIncorrectas()));
        e.setReferenciaNormativa(p.getReferenciaNormativa());
        e.setNivelDificultad(p.getNivelDificultad() != null
                ? p.getNivelDificultad().name()
                : Pregunta.NivelDificultad.INTERMEDIO.name());
        e.setProcesoCognitivo(p.getProcesoCognitivo() != null
                ? p.getProcesoCognitivo().name()
                : Pregunta.ProcesoCognitivo.APLICACION.name());
        e.setGeneradaPorIa(p.isGeneradaPorIa());
        if (p.getCreatedAt() != null) e.setCreatedAt(p.getCreatedAt());

        // Calidad del banco. El hash se calcula aquí si aún no se fijó, para que
        // siempre viaje a la BD (lo usa el índice único de deduplicación).
        e.setEstadoCalidad(p.getEstadoCalidad() != null
                ? p.getEstadoCalidad().name()
                : Pregunta.EstadoCalidad.PENDIENTE_VALIDACION.name());
        e.setOrigenModelo(p.getOrigenModelo() != null
                ? p.getOrigenModelo().name()
                : Pregunta.OrigenModelo.PROVISIONAL.name());
        e.setHashContenido(p.getHashContenido() != null ? p.getHashContenido() : p.calcularHash());
        e.setIntentosGeneracion(p.getIntentosGeneracion());
        e.setVersionGeneracion(p.getVersionGeneracion());
        e.setValidadaAt(p.getValidadaAt());
        return e;
    }

    private Pregunta toDomain(PreguntaEntity e) {
        Pregunta p = new Pregunta();
        p.setId(e.getId());
        p.setItemId(e.getItemId());
        p.setEnunciado(e.getEnunciado());
        p.setOpcionA(e.getOpcionA());
        p.setOpcionB(e.getOpcionB());
        p.setOpcionC(e.getOpcionC());
        p.setOpcionD(e.getOpcionD());
        if (e.getRespuestaCorrecta() != null) {
            p.setRespuestaCorrecta(Pregunta.OpcionRespuesta.valueOf(e.getRespuestaCorrecta()));
        }
        p.setJustificacionCorrecta(e.getJustificacionCorrecta());
        p.setJustificacionIncorrectas(deserializar(e.getJustificacionIncorrectas()));
        p.setReferenciaNormativa(e.getReferenciaNormativa());
        if (e.getNivelDificultad() != null) {
            p.setNivelDificultad(Pregunta.NivelDificultad.valueOf(e.getNivelDificultad()));
        }
        if (e.getProcesoCognitivo() != null) {
            p.setProcesoCognitivo(Pregunta.ProcesoCognitivo.valueOf(e.getProcesoCognitivo()));
        }
        p.setGeneradaPorIa(e.isGeneradaPorIa());
        p.setCreatedAt(e.getCreatedAt());

        if (e.getEstadoCalidad() != null) {
            p.setEstadoCalidad(Pregunta.EstadoCalidad.valueOf(e.getEstadoCalidad()));
        }
        if (e.getOrigenModelo() != null) {
            p.setOrigenModelo(Pregunta.OrigenModelo.valueOf(e.getOrigenModelo()));
        }
        p.setHashContenido(e.getHashContenido());
        p.setIntentosGeneracion(e.getIntentosGeneracion());
        p.setVersionGeneracion(e.getVersionGeneracion());
        p.setValidadaAt(e.getValidadaAt());
        return p;
    }

    private String serializar(Map<String, String> mapa) {
        if (mapa == null || mapa.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(mapa);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, String> deserializar(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
