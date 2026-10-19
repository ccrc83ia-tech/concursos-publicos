-- V2: Tablas de concursos e ítems

CREATE TABLE concursos (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre              VARCHAR(200) NOT NULL,
    proceso_seleccion   VARCHAR(50),
    numero_inscripcion  VARCHAR(50),
    numero_empleo       VARCHAR(50),
    nivel_cargo         VARCHAR(50),
    denominacion        VARCHAR(100),
    fecha_examen        DATE,
    activo              BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_concursos_usuario ON concursos(usuario_id);

COMMENT ON TABLE concursos IS 'Concursos de mérito registrados por cada usuario';

-- -------------------------------------------------------

CREATE TYPE tipo_prueba   AS ENUM ('FUNCIONAL', 'COMPORTAMENTAL', 'INTEGRIDAD');
CREATE TYPE dominio_previo AS ENUM ('NO_SE', 'ALGO_SE', 'LO_DOMINO');
CREATE TYPE estado_generacion AS ENUM ('PENDIENTE', 'EN_PROCESO', 'COMPLETADO', 'ERROR');

CREATE TABLE items (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    concurso_id             UUID         NOT NULL REFERENCES concursos(id) ON DELETE CASCADE,
    nombre                  VARCHAR(300) NOT NULL,
    descripcion             TEXT,
    tipo_prueba             tipo_prueba  NOT NULL DEFAULT 'FUNCIONAL',
    nivel_competencia       SMALLINT,                -- 1,2,3,4 para comportamentales; NULL para funcionales e integridad
    peso_porcentual         DECIMAL(5,2) DEFAULT 0,
    preguntas_solicitadas   INTEGER      NOT NULL DEFAULT 50,
    dominio_previo          dominio_previo DEFAULT 'NO_SE',
    umbral_brecha           DECIMAL(5,2) NOT NULL DEFAULT 70.0,
    orden                   INTEGER      NOT NULL DEFAULT 0,
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_items_concurso ON items(concurso_id);
CREATE INDEX idx_items_tipo     ON items(tipo_prueba);

COMMENT ON TABLE  items                    IS 'Indicadores / ítems de cada concurso a evaluar';
COMMENT ON COLUMN items.nivel_competencia  IS 'Solo para comportamentales: 1, 2, 3 o 4';
COMMENT ON COLUMN items.umbral_brecha      IS '70% para funcionales (eliminatoria), 60% para las demás';

-- -------------------------------------------------------

CREATE TABLE generacion_jobs (
    id                      UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id                 UUID             NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    estado                  estado_generacion NOT NULL DEFAULT 'PENDIENTE',
    preguntas_generadas     INTEGER          NOT NULL DEFAULT 0,
    total_esperadas         INTEGER          NOT NULL DEFAULT 0,
    mensaje_error           TEXT,
    created_at              TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_generacion_jobs_item   ON generacion_jobs(item_id);
CREATE INDEX idx_generacion_jobs_estado ON generacion_jobs(estado);
