-- V3: Tablas de preguntas, simulacros y resultados

CREATE TYPE nivel_dificultad AS ENUM ('BASICO', 'INTERMEDIO', 'AVANZADO');
CREATE TYPE opcion_respuesta  AS ENUM ('A', 'B', 'C', 'D');
CREATE TYPE estado_simulacro  AS ENUM ('EN_CURSO', 'COMPLETADO', 'ABANDONADO');
CREATE TYPE tipo_simulacro    AS ENUM ('POR_ITEM', 'COMPLETO', 'BRECHAS');
CREATE TYPE nivel_dominio     AS ENUM ('ROJO', 'AMARILLO', 'VERDE');

-- -------------------------------------------------------

CREATE TABLE preguntas (
    id                          UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id                     UUID             NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    enunciado                   TEXT             NOT NULL,
    opcion_a                    TEXT             NOT NULL,
    opcion_b                    TEXT             NOT NULL,
    opcion_c                    TEXT             NOT NULL,
    opcion_d                    TEXT             NOT NULL,
    respuesta_correcta          opcion_respuesta NOT NULL,
    justificacion_correcta      TEXT             NOT NULL,
    justificacion_incorrectas   JSONB,           -- {"A":"...", "B":"...", "C":"..."}
    referencia_normativa        VARCHAR(500),    -- "Art. 647 Estatuto Tributario"
    nivel_dificultad            nivel_dificultad NOT NULL DEFAULT 'INTERMEDIO',
    generada_por_ia             BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_preguntas_item       ON preguntas(item_id);
CREATE INDEX idx_preguntas_dificultad ON preguntas(nivel_dificultad);

COMMENT ON TABLE preguntas IS 'Banco de preguntas generadas por IA por cada ítem';

-- -------------------------------------------------------

CREATE TABLE simulacros (
    id                          UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                  UUID             NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    concurso_id                 UUID             NOT NULL REFERENCES concursos(id) ON DELETE CASCADE,
    tipo                        tipo_simulacro   NOT NULL DEFAULT 'POR_ITEM',
    estado                      estado_simulacro NOT NULL DEFAULT 'EN_CURSO',
    tiempo_limite_segundos      INTEGER,
    tiempo_usado_segundos       INTEGER          DEFAULT 0,
    puntaje_final               DECIMAL(5,2),
    created_at                  TIMESTAMP        NOT NULL DEFAULT NOW(),
    finalizado_at               TIMESTAMP
);

CREATE INDEX idx_simulacros_usuario  ON simulacros(usuario_id);
CREATE INDEX idx_simulacros_concurso ON simulacros(concurso_id);
CREATE INDEX idx_simulacros_estado   ON simulacros(estado);

-- -------------------------------------------------------

CREATE TABLE simulacro_preguntas (
    id                          UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    simulacro_id                UUID             NOT NULL REFERENCES simulacros(id) ON DELETE CASCADE,
    pregunta_id                 UUID             NOT NULL REFERENCES preguntas(id),
    respuesta_dada              opcion_respuesta,
    es_correcta                 BOOLEAN,
    tiempo_respuesta_segundos   INTEGER          DEFAULT 0,
    marcada_para_revision       BOOLEAN          NOT NULL DEFAULT FALSE,
    orden                       INTEGER          NOT NULL DEFAULT 0
);

CREATE INDEX idx_sim_preguntas_simulacro ON simulacro_preguntas(simulacro_id);
CREATE INDEX idx_sim_preguntas_pregunta  ON simulacro_preguntas(pregunta_id);
CREATE UNIQUE INDEX idx_sim_preg_unique  ON simulacro_preguntas(simulacro_id, pregunta_id);

-- -------------------------------------------------------

CREATE TABLE desempeno_item (
    id                          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                  UUID          NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    item_id                     UUID          NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    simulacro_id                UUID          NOT NULL REFERENCES simulacros(id) ON DELETE CASCADE,
    preguntas_correctas         INTEGER       NOT NULL DEFAULT 0,
    total_preguntas             INTEGER       NOT NULL DEFAULT 0,
    puntaje_porcentual          DECIMAL(5,2)  NOT NULL DEFAULT 0,
    nivel_dominio               nivel_dominio NOT NULL DEFAULT 'ROJO',
    created_at                  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_desempeno_usuario ON desempeno_item(usuario_id);
CREATE INDEX idx_desempeno_item    ON desempeno_item(item_id);

-- -------------------------------------------------------

CREATE TABLE recomendaciones (
    id                          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                  UUID      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    item_id                     UUID      NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    simulacro_id                UUID      NOT NULL REFERENCES simulacros(id) ON DELETE CASCADE,
    temas_fallidos              JSONB,    -- [{tema, descripcion}]
    referencias_normativas      JSONB,    -- [{norma, articulo, url}]
    resumen_ejecutivo           TEXT,
    tiempo_estudio_horas        DECIMAL(4,1),
    created_at                  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recomendaciones_usuario ON recomendaciones(usuario_id);
