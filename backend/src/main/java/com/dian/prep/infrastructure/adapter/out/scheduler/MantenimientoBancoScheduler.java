package com.dian.prep.infrastructure.adapter.out.scheduler;

import com.dian.prep.domain.service.MantenimientoBancoService;
import com.dian.prep.infrastructure.config.IaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara el worker de mantenimiento del banco de preguntas de forma periódica. Es un
 * adaptador de infraestructura: solo agenda la ejecución y traduce la configuración
 * ({@link IaProperties}) a los parámetros de dominio; la lógica vive en
 * {@link MantenimientoBancoService}.
 *
 * <p>Se activa solo si {@code app.ai.mantenimiento.enabled=true}. En lugar de correr
 * una vez al día, se dispara con un intervalo fijo corto (por defecto 15 min) y
 * trabaja mientras haya cupo premium y queden ítems por completar; así "persigue" el
 * objetivo del banco y, al agotarse el cupo del día, espera y retoma en la siguiente
 * corrida tras la renovación. Usa {@code fixedDelay} para que una corrida no empiece
 * hasta que termine la anterior (no se solapan). El intervalo es configurable con
 * {@code app.ai.mantenimiento.intervalo-ms}.</p>
 */
@Component
@ConditionalOnProperty(prefix = "app.ai.mantenimiento", name = "enabled", havingValue = "true")
public class MantenimientoBancoScheduler {

    /** Enunciados por lote al regenerar; pequeño para no exceder tokens/min del free tier. */
    private static final int TAMANO_LOTE = 2;

    private static final Logger log = LoggerFactory.getLogger(MantenimientoBancoScheduler.class);

    private final MantenimientoBancoService servicio;
    private final IaProperties props;

    public MantenimientoBancoScheduler(MantenimientoBancoService servicio, IaProperties props) {
        this.servicio = servicio;
        this.props    = props;
    }

    @Scheduled(
            fixedDelayString = "${app.ai.mantenimiento.intervalo-ms:900000}",
            initialDelayString = "${app.ai.mantenimiento.initial-delay-ms:60000}")
    public void ejecutar() {
        IaProperties.Mantenimiento m = props.getMantenimiento();
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
