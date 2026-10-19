---
name: generacion-preguntas-ia
description: Implementación de la generación de preguntas con Groq API (Llama 3.3 70B) para cada ítem del concurso
---

# Skill: Generación de Preguntas con IA

## Contexto
La generación usa **Groq API** (gratuita, sin tarjeta) con modelo `llama-3.3-70b-versatile`.
Fallback automático a **Gemini 2.5 Flash** si Groq falla.
El proceso es **asíncrono** — no bloquea al usuario.

## Estructura de una pregunta DIAN

Cada pregunta generada debe tener:
```json
{
  "enunciado": "Texto de la pregunta contextualizado en ambiente DIAN",
  "opcionA": "...",
  "opcionB": "...",
  "opcionC": "...",
  "opcionD": "...",
  "respuestaCorrecta": "A|B|C|D",
  "justificacionCorrecta": "Por qué esa opción es correcta",
  "justificacionIncorrectas": {
    "A": "Por qué A es incorrecta (si A no es la correcta)",
    "B": "...", "C": "...", "D": "..."
  },
  "referenciaNormativa": "Art. X de Y / Manual Z",
  "nivelDificultad": "BASICO|INTERMEDIO|AVANZADO"
}
```

## Tipos de prompt según TipoPrueba

### FUNCIONAL
```
Genera {N} preguntas de selección múltiple sobre "{NOMBRE_INDICADOR}" 
para el concurso de méritos DIAN 2676, cargo Gestor III.
Formato: enunciado técnico contextualizado en procesos de la DIAN,
4 opciones (A,B,C,D), una sola correcta.
Incluye referencia normativa exacta (decreto, resolución, manual).
```

### COMPORTAMENTAL
```
Genera {N} preguntas de competencias comportamentales sobre 
"{NOMBRE_INDICADOR}" nivel {NIVEL} para el concurso DIAN 2676.
Formato: escenario laboral real en la DIAN + 4 conductas posibles.
Una conducta es claramente la más efectiva según el nivel evaluado.
```

### INTEGRIDAD
```
Genera {N} preguntas de integridad sobre "{NOMBRE_INDICADOR}" 
para el concurso DIAN 2676.
Formato: dilema ético en contexto de función pública + 4 decisiones.
Una decisión refleja mejor el valor de {NOMBRE_INDICADOR}.
```

## Arquitectura del job asíncrono (IMPLEMENTADO)

```
GeneracionController
  → POST /api/v1/items/{itemId}/generar
  → GenerarPreguntasUseCase.iniciarGeneracion(itemId)
     → GeneracionPreguntasService crea el GeneracionJob (estado PENDIENTE)
     → GeneracionPreguntasRunner.ejecutar(...)  @Async("generacionPreguntasExecutor")
        → IaGeneradorAdapter.generarLote(contexto, 10)  (Groq; fallback Gemini)
        → guarda cada lote con PreguntaRepository
        → job.sumarGeneradas(...) y persiste el progreso tras cada lote
        → pausa 2s entre lotes; hasta 3 reintentos con backoff lineal
        → al terminar: estado COMPLETADO (o ERROR con mensaje)

GET /api/v1/items/{itemId}/generacion/estado  → GeneracionJobResponse con % progreso
GET /api/v1/items/{itemId}/preguntas          → lista de preguntas generadas
```

## Archivos reales (ya existen)

**Dominio:**
- `model/Pregunta.java`, `model/GeneracionJob.java`
- `port/in/GenerarPreguntasUseCase.java`
- `port/out/PreguntaRepository.java`, `port/out/GeneracionJobRepository.java`, `port/out/IaGeneradorPort.java`
- `service/GeneracionPreguntasService.java` + `service/GeneracionPreguntasRunner.java` (el @Async)

**Infraestructura:**
- `adapter/out/persistence/entity/PreguntaEntity.java`, `GeneracionJobEntity.java` (+ repos y adapters)
- `adapter/out/ai/IaGeneradorAdapter.java` (Groq OpenAI-compatible + fallback Gemini, un solo adaptador)
- `adapter/out/ai/PromptBuilder.java` (prompts por tipo de prueba, parametrizados con el concurso)
- `config/IaProperties.java` (prefijo `app.ai`)
- `adapter/in/web/GeneracionController.java` + DTOs `GeneracionJobResponse`, `PreguntaResponse`

**Frontend:** `api/generacion.ts`, `components/shared/GeneracionItem.tsx` (botón + barra de progreso con polling).

> Nota: el prompt se construye con el nombre del concurso/denominación para que
> funcione con cualquier convocatoria del SIMO, no solo DIAN.

## Variables de entorno requeridas
```
GROQ_API_KEY=     # console.groq.com — gratis, sin tarjeta
GEMINI_API_KEY=   # aistudio.google.com — gratis, sin tarjeta (fallback)
```

## Límites de rate Groq (free tier)
- 30 req/min
- 1.000 req/día para llama-3.3-70b-versatile
- 100K tokens/día
- Estrategia implementada: lotes de 10 preguntas, pausa 2s entre lotes, 3 reintentos con backoff.

## Pendiente de verificación
La generación real contra Groq NO se ha probado en vivo (requiere API key y que el
backend arranque — ver bloqueo en `estado-desarrollo.md`). El código compila y ensambla.
