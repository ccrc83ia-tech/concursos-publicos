package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.CupoIaDiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CupoIaDiarioJpaRepository extends JpaRepository<CupoIaDiarioEntity, UUID> {

    Optional<CupoIaDiarioEntity> findByFechaAndModelo(LocalDate fecha, String modelo);

    /**
     * Upsert atómico del contador diario. Inserta la fila (fecha, modelo) o, si ya
     * existe, suma {@code cantidad} a requests_usadas. Resuelve la concurrencia en la
     * base de datos vía el índice único (fecha, modelo).
     */
    @Modifying
    @Query(value = """
            INSERT INTO cupo_ia_diario (id, fecha, modelo, requests_usadas, updated_at)
            VALUES (gen_random_uuid(), :fecha, :modelo, :cantidad, NOW())
            ON CONFLICT (fecha, modelo)
            DO UPDATE SET requests_usadas = cupo_ia_diario.requests_usadas + EXCLUDED.requests_usadas,
                          updated_at = NOW()
            """, nativeQuery = true)
    void upsertSuma(@Param("fecha") LocalDate fecha,
                    @Param("modelo") String modelo,
                    @Param("cantidad") int cantidad);
}
