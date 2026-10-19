---
inclusion: always
---

# Dominio: Concursos DIAN

## Proceso de Selección DIAN 2676

El concurso DIAN es administrado por la **CNSC (Comisión Nacional del Servicio Civil)**.
Tiene 3 pruebas escritas con naturaleza y umbrales distintos.

## Las 3 Pruebas

| Prueba | Tipo | Umbral aprobación | Carácter |
|---|---|---|---|
| Competencias Funcionales | Conocimiento técnico | **70/100 — ELIMINATORIA** | Obligatorio aprobar |
| Competencias Comportamentales | Escenarios laborales | 60% (clasificatoria) | Suma puntaje |
| Integridad | Dilemas éticos | 60% (clasificatoria) | Suma puntaje |

## Indicadores del Empleo 236861 (Gestor III — Profesional)

### Funcionales (11) — TipoPrueba.FUNCIONAL
1. Algoritmos y Estructuras de Datos
2. Automatización de Pruebas y CI/CD
3. Elicitación de Necesidades y Técnicas de Modelado
4. Ingeniería de Requisitos y Especificación
5. Integración, Administración y Procesamiento de Datos
6. Misionalidad de la DIAN
7. Paradigmas, Buenas Prácticas de Programación, Manejo de Errores y Depuración
8. Pruebas Funcionales y de Regresión
9. Razonamiento Analítico
10. Resolución de Problemas y Eficiencia Computacional
11. Soporte, Operación y Gobernanza Técnica, Incidentes, Monitorización, Documentación y Cumplimiento

### Comportamentales (10) — TipoPrueba.COMPORTAMENTAL
1. Adaptabilidad Nivel 1
2. Comportamiento Ético Nivel 1
3. Comportamiento Ético Nivel 4
4. Comunicación Efectiva Nivel 1
5. Comunicación Efectiva Nivel 4
6. Innovación Nivel 3
7. Orientación al Logro Nivel 1
8. Orientación al Usuario y al Ciudadano Nivel 1
9. Trabajo en Equipo Nivel 1
10. Trabajo en Equipo Nivel 3

### Integridad (5) — TipoPrueba.INTEGRIDAD
1. Compromiso
2. Diligencia
3. Honestidad
4. Justicia
5. Respeto

## Lógica de Clasificación Automática de Indicadores

La clasificación la hace `PerfilClasificacion` (en `domain/model`). Las reglas son
**configurables por concurso**; lo siguiente es solo el **perfil por defecto** (CNSC/DIAN):

```
Si el nombre contiene la palabra comportamental ("NIVEL") → COMPORTAMENTAL
Si el nombre está en el set de integridad {COMPROMISO, DILIGENCIA, HONESTIDAD, JUSTICIA, RESPETO} → INTEGRIDAD
En cualquier otro caso → FUNCIONAL
```

Extracción de nivel: regex `<palabra_comportamental>\s+(\d)` sobre el nombre en mayúsculas.

Para otro concurso del SIMO, el usuario puede enviar un perfil propio al parsear
(`ParsarIndicadoresRequest`): otro set de palabras de integridad, otra palabra
comportamental y otros umbrales. Si no envía nada, se usa este perfil por defecto.

## Lógica de Brechas

Umbrales por defecto (configurables por concurso vía el perfil, guardados por ítem en `umbralBrecha`):
- Funcionales: **70%** (umbral real de eliminación en DIAN 2676)
- Comportamentales e integridad: **60%**

Semáforo de dominio (`DesempenoItem.clasificarNivel`):
- 🔴 ROJO: < 50%
- 🟡 AMARILLO: 50% – (umbral del ítem)
- 🟢 VERDE: >= umbral del ítem

El cálculo de desempeño se hace al finalizar un simulacro: agrupa las respuestas por
ítem, calcula el % de aciertos y asigna el nivel con el umbral configurado de ese ítem.

## Estructura de una Pregunta DIAN

- Formato: selección múltiple, única respuesta, 4 opciones (A, B, C, D)
- Para funcionales: enunciado contextualizado en ambiente DIAN + opciones conceptuales
- Para comportamentales: escenario laboral + 4 conductas posibles
- Para integridad: dilema ético + 4 decisiones posibles
- Cada pregunta incluye: justificación correcta + justificaciones incorrectas + referencia normativa

## Configuración de Preguntas

- El usuario elige cuántas preguntas generar por indicador (mínimo 20, máximo 200)
- Sugerencia automática según dominio previo declarado:
  - NO_SE → 100 preguntas
  - ALGO_SE → 60 preguntas
  - LO_DOMINO → 30 preguntas
- Generación en background, lotes de 10 preguntas por llamada a la IA
