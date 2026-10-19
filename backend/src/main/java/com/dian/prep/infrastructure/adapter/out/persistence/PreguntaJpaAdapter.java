package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.Pregunta;
import com.dian.prep.domain.port.out.PreguntaRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.PreguntaEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.PreguntaJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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

    @Override
    public List<Pregunta> guardarTodas(List<Pregunta> preguntas) {
        List<PreguntaEntity> entities = preguntas.stream().map(this::toEntity).toList();
        return repo.saveAll(entities).stream().map(this::toDomain).toList();
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
    public List<Pregunta> buscarAleatoriasPorItemId(UUID itemId, int limite) {
        return repo.findAleatoriasByItemId(itemId, limite).stream().map(this::toDomain).toList();
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
        e.setGeneradaPorIa(p.isGeneradaPorIa());
        if (p.getCreatedAt() != null) e.setCreatedAt(p.getCreatedAt());
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
        p.setGeneradaPorIa(e.isGeneradaPorIa());
        p.setCreatedAt(e.getCreatedAt());
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
