package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.DesempenoItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DesempenoItemJpaRepository extends JpaRepository<DesempenoItemEntity, UUID> {

    List<DesempenoItemEntity> findBySimulacroId(UUID simulacroId);

    /**
     * Último desempeño por ítem de un concurso para un usuario. Toma el registro
     * más reciente (DISTINCT ON por item_id, ordenado por created_at desc).
     */
    @Query(value = """
            SELECT DISTINCT ON (d.item_id) d.*
            FROM desempeno_item d
            JOIN items i ON i.id = d.item_id
            WHERE i.concurso_id = :concursoId AND d.usuario_id = :usuarioId
            ORDER BY d.item_id, d.created_at DESC
            """, nativeQuery = true)
    List<DesempenoItemEntity> findUltimoPorConcurso(@Param("concursoId") UUID concursoId,
                                                    @Param("usuarioId") UUID usuarioId);

    /**
     * IDs de ítems cuyo desempeño MÁS RECIENTE (por usuario) no es VERDE. Toma el
     * último registro de cada par (usuario, ítem) y conserva el ítem si alguno de
     * esos últimos está en ROJO/AMARILLO.
     */
    @Query(value = """
            SELECT DISTINCT item_id FROM (
                SELECT DISTINCT ON (d.usuario_id, d.item_id) d.item_id, d.nivel_dominio
                FROM desempeno_item d
                ORDER BY d.usuario_id, d.item_id, d.created_at DESC
            ) ult
            WHERE ult.nivel_dominio <> 'VERDE'
            """, nativeQuery = true)
    List<UUID> findItemIdsConBrecha();
}
