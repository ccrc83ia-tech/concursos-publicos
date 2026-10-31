package com.dian.prep.domain.port.out;

import java.time.LocalDate;

/**
 * Lleva la cuenta de cuántas peticiones se han hecho a cada modelo de IA por día.
 *
 * <p>Los límites del free tier (p. ej. ~20 solicitudes/día del modelo premium) son
 * diarios del lado del proveedor y NO se reinician al reiniciar el proceso. Por eso
 * el contador se persiste: así el job de mantenimiento sabe cuánto cupo le queda hoy
 * aunque la aplicación se haya reiniciado entre corridas.</p>
 */
public interface CupoIaRepository {

    /** Peticiones ya registradas para el modelo en la fecha dada. */
    int requestsUsadas(LocalDate fecha, String modelo);

    /**
     * Registra {@code cantidad} peticiones consumidas por el modelo en la fecha y
     * devuelve el nuevo total acumulado del día. Debe ser atómico por (fecha, modelo).
     */
    int registrarUso(LocalDate fecha, String modelo, int cantidad);

    /**
     * Indica si aún queda cupo para el modelo hoy, dado un límite diario.
     *
     * @param fecha       día a consultar
     * @param modelo      identificador del modelo
     * @param limiteDiario tope de peticiones permitidas en el día
     * @return true si {@code requestsUsadas < limiteDiario}
     */
    default boolean hayCupo(LocalDate fecha, String modelo, int limiteDiario) {
        return requestsUsadas(fecha, modelo) < limiteDiario;
    }
}
