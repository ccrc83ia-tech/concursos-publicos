package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.PreguntaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PreguntaJpaRepository extends JpaRepository<PreguntaEntity, UUID> {

    List<PreguntaEntity> findByItemId(UUID itemId);

    long countByItemId(UUID itemId);

    void deleteByItemId(UUID itemId);

    @Query(value = "SELECT * FROM preguntas WHERE item_id = :itemId ORDER BY random() LIMIT :limite",
           nativeQuery = true)
    List<PreguntaEntity> findAleatoriasByItemId(@Param("itemId") UUID itemId, @Param("limite") int limite);
}
