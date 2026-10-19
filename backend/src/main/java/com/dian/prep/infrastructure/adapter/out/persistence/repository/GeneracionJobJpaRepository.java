package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.GeneracionJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GeneracionJobJpaRepository extends JpaRepository<GeneracionJobEntity, UUID> {

    Optional<GeneracionJobEntity> findFirstByItemIdOrderByCreatedAtDesc(UUID itemId);
}
