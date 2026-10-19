---
inclusion: always
---

# Estado del Desarrollo

## Resumen

| Capacidad | Código | Verificado en vivo |
|---|---|---|
| Auth JWT + registro de concursos + indicadores | ✅ 100% | ✅ OK |
| Generalización para cualquier concurso del SIMO (Fase A) | ✅ ~85% | ✅ OK (perfil custom probado) |
| Generación de preguntas con IA (Fase B.1) | ✅ 100% | ✅ OK (Groq genera y persiste) |
| Simulacros cronometrados + desempeño (Fase B.2) | ✅ 100% | 🟡 parcial (falta correr un simulacro completo) |
| Dashboard de brechas (semáforo por concurso) | ✅ 100% (backend + UI Recharts) | ❌ falta verificar en vivo |
| Recomendaciones de estudio con IA | ✅ 100% (backend + UI) | ❌ falta verificar en vivo |
| Examen adaptativo de brechas (70/30) | ✅ 100% (selección ponderada) | ❌ falta verificar en vivo |

**Avance global aproximado: ~90% del producto descrito en la visión (pendiente verificación en vivo).**

> "Código" = escrito y compila. "Verificado en vivo" = probado contra BD real / API de IA.

---

## ✅ El backend ARRANCA y la IA FUNCIONA (bloqueo resuelto)

El bloqueo de arranque quedó resuelto. Lo que lo causaba y cómo se solucionó:

1. **Tipos ENUM nativos de Postgres incompatibles con Hibernate 7.4** → migración **V5**
   convirtió todas las columnas enum a `VARCHAR` y eliminó los tipos enum.
2. **Faltaba el bean `ObjectMapper`** que inyectaban los adaptadores de IA/persistencia JSONB
   → se creó `config/JacksonConfig.java`.

Verificado en vivo: Tomcat levanta en :8080, Flyway aplica V1–V5, JPA inicializa, y el flujo
`registro → login → concurso → parsear indicadores (con perfil SIMO custom)` responde correctamente.

### Generación con IA — verificada contra Groq
- **Modelo:** `llama-3.3-70b-versatile` fue descontinuado por Groq. Se cambió a
  `openai/gpt-oss-120b` (configurable con `GROQ_MODEL`). Confirmado con el endpoint `/models` de la cuenta.
- **Bug de parseo corregido:** el `RestClient` no sabía convertir a `JsonNode`; ahora lee el
  cuerpo como `String` y lo parsea con `ObjectMapper`.
- **Rate limit (free tier 8.000 TPM):** lote reducido a 5 preguntas, `max-tokens` a 2500,
  pausa de 15s entre lotes.
- **Evidencia:** jobs pasan a `EN_PROCESO`/`COMPLETADO` y se guardan preguntas reales en la
  tabla `preguntas` (enunciado contextualizado, 4 opciones, respuesta correcta, dificultad).

### Cómo arrancar el backend con la key
```powershell
cd c:\proyectos\concursos-publicos\backend
$env:GROQ_API_KEY = "<tu_key_de_groq>"   # nunca commitearla
.\gradlew.bat bootRun
```
> La API key NO se guarda en el repo. Se pasa por variable de entorno al proceso.

---

## Implementado en esta fase

### Fase A — Desacople de DIAN (sirve para cualquier concurso del SIMO)
- `domain/model/PerfilClasificacion.java` — reglas de clasificación como datos configurables
  (palabras de integridad, palabra comportamental, regex de nivel, umbrales). DIAN = perfil por defecto.
- `ItemService` refactorizado: usa el perfil en vez de constantes DIAN hardcodeadas.
- `ParsarIndicadoresCommand` / `ParsarIndicadoresRequest` aceptan perfil opcional.
- Frontend: defaults DIAN removidos, textos genéricos SIMO.

### Fase B.1 — Generación de preguntas con IA
- Dominio: `Pregunta`, `GeneracionJob`.
- Puertos: `GenerarPreguntasUseCase` (in); `PreguntaRepository`, `GeneracionJobRepository`, `IaGeneradorPort` (out).
- `GeneracionPreguntasService` + `GeneracionPreguntasRunner` (`@Async`, lotes de 10, pausa 2s, 3 reintentos backoff).
- `IaGeneradorAdapter` — Groq (OpenAI-compatible) con fallback Gemini vía `RestClient`.
- `PromptBuilder` — prompts por tipo de prueba, parametrizados con el concurso (sin hardcode DIAN).
- `GeneracionController`: `POST /items/{id}/generar`, `GET /items/{id}/generacion/estado`, `GET /items/{id}/preguntas`.
- Frontend: `api/generacion.ts`, componente `GeneracionItem` (barra de progreso con polling), integrado en `DetalleConcurso`.

### Fase B.2 — Simulacros cronometrados
- Dominio: `Simulacro`, `SimulacroPregunta`, `DesempenoItem` (semáforo ROJO<50 / AMARILLO<umbral / VERDE>=umbral).
- Puertos: `GestionarSimulacrosUseCase` (in); `SimulacroRepository`, `DesempenoRepository` (out).
- `SimulacroService`: arma simulacro con preguntas aleatorias, califica contra la respuesta real, calcula desempeño por ítem.
- Persistencia + migración **V4** (agrega `item_id` a `simulacro_preguntas`).
- `SimulacroController`: `POST /simulacros`, `GET /simulacros/{id}`, `POST /{id}/responder`, `POST /{id}/finalizar`, `GET /{id}/desempeno`, `GET /brechas/{concursoId}`.
- `SimulacroResponse` oculta la respuesta correcta mientras EN_CURSO; la revela al COMPLETADO.
- Frontend: `api/simulacros.ts`, páginas `ConfigurarSimulacro`, `SimulacroActivo` (cronómetro con auto-finalización), `ResultadoSimulacro` (puntaje + semáforo + revisión). Rutas en `App.tsx`.

