-- V8: Feedback del usuario sobre preguntas.
-- Permite que un aspirante reporte una pregunta como problemática (clave incorrecta,
-- enunciado confuso, varias correctas, fuera de tema, error de redacción, otro).
-- Al acumular varios reportes de usuarios distintos, el servicio degrada la pregunta a
-- RECHAZADA, de modo que deja de servirse y el job de limpieza la elimina.
--
-- Idempotente (estilo V7): IF NOT EXISTS en tabla e índices.

CREATE TABLE IF NOT EXISTS feedback_preguntas (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    pregunta_id UUID        NOT NULL REFERENCES preguntas(id) ON DELETE CASCADE,
    usuario_id  UUID        NOT NULL REFERENCES usuarios(id)  ON DELETE CASCADE,
    tipo        VARCHAR(30) NOT NULL,   -- CLAVE_INCORRECTA | ENUNCIADO_CONFUSO | VARIAS_CORRECTAS | FUERA_DE_TEMA | ERROR_REDACCION | OTRO
    comentario  TEXT,                   -- observación opcional del aspirante
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Un usuario solo puede reportar una vez la misma pregunta (evita inflar el conteo).
CREATE UNIQUE INDEX IF NOT EXISTS idx_feedback_pregunta_usuario
    ON feedback_preguntas(pregunta_id, usuario_id);

-- Acelera el conteo de reportes por pregunta (usado para decidir la degradación).
CREATE INDEX IF NOT EXISTS idx_feedback_pregunta ON feedback_preguntas(pregunta_id);

COMMENT ON TABLE feedback_preguntas IS
    'Reportes de los aspirantes sobre preguntas problemáticas. Al superar un umbral de '
    'reportes distintos, la pregunta se marca RECHAZADA y se retira del banco.';
