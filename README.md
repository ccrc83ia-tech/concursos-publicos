# Preparación para Concursos SIMO / CNSC

Aplicación web para preparar **cualquier concurso de méritos publicado en el SIMO**
(Sistema de Apoyo para la Igualdad, el Mérito y la Oportunidad — CNSC, Colombia): registrar
convocatorias e indicadores, generar bancos de preguntas con IA, tomar simulacros cronometrados,
detectar brechas de conocimiento y recibir recomendaciones de estudio.

El sistema es **genérico**: las reglas de clasificación de indicadores y los umbrales de
aprobación son configurables por concurso (`PerfilClasificacion`). El caso de referencia es el
**Proceso de Selección DIAN 2676 — Gestor III (Empleo 236861)**.

---

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 25 · Spring Boot 4.1 · Gradle (wrapper) |
| Arquitectura | Hexagonal (Ports & Adapters) |
| Frontend | React 18 · TypeScript · Vite · Tailwind · Recharts |
| Base de datos | PostgreSQL 16 (Docker) · migraciones Flyway |
| Autenticación | Spring Security + JWT |
| IA | Groq (redactor, OpenAI-compatible) + Gemini (validador / fallback) |
| Docs API | SpringDoc OpenAPI / Swagger UI |

---

## Estructura

```
concursos-publicos/
├── backend/    Java Spring Boot (hexagonal, paquete com.dian.prep)
│   ├── domain/            model · port/in · port/out · service
│   └── infrastructure/    adapter/in/web · adapter/out/{persistence,ai,scheduler} · config
├── frontend/   React + TypeScript + Vite
│   └── src/     api · components · pages · store · types
├── documento/  guías y plan (PDFs y plan-banco-preguntas-doble-ia.md) — ignorado en git
└── .kiro/      steering (convenciones, dominio, estado del desarrollo) y skills
```

---

## Puesta en marcha (local)

Requisitos: Docker, JDK 25, Node 24.

```powershell
# 1) Base de datos
cd backend
docker-compose up -d

# 2) API keys de IA (no se versionan; van por variable de entorno o backend/.env)
$env:GROQ_API_KEY   = "<tu_key_groq>"     # console.groq.com (gratis)
$env:GEMINI_API_KEY = "<tu_key_gemini>"   # aistudio.google.com/apikey (gratis)

# 3) Backend
.\gradlew.bat bootRun        # http://localhost:8080

# 4) Frontend (otra terminal)
cd ../frontend
npm install
npm run dev                  # http://localhost:5173
```

URLs: Frontend `:5173` · API `:8080` · Swagger `/swagger-ui.html` · pgAdmin `:5050`.

> Las API keys **nunca** se commitean (`backend/.env` y `frontend/.env.local` están en `.gitignore`).

---

## Funcionalidades

- **Registro de convocatorias e indicadores** del SIMO, con parseo automático y clasificación
  (funcional / comportamental / integridad) por perfil configurable.
- **Generación de preguntas con doble IA**: un redactor (Groq) produce el material y un validador
  (Gemini) lo revisa por lote; solo se persisten las preguntas aprobadas.
- **Modelo enunciado → preguntas**: cada *enunciado* es un texto de lectura del que se desprenden
  3–4 preguntas de selección múltiple (3 opciones, única respuesta).
- **Banco de calidad**: estados por pregunta (PENDIENTE_VALIDACION / VALIDADA / RECHAZADA / AGOTADA),
  deduplicación por hash, y jobs de mantenimiento y limpieza en segundo plano respetando cupos.
- **Simulacros cronometrados** que sirven preferentemente preguntas validadas, con cálculo de
  desempeño por indicador (semáforo rojo/amarillo/verde) y examen adaptativo 70/30 por brechas.
- **Dashboard de brechas** (Recharts) y **recomendaciones de estudio con IA**.
- **Feedback del usuario**: reportar preguntas problemáticas; al acumular reportes se retiran del banco.

---

## Calidad y verificación

- Compilación: `./gradlew compileJava` (backend) y `npm run build` (frontend).
- Build completo: `./gradlew clean build -x test`.

---

## Documentación interna

El detalle de convenciones, dominio y estado de desarrollo vive en `.kiro/steering/`:
`proyecto.md`, `backend-conventions.md`, `frontend-conventions.md`, `dominio-dian.md` y
`estado-desarrollo.md`. El plan del banco de preguntas con doble IA está en
`documento/plan-banco-preguntas-doble-ia.md`.
