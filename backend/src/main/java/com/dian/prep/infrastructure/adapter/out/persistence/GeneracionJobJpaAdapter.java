package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.GeneracionJob;
import com.dian.prep.domain.port.out.GeneracionJobRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.GeneracionJobEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.GeneracionJobJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GeneracionJobJpaAdapter implements GeneracionJobRepository {

    private final GeneracionJobJpaRepository repo;

    public GeneracionJobJpaAdapter(GeneracionJobJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public GeneracionJob guardar(GeneracionJob job) {
        return toDomain(repo.save(toEntity(job)));
    }

    @Override
    public Optional<GeneracionJob> buscarPorId(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<GeneracionJob> buscarUltimoPorItemId(UUID itemId) {
        return repo.findFirstByItemIdOrderByCreatedAtDesc(itemId).map(this::toDomain);
    }

    // --- Mappers ---

    private GeneracionJobEntity toEntity(GeneracionJob j) {
        GeneracionJobEntity e = new GeneracionJobEntity();
        if (j.getId() != null) e.setId(j.getId());
        e.setItemId(j.getItemId());
        e.setEstado(j.getEstado().name());
        e.setPreguntasGeneradas(j.getPreguntasGeneradas());
        e.setTotalEsperadas(j.getTotalEsperadas());
        e.setMensajeError(j.getMensajeError());
        if (j.getCreatedAt() != null) e.setCreatedAt(j.getCreatedAt());
        return e;
    }

    private GeneracionJob toDomain(GeneracionJobEntity e) {
        GeneracionJob j = new GeneracionJob();
        j.setId(e.getId());
        j.setItemId(e.getItemId());
        j.setEstado(GeneracionJob.EstadoGeneracion.valueOf(e.getEstado()));
        j.setPreguntasGeneradas(e.getPreguntasGeneradas());
        j.setTotalEsperadas(e.getTotalEsperadas());
        j.setMensajeError(e.getMensajeError());
        j.setCreatedAt(e.getCreatedAt());
        j.setUpdatedAt(e.getUpdatedAt());
        return j;
    }
}
