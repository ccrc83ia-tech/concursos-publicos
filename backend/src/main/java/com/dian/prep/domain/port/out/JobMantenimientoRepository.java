package com.dian.prep.domain.port.out;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Checkpoint de idempotencia de un job periódico. Guarda la última fecha en que el
 * job se ejecutó, para que el scheduler no repita trabajo el mismo día aunque la
 * aplicación se reinicie entre corridas.
 */
public interface JobMantenimientoRepository {

    /** Última fecha en que corrió el job identificado por {@code jobId}, si existe. */
    Optional<LocalDate> ultimaCorrida(String jobId);

    /** Registra que el job corrió en {@code fecha}, con un resumen de lo realizado. */
    void marcarCorrida(String jobId, LocalDate fecha, String resumen);
}
