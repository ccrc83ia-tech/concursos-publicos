package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.SimulacroEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimulacroJpaRepository extends JpaRepository<SimulacroEntity, UUID> {
}
