-- V7: Banco de preguntas de calidad con doble IA.
-- Añade a cada pregunta su estado de calidad, el origen del modelo que la produjo,
-- un hash de contenido para deduplicar, el contador de intentos de validación y la
-- versión de generación. Crea además las tablas de control del job de mantenimiento
-- (cupo diario por modelo e idempotencia del scheduler).
--
-- Coherente con V5: todos los "enum" se guardan como VARCHAR (no se crean tipos enum nativos).
--
-- IDEMPOTENTE: usa IF NOT EXISTS en columnas, tablas e índices. Así la migración se
-- aplica igual en una BD nueva (construida por Flyway) que en una donde el esquema ya
-- existía (p. ej. creado por Hibernate ddl-auto), sin fallar por objetos duplicados.

-- ---------------------------------------------------------------------------
-- 1) Columnas de calidad en la tabla preguntas
-- ---------------------------------------------------------------------------
ALTER TABLE preguntas
    ADD COLUMN IF NOT EXISTS estado_calidad       VARCHAR(25)  NOT NULL DEFAULT 'PENDIENTE_VALIDACION',
    ADD COLUMN IF NOT EXISTS origen_modelo        VARCHAR(15)  NOT NULL DEFAULT 'PROVISIONAL',
    ADD COLUMN IF NOT EXISTS hash_contenido       VARCHAR(64),
    ADD COLUMN IF NOT EXISTS intentos_generacion  INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS version_generacion   INTEGER      NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS validada_at          TIMESTAMP;

COMMENT ON COLUMN preguntas.estado_calidad IS
    'PENDIENTE_VALIDACION | VALIDADA | RECHAZADA | AGOTADA';
COMMENT ON COLUMN preguntas.origen_modelo IS
    'PREMIUM (Gemini 3.x) | PROVISIONAL (Groq), modelo que redactó la pregunta';
COMMENT ON COLUMN preguntas.hash_contenido IS
    'SHA-256 del texto normalizado (enunciado + 3 opciones ordenadas) para deduplicar';
COMMENT ON COLUMN preguntas.intentos_generacion IS
    'Veces que el hueco/pregunta fue sometido a validación; a los 3 rechazos pasa a AGOTADA';
COMMENT ON COLUMN preguntas.version_generacion IS
    'Versión del perfil/prompt con el que se generó (trazabilidad de mejoras)';

-- Backfill: las preguntas ya existentes se marcan como VALIDADAS (ya se usaban para
-- estudiar). Solo afecta a las que quedaron en el estado por defecto, de modo que
-- re-ejecutar la migración (o aplicarla sobre datos ya migrados) no pisa estados reales.
UPDATE preguntas
    SET estado_calidad = 'VALIDADA', validada_at = created_at
    WHERE estado_calidad = 'PENDIENTE_VALIDACION';

-- Índice único para deduplicación por ítem. Es parcial: solo aplica cuando hay hash,
-- de modo que las filas antiguas sin hash no choquen entre sí.
CREATE UNIQUE INDEX IF NOT EXISTS idx_preguntas_item_hash
    ON preguntas(item_id, hash_contenido)
    WHERE hash_contenido IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_preguntas_estado_calidad ON preguntas(item_id, estado_calidad);

-- ---------------------------------------------------------------------------
-- 2) Cupo diario de IA por modelo (control de free tier, persistente entre reinicios)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cupo_ia_diario (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha             DATE        NOT NULL,
    modelo            VARCHAR(80) NOT NULL,
    requests_usadas   INTEGER     NOT NULL DEFAULT 0,
    updated_at        TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_cupo_ia_fecha_modelo ON cupo_ia_diario(fecha, modelo);

COMMENT ON TABLE cupo_ia_diario IS
    'Contador de requests a cada modelo de IA por día. El cupo del proveedor no se '
    'reinicia al reiniciar el proceso, por eso se persiste.';

-- ---------------------------------------------------------------------------
-- 3) Checkpoint de idempotencia del job de mantenimiento del banco
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS job_mantenimiento (
    id              VARCHAR(50) PRIMARY KEY,   -- clave lógica del job, p.ej. 'banco-preguntas'
    ultima_corrida  DATE,
    resumen         TEXT,
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE job_mantenimiento IS
    'Última fecha de corrida de cada job periódico, para que el scheduler sea idempotente '
    '(no repite trabajo el mismo día aunque el proceso se reinicie).';
