package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemJpaRepository extends JpaRepository<ItemEntity, UUID> {
    List<ItemEntity> findByConcurso_IdOrderByOrdenAsc(UUID concursoId);
}
