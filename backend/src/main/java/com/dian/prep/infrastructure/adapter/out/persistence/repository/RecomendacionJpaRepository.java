package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.RecomendacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RecomendacionJpaRepository extends JpaRepository<RecomendacionEntity, UUID> {

    List<RecomendacionEntity> findBySimulacroId(UUID simulacroId);

    /**
     * Última recomendación por ítem de un concurso para un usuario. Toma el
     * registro más reciente (DISTINCT ON por item_id, ordenado por created_at desc).
     */
    @Query(value = """
            SELECT DISTINCT ON (r.item_id) r.*
            FROM recomendaciones r
            JOIN items i ON i.id = r.item_id
            WHERE i.concurso_id = :concursoId AND r.usuario_id = :usuarioId
            ORDER BY r.item_id, r.created_at DESC
            """, nativeQuery = true)
    List<RecomendacionEntity> findUltimoPorConcurso(@Param("concursoId") UUID concursoId,
                                                    @Param("usuarioId") UUID usuarioId);
}
