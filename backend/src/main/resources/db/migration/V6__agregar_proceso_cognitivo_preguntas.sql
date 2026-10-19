-- V6: Proceso cognitivo evaluado por cada pregunta (metodología de ítems CNSC).
-- Valores: EVOCACION, COMPRENSION, APLICACION. Se guarda como VARCHAR (coherente
-- con V5, que pasó todos los enums a texto). Default APLICACION por ser el proceso
-- más representativo de las pruebas funcionales/comportamentales de la CNSC.

ALTER TABLE preguntas
    ADD COLUMN proceso_cognitivo VARCHAR(20) NOT NULL DEFAULT 'APLICACION';

COMMENT ON COLUMN preguntas.proceso_cognitivo IS
    'Proceso cognitivo del ítem (EVOCACION, COMPRENSION, APLICACION) según la metodología CNSC';
