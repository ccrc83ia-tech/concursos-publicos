package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.FeedbackPreguntaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedbackPreguntaJpaRepository extends JpaRepository<FeedbackPreguntaEntity, UUID> {

    boolean existsByPreguntaIdAndUsuarioId(UUID preguntaId, UUID usuarioId);

    long countByPreguntaId(UUID preguntaId);
}
