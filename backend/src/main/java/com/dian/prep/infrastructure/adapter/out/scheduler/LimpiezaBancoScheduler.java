package com.dian.prep.infrastructure.adapter.out.scheduler;

import com.dian.prep.domain.service.MantenimientoBancoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara periódicamente la limpieza del banco: elimina las preguntas RECHAZADA y
 * AGOTADA que el validador descartó, de modo que nunca lleguen a un simulacro ni
 * ocupen espacio. Es un adaptador de infraestructura; la lógica vive en
 * {@link MantenimientoBancoService#limpiarDescartadas()}.
 *
 * <p>Activo por defecto ({@code app.ai.limpieza.enabled=true}); el cron es
 * configurable con {@code app.ai.limpieza.cron}. La operación es segura e idempotente:
 * borra por estado, así que correrla de más no causa daño.</p>
 */
@Component
@ConditionalOnProperty(prefix = "app.ai.limpieza", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LimpiezaBancoScheduler {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaBancoScheduler.class);

    private final MantenimientoBancoService servicio;

    public LimpiezaBancoScheduler(MantenimientoBancoService servicio) {
        this.servicio = servicio;
    }

    @Scheduled(cron = "${app.ai.limpieza.cron:0 0 2 * * *}")
    public void ejecutar() {
        try {
            int eliminadas = servicio.limpiarDescartadas();
            log.info("Limpieza del banco ejecutada. Preguntas eliminadas: {}", eliminadas);
        } catch (Exception e) {
            log.error("La limpieza del banco terminó con error", e);
        }
    }
}
