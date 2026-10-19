package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.Recomendacion;
import com.dian.prep.domain.model.Recomendacion.ReferenciaNormativa;
import com.dian.prep.domain.model.Recomendacion.TemaFallido;
import com.dian.prep.domain.port.out.RecomendacionRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.RecomendacionEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.RecomendacionJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RecomendacionJpaAdapter implements RecomendacionRepository {

    private static final TypeReference<List<TemaFallido>> TEMAS_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<ReferenciaNormativa>> REFS_TYPE = new TypeReference<>() {};

    private final RecomendacionJpaRepository repo;
    private final ObjectMapper objectMapper;

    public RecomendacionJpaAdapter(RecomendacionJpaRepository repo, ObjectMapper objectMapper) {
        this.repo         = repo;
        this.objectMapper = objectMapper;
    }

    @Override
    public Recomendacion guardar(Recomendacion recomendacion) {
        return toDomain(repo.save(toEntity(recomendacion)));
    }

    @Override
    public List<Recomendacion> guardarTodas(List<Recomendacion> recomendaciones) {
        List<RecomendacionEntity> entities = recomendaciones.stream().map(this::toEntity).toList();
        return repo.saveAll(entities).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Recomendacion> buscarPorSimulacroId(UUID simulacroId) {
        return repo.findBySimulacroId(simulacroId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Recomendacion> buscarUltimoPorConcurso(UUID concursoId, UUID usuarioId) {
        return repo.findUltimoPorConcurso(concursoId, usuarioId).stream().map(this::toDomain).toList();
    }

    // --- Mappers ---

    private RecomendacionEntity toEntity(Recomendacion r) {
        RecomendacionEntity e = new RecomendacionEntity();
        if (r.getId() != null) e.setId(r.getId());
        e.setUsuarioId(r.getUsuarioId());
        e.setItemId(r.getItemId());
        e.setSimulacroId(r.getSimulacroId());
        e.setTemasFallidos(serializar(r.getTemasFallidos()));
        e.setReferenciasNormativas(serializar(r.getReferenciasNormativas()));
        e.setResumenEjecutivo(r.getResumenEjecutivo());
        e.setTiempoEstudioHoras(r.getTiempoEstudioHoras());
        if (r.getCreatedAt() != null) e.setCreatedAt(r.getCreatedAt());
        return e;
    }

    private Recomendacion toDomain(RecomendacionEntity e) {
        Recomendacion r = new Recomendacion();
        r.setId(e.getId());
        r.setUsuarioId(e.getUsuarioId());
        r.setItemId(e.getItemId());
        r.setSimulacroId(e.getSimulacroId());
        r.setTemasFallidos(deserializar(e.getTemasFallidos(), TEMAS_TYPE));
        r.setReferenciasNormativas(deserializar(e.getReferenciasNormativas(), REFS_TYPE));
        r.setResumenEjecutivo(e.getResumenEjecutivo());
        r.setTiempoEstudioHoras(e.getTiempoEstudioHoras());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }

    private String serializar(Object valor) {
        if (valor == null) return null;
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (Exception ex) {
            return null;
        }
    }

    private <T> List<T> deserializar(String json, TypeReference<List<T>> tipo) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, tipo);
        } catch (Exception ex) {
            return List.of();
        }
    }
}
