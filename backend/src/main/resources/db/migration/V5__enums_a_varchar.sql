-- V5: Convertir los tipos ENUM nativos de PostgreSQL a VARCHAR.
--
-- Motivo: Hibernate 7.x autodetecta columnas de tipo enum nativo y les aplica
-- PostgreSQLEnumJdbcType, que exige mapear el campo Java como enum. En este
-- proyecto los enums se mapean como String (modelo de dominio desacoplado de JPA),
-- lo que provoca un fallo al construir el EntityManagerFactory. Usar VARCHAR
-- elimina esa fricción y mantiene los valores idénticos (se validan en el dominio).

-- items
ALTER TABLE items           ALTER COLUMN tipo_prueba        TYPE VARCHAR(20) USING tipo_prueba::text;
ALTER TABLE items           ALTER COLUMN dominio_previo     TYPE VARCHAR(20) USING dominio_previo::text;

-- generacion_jobs
ALTER TABLE generacion_jobs ALTER COLUMN estado             TYPE VARCHAR(20) USING estado::text;

-- preguntas
ALTER TABLE preguntas       ALTER COLUMN respuesta_correcta TYPE VARCHAR(1)  USING respuesta_correcta::text;
ALTER TABLE preguntas       ALTER COLUMN nivel_dificultad   TYPE VARCHAR(20) USING nivel_dificultad::text;

-- simulacros
ALTER TABLE simulacros      ALTER COLUMN tipo               TYPE VARCHAR(20) USING tipo::text;
ALTER TABLE simulacros      ALTER COLUMN estado             TYPE VARCHAR(20) USING estado::text;

-- simulacro_preguntas
ALTER TABLE simulacro_preguntas ALTER COLUMN respuesta_dada TYPE VARCHAR(1)  USING respuesta_dada::text;

-- desempeno_item
ALTER TABLE desempeno_item  ALTER COLUMN nivel_dominio      TYPE VARCHAR(20) USING nivel_dominio::text;

-- Eliminar los tipos enum ya no referenciados.
DROP TYPE IF EXISTS tipo_prueba;
DROP TYPE IF EXISTS dominio_previo;
DROP TYPE IF EXISTS estado_generacion;
DROP TYPE IF EXISTS opcion_respuesta;
DROP TYPE IF EXISTS nivel_dificultad;
DROP TYPE IF EXISTS tipo_simulacro;
DROP TYPE IF EXISTS estado_simulacro;
DROP TYPE IF EXISTS nivel_dominio;
