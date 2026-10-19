package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.SimulacroPreguntaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimulacroPreguntaJpaRepository extends JpaRepository<SimulacroPreguntaEntity, UUID> {
}
