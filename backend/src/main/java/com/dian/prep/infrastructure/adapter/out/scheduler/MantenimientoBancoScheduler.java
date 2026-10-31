package com.dian.prep.infrastructure.adapter.out.scheduler;

import com.dian.prep.domain.service.MantenimientoBancoService;
import com.dian.prep.infrastructure.config.IaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara periódicamente el job de mantenimiento del banco de preguntas. Es un
 * adaptador de infraestructura: solo agenda la ejecución y traduce la configuración
 * ({@link IaProperties}) a los parámetros de dominio; la lógica vive en
 * {@link MantenimientoBancoService}.
 *
 * <p>Se activa únicamente si {@code app.ai.mantenimiento.enabled=true}. El cron es
 * configurable con {@code app.ai.mantenimiento.cron}. La idempotencia por día la
 * garantiza el servicio (checkpoint en BD), de modo que reinicios o cron muy
 * frecuente no provocan trabajo duplicado.</p>
 */
@Component
@ConditionalOnProperty(prefix = "app.ai.mantenimiento", name = "enabled", havingValue = "true")
public class MantenimientoBancoScheduler {

    /** Tamaño de lote al regenerar; pequeño para no exceder tokens/min del free tier. */
    private static final int TAMANO_LOTE = 5;

    private static final Logger log = LoggerFactory.getLogger(MantenimientoBancoScheduler.class);

    private final MantenimientoBancoService servicio;
    private final IaProperties props;

    public MantenimientoBancoScheduler(MantenimientoBancoService servicio, IaProperties props) {
        this.servicio = servicio;
        this.props    = props;
    }

    @Scheduled(cron = "${app.ai.mantenimiento.cron:0 0 3 * * *}")
    public void ejecutar() {
        IaProperties.Mantenimiento m = props.getMantenimiento();
        log.info("Disparando mantenimiento del banco (modelo premium={}, objetivo/ítem={})",
                m.getModeloPremium(), m.getObjetivoValidadasPorItem());
        var params = new MantenimientoBancoService.Parametros(
                m.getModeloPremium(),
                m.getLimitePremiumDiario(),
                m.getObjetivoValidadasPorItem(),
                m.getMaxItemsPorCorrida(),
                TAMANO_LOTE);
        try {
            servicio.ejecutar(params);
        } catch (Exception e) {
            log.error("El mantenimiento del banco terminó con error", e);
        }
    }
}
