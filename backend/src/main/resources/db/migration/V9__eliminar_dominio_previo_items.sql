-- =====================================================================
-- V9 — Eliminar la autoevaluación manual "dominio_previo" de los ítems.
--
-- El "dominio" de un tema deja de declararse a mano (NO_SE/ALGO_SE/LO_DOMINO)
-- y pasa a derivarse del desempeño real en los simulacros: todo tema parte en
-- 0% y su porcentaje de dominio se calcula a partir del último simulacro
-- (ya persistido en desempeno_item.puntaje_porcentual / nivel_dominio).
--
-- No se añaden columnas nuevas a items: el dominio real ya vive en
-- desempeno_item; el backend lo adjunta a cada ítem al listarlos.
-- =====================================================================

ALTER TABLE items DROP COLUMN IF EXISTS dominio_previo;

-- El tipo enum nativo dominio_previo ya se había convertido a VARCHAR y
-- eliminado en V5; por si en alguna BD quedara, se elimina de forma segura.
DROP TYPE IF EXISTS dominio_previo;
