package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.SimulacroPreguntaEntity;

public interface SimulacroPreguntaJpaRepository extends JpaRepository<SimulacroPreguntaEntity, UUID> {

    /**
     * IDs de preguntas que el usuario ya respondió CORRECTAMENTE en cualquiera de
     * sus simulacros. Se usa para despriorizar esas preguntas al armar un nuevo
     * simulacro (entran primero las no vistas o falladas).
     */
    @Query("""
            SELECT DISTINCT sp.preguntaId
            FROM SimulacroPreguntaEntity sp
            WHERE sp.simulacro.usuarioId = :usuarioId
              AND sp.esCorrecta = true
            """)
    List<UUID> findPreguntaIdsAcertadasPorUsuario(@Param("usuarioId") UUID usuarioId);
}
