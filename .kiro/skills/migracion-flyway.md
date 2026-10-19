---
name: migracion-flyway
description: Guía para crear migraciones Flyway correctamente — naming, SQL patterns, errores frecuentes y rollback
---

# Skill: Migraciones Flyway

## Regla de oro

**Nunca modificar un archivo de migración ya ejecutado.**
Flyway guarda el checksum de cada migración aplicada. Si el archivo cambia, el backend falla al arrancar con `checksum mismatch`. Siempre crear un nuevo archivo.

## ⚠️ NO usar tipos ENUM nativos de PostgreSQL (`CREATE TYPE ... AS ENUM`)

Las migraciones V2/V3 crearon tipos enum nativos (`tipo_prueba`, `opcion_respuesta`, etc.).
Con **Hibernate 7.x** esto provoca un fallo al arrancar (`PostgreSQLEnumJdbcType` →
`NullPointerException`) cuando la entidad mapea la columna como `String` (que es como
las mapea este proyecto). La migración **V5** los convirtió todos a `VARCHAR`.

**Regla:** para campos tipo enum, usar siempre `VARCHAR(n)` + `CHECK (...)`. Los valores
válidos se validan además en el dominio Java. Nunca `CREATE TYPE ... AS ENUM`.

## Naming convention

```
V{versión}__{descripción}.sql
│              │
│              └─ snake_case, palabras separadas por guiones bajos
└─ Número entero correlativo. Ver la última versión existente.
```

**Ejemplos correctos:**
```
V1__crear_tabla_usuarios.sql
V2__crear_tablas_concurso_items.sql
V3__crear_tablas_preguntas_simulacros.sql
V4__agregar_columna_nivel_item.sql
V5__crear_tabla_generacion_jobs.sql
```

**Reglas:**
- Doble guion bajo `__` entre versión y descripción (obligatorio)
- Sin espacios en el nombre del archivo
- Extensión `.sql` en minúscula
- La versión no puede repetirse
- Los archivos van en: `src/main/resources/db/migration/`

## Ver la última versión existente

```powershell
# Listar las migraciones actuales
Get-ChildItem c:\proyectos\concursos-publicos\backend\src\main\resources\db\migration\ | Sort-Object Name
```

**Migraciones existentes a la fecha:** V1..V5 ya están aplicadas. La próxima debe ser **V6**.

O en pgAdmin:
```sql
SELECT version, description, installed_on
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 5;
```

## Plantilla de migración — nueva tabla

```sql
-- V{N}__crear_tabla_preguntas.sql   (usar el siguiente número libre; hoy sería V6)

CREATE TABLE preguntas (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id         UUID NOT NULL,
    enunciado       TEXT NOT NULL,
    opcion_a        TEXT NOT NULL,
    opcion_b        TEXT NOT NULL,
    opcion_c        TEXT NOT NULL,
    opcion_d        TEXT NOT NULL,
    respuesta_correcta  CHAR(1) NOT NULL CHECK (respuesta_correcta IN ('A','B','C','D')),
    justificacion_correcta   TEXT,
    referencia_normativa     TEXT,
    nivel_dificultad         VARCHAR(20) DEFAULT 'INTERMEDIO',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_preguntas_item
        FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- Índices para consultas frecuentes
CREATE INDEX idx_preguntas_item_id ON preguntas(item_id);
CREATE INDEX idx_preguntas_nivel   ON preguntas(nivel_dificultad);

COMMENT ON TABLE preguntas IS 'Banco de preguntas generadas por IA por indicador';
COMMENT ON COLUMN preguntas.respuesta_correcta IS 'Letra de la opción correcta: A, B, C o D';
COMMENT ON COLUMN preguntas.nivel_dificultad   IS 'BASICO, INTERMEDIO o AVANZADO';
```

## Plantilla de migración — agregar columna

```sql
-- V{N}__agregar_dominio_previo_item.sql

ALTER TABLE items
    ADD COLUMN IF NOT EXISTS dominio_previo VARCHAR(20) DEFAULT 'NO_SE',
    ADD COLUMN IF NOT EXISTS cantidad_preguntas_objetivo INTEGER DEFAULT 100;

-- Actualizar registros existentes con valor por defecto
UPDATE items SET dominio_previo = 'NO_SE' WHERE dominio_previo IS NULL;

-- Agregar constraint después de poblar
ALTER TABLE items
    ADD CONSTRAINT chk_dominio_previo
    CHECK (dominio_previo IN ('NO_SE','ALGO_SE','LO_DOMINO'));
```

