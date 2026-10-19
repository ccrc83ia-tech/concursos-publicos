package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.ConcursoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConcursoJpaRepository extends JpaRepository<ConcursoEntity, UUID> {
    List<ConcursoEntity> findByUsuarioIdAndActivoTrue(UUID usuarioId);
    boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId);
}
