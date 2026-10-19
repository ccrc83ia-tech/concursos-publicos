---
inclusion: always
---

# Proyecto: App Preparación Concursos SIMO/CNSC

## Descripción
Aplicación web para preparar **cualquier concurso de méritos publicado en el SIMO**
(Sistema de Apoyo para la Igualdad, el Mérito y la Oportunidad — CNSC, Colombia).
Permite registrar convocatorias, generar bancos de preguntas con IA, tomar simulacros
cronometrados, detectar brechas de conocimiento y recibir recomendaciones de estudio.

## Enfoque: genérico, no atado a una entidad
El objetivo es que sirva para cualquier convocatoria del SIMO, no solo la DIAN.
Las reglas de clasificación de indicadores (qué es funcional, comportamental o de
integridad, y los umbrales de aprobación) son **configurables por concurso** mediante
`PerfilClasificacion`. La estructura típica de la CNSC (usada por la DIAN) es solo el
**perfil por defecto**; cada concurso puede sobrescribirla sin tocar código.

## Caso de uso de referencia
El perfil por defecto cubre el **Proceso de Selección DIAN 2676**, cargo **Gestor III**
(Empleo 236861): 25 indicadores en 3 pruebas (funcional / comportamental / integridad).
Sirve como ejemplo, no como límite del sistema.

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 25 + Spring Boot 4.1.0 + Gradle 9.8.1 |
| Arquitectura Backend | Hexagonal (Ports & Adapters) |
| Frontend | React 18 + TypeScript + Vite |
| UI | Tailwind CSS + Recharts |
| Base de datos | PostgreSQL 16 (Docker local) |
| Migraciones | Flyway |
| Autenticación | Spring Security + JWT (JJWT 0.12.6) |
| IA Generación | Groq API (Llama 3.3 70B) — gratis, fallback Gemini 2.5 Flash |
| Jobs async | Spring @Async |
| Documentación API | SpringDoc OpenAPI 2.8.8 / Swagger UI |

## Estructura del Workspace

Raíz real del proyecto: `c:\proyectos\concursos-publicos\`

```
c:\proyectos\concursos-publicos\
├── .kiro\              ← configuración Kiro (steering + skills)
├── backend\            ← Java Spring Boot 4.1
│   ├── build.gradle, settings.gradle, gradlew.bat, docker-compose.yml
│   └── src\main\java\com\dian\prep\
│       ├── domain\
│       │   ├── model\          ← Usuario, Concurso, Item, PerfilClasificacion,
│       │   │                      Pregunta, GeneracionJob, Simulacro,
│       │   │                      SimulacroPregunta, DesempenoItem
│       │   ├── port\in\        ← casos de uso (interfaces)
│       │   ├── port\out\       ← puertos de salida (interfaces)
│       │   └── service\        ← implementaciones + runner @Async de generación
│       └── infrastructure\
│           ├── adapter\in\web\          ← REST controllers + DTOs
│           ├── adapter\out\persistence\ ← JPA entities + repos + adapters
│           ├── adapter\out\ai\          ← IaGeneradorAdapter (Groq/Gemini) + PromptBuilder
│           └── config\                  ← Security, JWT, Async, OpenAPI, IaProperties
└── frontend\           ← React 18 + TypeScript + Vite
    └── src\
        ├── api\        ← auth, concursos, items, generacion, simulacros
        ├── components\ ← Layout, ProtectedRoute, Spinner, SemaforoDominio, GeneracionItem
        ├── pages\      ← auth/, dashboard/, concursos/, simulacros/
        ├── store\      ← authStore (Zustand)
        └── types\      ← index.ts
```

> Nota: el paquete raíz sigue siendo `com.dian.prep` por historia del proyecto.
> Renombrarlo a algo neutral (p. ej. `com.simo.prep`) es deuda técnica pendiente.

## Reglas de Arquitectura Hexagonal

1. El paquete `domain` NO puede importar nada de `infrastructure`
2. Los `service` implementan interfaces de `port/in`
3. Los adaptadores JPA implementan interfaces de `port/out`
4. Los controllers solo llaman casos de uso via interfaces `port/in`
5. Nunca exponer entidades JPA (`*Entity`) fuera de `infrastructure`
6. Siempre mapear: Entity ↔ Domain model ↔ DTO

## Convenciones de Código

- Nombres en **español** para dominio de negocio (Concurso, Item, Simulacro)
- Nombres en **inglés** para infraestructura técnica (Repository, Adapter, Config)
- DTOs con sufijo `Request` (entrada) y `Response` (salida)
- Endpoints REST bajo `/api/v1/`
- Auth bajo `/api/auth/`
- Todos los IDs son UUID
- Fechas en ISO 8601

## Variables de Entorno

```
GROQ_API_KEY=       # API key de Groq (gratis en console.groq.com)
GEMINI_API_KEY=     # API key de Gemini (gratis en aistudio.google.com)
```

La BD local no requiere variables de entorno — usa valores por defecto del `application.yml`.
