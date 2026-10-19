package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.Concurso;
import com.dian.prep.domain.port.out.ConcursoRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.ConcursoEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.ConcursoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConcursoJpaAdapter implements ConcursoRepository {

    private final ConcursoJpaRepository jpaRepository;

    public ConcursoJpaAdapter(ConcursoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Concurso guardar(Concurso concurso) {
        return toDomain(jpaRepository.save(toEntity(concurso)));
    }

    @Override
    public Optional<Concurso> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Concurso> buscarPorUsuarioId(UUID usuarioId) {
        return jpaRepository.findByUsuarioIdAndActivoTrue(usuarioId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existePorIdYUsuarioId(UUID id, UUID usuarioId) {
        return jpaRepository.existsByIdAndUsuarioId(id, usuarioId);
    }

    private ConcursoEntity toEntity(Concurso c) {
        ConcursoEntity e = new ConcursoEntity();
        if (c.getId() != null) e.setId(c.getId());
        e.setUsuarioId(c.getUsuarioId());
        e.setNombre(c.getNombre());
        e.setProcesoSeleccion(c.getProcesoSeleccion());
        e.setNumeroInscripcion(c.getNumeroInscripcion());
        e.setNumeroEmpleo(c.getNumeroEmpleo());
        e.setNivelCargo(c.getNivelCargo());
        e.setDenominacion(c.getDenominacion());
        e.setFechaExamen(c.getFechaExamen());
        e.setActivo(c.isActivo());
        if (c.getCreatedAt() != null) e.setCreatedAt(c.getCreatedAt());
        return e;
    }

    private Concurso toDomain(ConcursoEntity e) {
        Concurso c = new Concurso();
        c.setId(e.getId());
        c.setUsuarioId(e.getUsuarioId());
        c.setNombre(e.getNombre());
        c.setProcesoSeleccion(e.getProcesoSeleccion());
        c.setNumeroInscripcion(e.getNumeroInscripcion());
        c.setNumeroEmpleo(e.getNumeroEmpleo());
        c.setNivelCargo(e.getNivelCargo());
        c.setDenominacion(e.getDenominacion());
        c.setFechaExamen(e.getFechaExamen());
        c.setActivo(e.isActivo());
        c.setCreatedAt(e.getCreatedAt());
        return c;
    }
}
