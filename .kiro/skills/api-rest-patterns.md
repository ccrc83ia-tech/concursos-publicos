---
name: api-rest-patterns
description: Convenciones REST del proyecto DIAN — validación, paginación, manejo de errores, respuestas estándar y patrones de controller
---

# Skill: Patrones REST del Proyecto DIAN

## Convenciones de URL

```
/api/auth/          → autenticación (sin JWT requerido)
/api/v1/            → recursos protegidos (requieren JWT)

# Recursos
GET    /api/v1/concursos                     → listar todos
POST   /api/v1/concursos                     → crear
GET    /api/v1/concursos/{id}                → obtener uno
PUT    /api/v1/concursos/{id}                → actualizar completo
DELETE /api/v1/concursos/{id}                → eliminar

# Sub-recursos
GET    /api/v1/concursos/{id}/items          → items de un concurso
POST   /api/v1/concursos/{id}/items          → crear item en concurso

# Acciones (verbos cuando REST puro no aplica)
POST   /api/v1/items/{id}/generar            → disparar generación IA
POST   /api/v1/simulacros/{id}/finalizar     → acción de negocio
GET    /api/v1/items/{id}/generacion/estado  → consultar progreso
```

## Plantilla de controller

```java
@RestController
@RequestMapping("/api/v1/recursos")
@Tag(name = "Recursos", description = "Gestión de recursos")
@SecurityRequirement(name = "bearerAuth")
public class RecursoController {

    private final GestionarRecursoUseCase recursoUseCase;

    public RecursoController(GestionarRecursoUseCase recursoUseCase) {
        this.recursoUseCase = recursoUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar recursos del usuario autenticado")
    public ResponseEntity<List<RecursoResponse>> listar(Authentication auth) {
        UUID usuarioId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(recursoUseCase.listarPorUsuario(usuarioId));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo recurso")
    public ResponseEntity<RecursoResponse> crear(
            @RequestBody @Valid RecursoRequest request,
            Authentication auth) {
        UUID usuarioId = UUID.fromString(auth.getName());
        RecursoResponse response = recursoUseCase.crear(
            new GestionarRecursoUseCase.CrearRecursoCommand(
                request.nombre(), request.descripcion(), usuarioId
            )
        );
        URI location = URI.create("/api/v1/recursos/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un recurso por ID")
    public ResponseEntity<RecursoResponse> obtener(
            @PathVariable UUID id,
            Authentication auth) {
        UUID usuarioId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(recursoUseCase.obtenerPorId(id, usuarioId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un recurso")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            Authentication auth) {
        UUID usuarioId = UUID.fromString(auth.getName());
        recursoUseCase.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
```

## Plantilla de DTOs

```java
// RecursoRequest.java — entrada con validaciones
public record RecursoRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    String nombre,

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    String descripcion,

    @NotNull(message = "El tipo es obligatorio")
    TipoRecurso tipo,                          // enum del dominio

    @Min(value = 1, message = "Debe ser mayor a 0")
    @Max(value = 200, message = "No puede superar 200")
    Integer cantidad
) {}

// RecursoResponse.java — salida
public record RecursoResponse(
    UUID id,
    String nombre,
    String descripcion,
    TipoRecurso tipo,
    int cantidad,
    String creadoEn        // ISO 8601
) {
    // Factory method — único punto de mapeo domain → DTO
    public static RecursoResponse from(Recurso recurso) {
        return new RecursoResponse(
            recurso.getId(),
            recurso.getNombre(),
            recurso.getDescripcion(),
            recurso.getTipo(),
            recurso.getCantidad(),
            recurso.getCreadoEn().toString()
        );
    }
}
```

## Manejo de errores — GlobalExceptionHandler

El `GlobalExceptionHandler` existente debe cubrir todos estos casos:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validación de @Valid en @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse(400, "Validación fallida", mensaje));
    }

    // Recurso no encontrado
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(404)
            .body(new ApiErrorResponse(404, "No encontrado", ex.getMessage()));
    }

    // Conflicto de negocio (email duplicado, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(IllegalArgumentException ex) {
        return ResponseEntity.status(409)
            .body(new ApiErrorResponse(409, "Conflicto", ex.getMessage()));
    }

    // Acceso prohibido (recurso de otro usuario)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(403)
            .body(new ApiErrorResponse(403, "Acceso denegado", ex.getMessage()));
    }

    // Constraint de BD violado
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDbConstraint(
            DataIntegrityViolationException ex) {
        return ResponseEntity.status(409)
            .body(new ApiErrorResponse(409, "Conflicto de datos",
                "Ya existe un registro con esos datos"));
    }

    // Error genérico — nunca exponer el stack trace
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.status(500)
            .body(new ApiErrorResponse(500, "Error interno",
                "Ocurrió un error inesperado"));
    }
}
```

## Estructura de ApiErrorResponse

```java
public record ApiErrorResponse(
    int status,
    String error,
    String mensaje
) {}
```

**Respuesta JSON resultante:**
```json
{
  "status": 400,
  "error": "Validación fallida",
  "mensaje": "nombre: El nombre es obligatorio; cantidad: Debe ser mayor a 0"
}
```

## Paginación (cuando se implemente)

```java
// Controller
@GetMapping
public ResponseEntity<Page<RecursoResponse>> listar(
        @RequestParam(defaultValue = "0") int pagina,
        @RequestParam(defaultValue = "20") int tamanio,
        Authentication auth) {
    Pageable pageable = PageRequest.of(pagina, tamanio, Sort.by("creadoEn").descending());
    UUID usuarioId = UUID.fromString(auth.getName());
    return ResponseEntity.ok(recursoUseCase.listar(usuarioId, pageable));
}

// URL de uso: GET /api/v1/recursos?pagina=0&tamanio=10
```

## Respuestas HTTP correctas por operación

| Operación | Status | Body |
|---|---|---|
| GET lista | 200 OK | `List<Response>` |
| GET uno | 200 OK | `Response` |
| GET no encontrado | 404 Not Found | `ApiErrorResponse` |
| POST crear | 201 Created + `Location` header | `Response` |
| PUT actualizar | 200 OK | `Response` |
| DELETE | 204 No Content | vacío |
| Validación falla | 400 Bad Request | `ApiErrorResponse` |
| Sin autenticar | 401 Unauthorized | `ApiErrorResponse` |
| Sin permisos | 403 Forbidden | `ApiErrorResponse` |
| Duplicado | 409 Conflict | `ApiErrorResponse` |

## Extraer el usuarioId del token JWT

```java
// En cualquier controller, el nombre del principal es el UUID del usuario
UUID usuarioId = UUID.fromString(auth.getName());

// JwtAuthFilter configura el principal así:
// UsernamePasswordAuthenticationToken(usuarioId.toString(), null, authorities)
```

## Checklist de un controller nuevo

- [ ] `@RestController` + `@RequestMapping("/api/v1/...")`
- [ ] `@SecurityRequirement(name = "bearerAuth")` para que aparezca en Swagger
- [ ] Inyecta solo la interfaz `UseCase`, no el `Service` directamente
- [ ] Todos los `@RequestBody` usan `@Valid`
- [ ] Extrae `UUID usuarioId` de `Authentication auth`
- [ ] POST retorna `201 Created` con header `Location`
- [ ] DELETE retorna `204 No Content`
- [ ] Los errores los maneja `GlobalExceptionHandler`, el controller no hace try/catch
