package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.FeedbackPregunta;
import com.dian.prep.domain.port.out.FeedbackPreguntaRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.FeedbackPreguntaEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.FeedbackPreguntaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeedbackPreguntaJpaAdapter implements FeedbackPreguntaRepository {

    private final FeedbackPreguntaJpaRepository repo;

    public FeedbackPreguntaJpaAdapter(FeedbackPreguntaJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public FeedbackPregunta guardar(FeedbackPregunta feedback) {
        return toDomain(repo.save(toEntity(feedback)));
    }

    @Override
    public boolean yaReporto(UUID preguntaId, UUID usuarioId) {
        return repo.existsByPreguntaIdAndUsuarioId(preguntaId, usuarioId);
    }

    @Override
    public long contarPorPregunta(UUID preguntaId) {
        return repo.countByPreguntaId(preguntaId);
    }

    private FeedbackPreguntaEntity toEntity(FeedbackPregunta f) {
        FeedbackPreguntaEntity e = new FeedbackPreguntaEntity();
        if (f.getId() != null) e.setId(f.getId());
        e.setPreguntaId(f.getPreguntaId());
        e.setUsuarioId(f.getUsuarioId());
        e.setTipo(f.getTipo() != null ? f.getTipo().name() : FeedbackPregunta.TipoFeedback.OTRO.name());
        e.setComentario(f.getComentario());
        if (f.getCreatedAt() != null) e.setCreatedAt(f.getCreatedAt());
        return e;
    }

    private FeedbackPregunta toDomain(FeedbackPreguntaEntity e) {
        FeedbackPregunta f = new FeedbackPregunta();
        f.setId(e.getId());
        f.setPreguntaId(e.getPreguntaId());
        f.setUsuarioId(e.getUsuarioId());
        if (e.getTipo() != null) f.setTipo(FeedbackPregunta.TipoFeedback.valueOf(e.getTipo()));
        f.setComentario(e.getComentario());
        f.setCreatedAt(e.getCreatedAt());
        return f;
    }
}
