package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.PreguntaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PreguntaJpaRepository extends JpaRepository<PreguntaEntity, UUID> {

    List<PreguntaEntity> findByItemId(UUID itemId);

    long countByItemId(UUID itemId);

    long countByItemIdAndEstadoCalidad(UUID itemId, String estadoCalidad);

    long countByItemIdAndOrigenModelo(UUID itemId, String origenModelo);

    List<PreguntaEntity> findByItemIdAndEstadoCalidad(UUID itemId, String estadoCalidad);

    Optional<PreguntaEntity> findByItemIdAndHashContenido(UUID itemId, String hashContenido);

    void deleteByItemId(UUID itemId);

    @Query(value = "SELECT * FROM preguntas WHERE item_id = :itemId ORDER BY random() LIMIT :limite",
           nativeQuery = true)
    List<PreguntaEntity> findAleatoriasByItemId(@Param("itemId") UUID itemId, @Param("limite") int limite);

    @Query(value = "SELECT * FROM preguntas WHERE item_id = :itemId AND estado_calidad = 'VALIDADA' "
                 + "ORDER BY random() LIMIT :limite",
           nativeQuery = true)
    List<PreguntaEntity> findAleatoriasValidadasByItemId(@Param("itemId") UUID itemId, @Param("limite") int limite);

    @Query(value = "SELECT * FROM preguntas WHERE item_id = :itemId AND estado_calidad = 'PENDIENTE_VALIDACION' "
                 + "ORDER BY random() LIMIT :limite",
           nativeQuery = true)
    List<PreguntaEntity> findAleatoriasPendientesByItemId(@Param("itemId") UUID itemId, @Param("limite") int limite);

    /** Borra en bloque las preguntas descartadas por el validador. Devuelve cuántas eliminó. */
    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "DELETE FROM preguntas WHERE estado_calidad IN ('RECHAZADA', 'AGOTADA')",
           nativeQuery = true)
    int deleteRechazadasYAgotadas();
}