## Plantilla de migración — tabla de relación (join table)

```sql
-- V{N}__crear_tabla_simulacro_preguntas.sql

CREATE TABLE simulacro_preguntas (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    simulacro_id    UUID NOT NULL,
    pregunta_id     UUID NOT NULL,
    orden           INTEGER NOT NULL,
    respuesta_dada  CHAR(1) CHECK (respuesta_dada IN ('A','B','C','D')),
    tiempo_respuesta_segundos  INTEGER,
    respondida      BOOLEAN NOT NULL DEFAULT FALSE,
    marcada_revision BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_sp_simulacro FOREIGN KEY (simulacro_id)
        REFERENCES simulacros(id) ON DELETE CASCADE,
    CONSTRAINT fk_sp_pregunta  FOREIGN KEY (pregunta_id)
        REFERENCES preguntas(id) ON DELETE CASCADE,
    CONSTRAINT uq_sp_simulacro_orden
        UNIQUE (simulacro_id, orden)
);

CREATE INDEX idx_sp_simulacro_id ON simulacro_preguntas(simulacro_id);
CREATE INDEX idx_sp_pregunta_id  ON simulacro_preguntas(pregunta_id);
```

## Patrones SQL PostgreSQL para este proyecto

```sql
-- UUID como PK (siempre)
id UUID PRIMARY KEY DEFAULT gen_random_uuid()

-- Timestamps automáticos
created_at TIMESTAMP NOT NULL DEFAULT NOW(),
updated_at TIMESTAMP NOT NULL DEFAULT NOW()

-- Enum como VARCHAR con CHECK
tipo_prueba VARCHAR(20) NOT NULL CHECK (tipo_prueba IN ('FUNCIONAL','COMPORTAMENTAL','INTEGRIDAD'))

-- Foreign key con cascade
CONSTRAINT fk_nombre FOREIGN KEY (columna_id) REFERENCES tabla(id) ON DELETE CASCADE

-- Índice para búsquedas frecuentes
CREATE INDEX idx_tabla_columna ON tabla(columna);

-- Índice para columnas con alta cardinalidad + filtros
CREATE INDEX idx_items_concurso_tipo ON items(concurso_id, tipo_prueba);
```

## Errores frecuentes y soluciones

### checksum mismatch
```
FlywayException: Validate failed: Migration checksum mismatch for migration version X
```
**Causa:** Se editó un archivo `.sql` ya aplicado.
**Solución:** 
```sql
-- Opción A (desarrollo local): reparar el checksum
-- En pgAdmin ejecutar:
DELETE FROM flyway_schema_history WHERE version = 'X';
-- Luego reiniciar el backend (re-aplica la migración)

-- Opción B (correcta): crear V{X+1} con los cambios adicionales
```

### out of order
```
FlywayException: Detected resolved migration not applied to database: version X
```
**Causa:** Se creó un archivo con versión menor a la ya aplicada.
**Solución:** Renombrar el archivo con la siguiente versión disponible.

### tabla ya existe
```
PSQLException: relation "tabla" already exists
```
**Causa:** Se intenta crear una tabla que ya existe en la BD.
**Solución:** Usar `CREATE TABLE IF NOT EXISTS` en desarrollo, o crear la migración con el número correcto.

## Workflow recomendado al agregar una entidad

1. Revisar el número de la última migración aplicada
2. Crear `V{N+1}__{descripcion}.sql` con la nueva tabla
3. Arrancar el backend — Flyway la aplica automáticamente al arrancar
4. Verificar en pgAdmin que la tabla existe
5. Crear la `*Entity.java` con los mismos nombres de columnas

## Checklist de migración

- [ ] El número de versión es el siguiente disponible
- [ ] El nombre usa doble guion bajo `__`
- [ ] Todas las tablas tienen `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- [ ] Todas las tablas tienen `created_at` y `updated_at`
- [ ] Las foreign keys tienen nombre descriptivo (`fk_tabla_columna`)
- [ ] Los índices cubren columnas de búsqueda frecuente
- [ ] Los ENUMs usan `CHECK` con los valores del dominio Java
- [ ] No se modificó ningún archivo de migración ya existente
