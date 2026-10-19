-- V4: el cálculo de desempeño por ítem necesita saber a qué ítem pertenece
-- cada pregunta del simulacro sin tener que navegar preguntas -> items.

ALTER TABLE simulacro_preguntas
    ADD COLUMN item_id UUID REFERENCES items(id) ON DELETE CASCADE;

CREATE INDEX idx_sim_preguntas_item ON simulacro_preguntas(item_id);
