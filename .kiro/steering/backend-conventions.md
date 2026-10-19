---
inclusion: fileMatch
fileMatchPattern: "**/*.java"
---

# Convenciones Backend Java

## Versiones
- Java 25
- Spring Boot 4.1.0
- Gradle 9.8.1 (via wrapper `gradlew.bat`)

## Paquete raíz
`com.dian.prep`

## Estructura de paquetes hexagonal

```
com.dian.prep
├── domain/
│   ├── model/          → clases de dominio puras (sin anotaciones JPA)
│   ├── port/
│   │   ├── in/         → interfaces de casos de uso (UseCase)
│   │   └── out/        → interfaces de repositorios y servicios externos
│   └── service/        → implementan port/in, usan port/out (@Service)
└── infrastructure/
    ├── adapter/
    │   ├── in/web/     → @RestController, DTOs Request/Response
    │   └── out/
    │       ├── persistence/         → JPA adapters (@Component)
    │       │   ├── entity/          → @Entity classes
    │       │   └── repository/      → JpaRepository interfaces
    │       └── ai/                  → Groq/Gemini adapters (Fase 2)
    └── config/         → SecurityConfig, JwtUtil, JwtAuthFilter, AsyncConfig, OpenApiConfig
```

## Reglas estrictas

1. `domain/` no importa nada de `infrastructure/`
2. Las entidades JPA (`*Entity`) solo existen en `infrastructure/adapter/out/persistence/entity/`
3. Mapeo obligatorio: `*Entity` ↔ modelo de dominio ↔ DTO
4. Los controllers solo reciben y devuelven DTOs
5. Los casos de uso (port/in) usan Records para los Commands
6. Todos los IDs son `UUID`
7. Manejo de errores centralizado en `GlobalExceptionHandler` (`@RestControllerAdvice`)

## Tabla de nomenclatura

| Tipo | Sufijo | Ejemplo |
|---|---|---|
| Caso de uso | UseCase | `RegistrarConcursoUseCase` |
| Servicio de dominio | Service | `ConcursoService` |
| Adaptador JPA | JpaAdapter | `ConcursoJpaAdapter` |
| Entidad JPA | Entity | `ConcursoEntity` |
| Repositorio JPA | JpaRepository | `ConcursoJpaRepository` |
| Controller | Controller | `ConcursoController` |
| DTO entrada | Request | `ConcursoRequest` |
| DTO salida | Response | `ConcursoResponse` |

## Endpoints REST

| Recurso | Path |
|---|---|
| Auth | `/api/auth/registro`, `/api/auth/login` |
| Concursos | `/api/v1/concursos` |
| Items | `/api/v1/concursos/{concursoId}/items` |
| Parseo indicadores | `/api/v1/concursos/{concursoId}/items/parsear` |
| Generación IA | `/api/v1/items/{itemId}/generar` (Fase 2) |
| Simulacros | `/api/v1/simulacros` (Fase 2) |

## Comandos

```powershell
# BD
docker-compose up -d

# Compilar y correr
.\gradlew.bat bootRun

# Solo compilar
.\gradlew.bat compileJava

# Limpiar y compilar
.\gradlew.bat clean build

# Swagger UI
# http://localhost:8080/swagger-ui.html
```

## Notas importantes
- `ddl-auto=update` — Flyway maneja el esquema, Hibernate solo actualiza
- JWT secret mínimo 256 bits definido en `application.yml`
- CORS configurado para `localhost:5173` (Vite dev server)
- `@EnableAsync` activo en `DianPrepApplication` para jobs de generación IA