---

## Migraciones Flyway (estado)
- `V1` usuarios
- `V2` concursos + items + generacion_jobs
- `V3` preguntas + simulacros + simulacro_preguntas + desempeno_item + recomendaciones
- `V4` agrega `item_id` a `simulacro_preguntas`
- `V5` convierte todos los `enum` nativos a `VARCHAR` y elimina los tipos enum (ver bloqueo arriba)

---

## Comandos para correr en local

```powershell
# BD (ya suele estar corriendo en Docker)
cd c:\proyectos\concursos-publicos\backend
docker-compose up -d

# Backend (definir GROQ_API_KEY antes para habilitar la generación IA)
$env:GROQ_API_KEY = "<tu_key>"
.\gradlew.bat bootRun

# Frontend
cd c:\proyectos\concursos-publicos\frontend
npm run dev
```

## URLs
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- pgAdmin: http://localhost:5050 (admin@gmail.com / admin123)

## Variables de entorno (para generación IA)
```
GROQ_API_KEY=     # console.groq.com — gratis
GEMINI_API_KEY=   # aistudio.google.com — gratis (fallback)
```

---

## Implementado en la fase C (commit feature/concursos `101a153`)

### Dashboard de brechas (UI)
- `frontend/src/pages/concursos/DashboardBrechas.tsx` — Recharts: barras horizontales por
  indicador (color por semáforo + línea de meta 70%), radar de cobertura (si ≥3 ítems),
  tarjetas resumen (promedio / críticos / dominados) y panel de plan de estudio IA.
- Ruta `/concursos/:concursoId/brechas` + botón de acceso en `DetalleConcurso`.
- `api/recomendaciones.ts`, tipos `Recomendacion`/`TemaFallido`/`ReferenciaNormativa`.

### Recomendaciones de estudio con IA
- Dominio: `Recomendacion` (records `TemaFallido`, `ReferenciaNormativa`).
- Puertos: `GenerarRecomendacionesUseCase` (in); `RecomendacionRepository`,
  `GenerarRecomendacionPort` (out).
- `RecomendacionService` — idempotente por simulacro; solo genera para ítems con brecha (no VERDE).
- `RecomendacionIaAdapter` — Groq + Gemini con **fallback local** calculado si no hay API key.
- Persistencia sobre la tabla `recomendaciones` de V3 (JSONB con `@JdbcTypeCode`), sin migración nueva.
- `RecomendacionController`: `POST /recomendaciones/simulacro/{id}`, `GET /recomendaciones/concurso/{id}`.

### Examen adaptativo 70/30
- `SimulacroService.crear`: cuando el tipo es `BRECHAS` reparte el presupuesto de preguntas
  70% a ítems débiles (ROJO/AMARILLO o sin historial) y 30% a los dominados (VERDE),
  equitativo dentro de cada grupo; si un grupo está vacío su cupo pasa al otro.

### Deuda técnica resuelta
- `ItemRequest.preguntasSolicitadas`: mínimo bajado de 20 a 1 (pruebas sin agotar el free tier).
- Swagger: `springdoc` 2.8.8 → **3.1.1** (línea compatible con Spring Boot 4.x) y se quitó el
  override de `api-docs.path` para servir `/v3/api-docs` en la ruta estándar.
- Typo `parsar → parsear` corregido en backend y frontend (incluye rename del DTO).
- Entorno: Git instalado y operativo; `.gitignore` ya **no** ignora `.kiro/` (se versionan
  skills y steering); se ignora `frontend/tsconfig.tsbuildinfo`.

---

## Pendiente (para llegar al 100%)

### Verificación en vivo (lo más importante ahora)
Todo lo de la fase C compila (backend `compileJava` y frontend `tsc + vite build` en verde),
pero NO se ha probado contra la BD y la API de IA reales. Falta levantar el entorno y verificar:
1. **Simulacro completo end-to-end** (crear → responder → finalizar → resultado con semáforo).
2. **Dashboard de brechas** pintando datos reales tras un simulacro.
3. **Recomendaciones IA** (`POST /recomendaciones/simulacro/{id}`) contra Groq.
4. **Adaptativo 70/30**: crear un simulacro tipo `BRECHAS` y confirmar el reparto real.
   Nota: sin desempeño previo, todos los ítems cuentan como débiles y el reparto es uniforme (esperado).
5. Confirmar que `/v3/api-docs` responde 200 con springdoc 3.1.1 al arrancar.

### Deuda técnica restante (no bloqueante)
6. `ddl-auto=validate` (verificar que el esquema JPA valida contra Flyway).
7. Tests automáticos: del parser de indicadores y del cálculo de puntaje/desempeño (no existen aún).
8. Decidir renombrado del paquete `com.dian.prep` (el producto ya es genérico para el SIMO).
