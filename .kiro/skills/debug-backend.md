---
name: debug-backend
description: Diagnóstico y depuración de errores en el backend Spring Boot — errores comunes, logs, excepciones y herramientas
---

# Skill: Debug Backend Spring Boot

Cuando el usuario reporte un error en el backend, sigue este protocolo de diagnóstico sistemático.

## 1. Identificar la capa del error

```
Error 400 Bad Request      → Validación de DTO o parámetros
Error 401 Unauthorized     → JWT faltante, expirado o inválido
Error 403 Forbidden        → Token válido pero sin permisos
Error 404 Not Found        → Recurso no existe o UUID incorrecto
Error 409 Conflict         → Duplicado (email ya registrado, etc.)
Error 500 Internal Server  → Excepción no controlada en el servicio
Error de startup           → Flyway, DataSource o configuración
```

## 2. Leer los logs correctamente

El log de Spring Boot muestra la excepción completa con stack trace.
Buscar la **causa raíz** (`Caused by:`), no el primer error.

```
# Buscar en el log de bootRun:
Caused by: org.postgresql.util.PSQLException  → problema de BD
Caused by: io.jsonwebtoken.ExpiredJwtException → JWT expirado
Caused by: org.flywaydb.core.api.FlywayException → migración fallida
Caused by: jakarta.validation.ConstraintViolationException → validación
Caused by: org.springframework.dao.DataIntegrityViolationException → constraint BD
```

## 3. Errores más frecuentes y soluciones

### Flyway falla al arrancar
```
FlywayException: Migration checksum mismatch
```
**Causa:** Se modificó un archivo `.sql` ya ejecutado.
**Solución:** Nunca editar migraciones ya aplicadas. Crear una nueva `V{N+1}__...sql`.

```
FlywayException: Found more than one migration with version X
```
**Causa:** Dos archivos con el mismo número de versión.
**Solución:** Renombrar uno de los archivos con el siguiente número disponible.

### Error de serialización JSON circular
```
HttpMessageNotWritableException: Could not write JSON
```
**Causa:** Una entidad JPA tiene relaciones bidireccionales expuestas en el DTO.
**Solución:** Nunca retornar entidades JPA en los controllers. Usar `*Response` DTOs.

### JWT inválido en Swagger UI
```
401 Unauthorized al probar endpoints en Swagger
```
**Solución:**
1. Llamar `POST /api/auth/login` y copiar el token del response
2. En Swagger UI, hacer click en "Authorize" (candado)
3. Ingresar: `Bearer eyJhbGciOi...` (con el prefijo "Bearer ")

### LazyInitializationException
```
org.hibernate.LazyInitializationException: could not initialize proxy
```
**Causa:** Se accede a una relación lazy fuera de la transacción.
**Solución:**
```java
// Opción 1: @Transactional en el servicio
@Transactional
public MiDominio buscarConRelaciones(UUID id) { ... }

// Opción 2: usar JOIN FETCH en la query
@Query("SELECT e FROM MiEntity e JOIN FETCH e.relacion WHERE e.id = :id")
Optional<MiEntity> findByIdConRelacion(@Param("id") UUID id);
```

### DataIntegrityViolationException (constraint violado)
```
ERROR: duplicate key value violates unique constraint
ERROR: null value in column violates not-null constraint
```
**Causa:** La BD rechaza la operación por una restricción.
**Solución en el servicio:**
```java
// Validar antes de insertar
if (repository.existsByEmail(email)) {
    throw new IllegalArgumentException("El email ya está registrado");
}
```
**El `GlobalExceptionHandler` debe capturar esta excepción y retornar 409.**

### Puerto 8080 ya en uso
```
Web server failed to start. Port 8080 was already in use.
```
```powershell
# Encontrar el proceso
netstat -ano | findstr :8080
# Matar el proceso (reemplazar PID)
taskkill /PID 12345 /F
```

## 4. Agregar logs de depuración temporales

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MiServicio {
    private static final Logger log = LoggerFactory.getLogger(MiServicio.class);

    public void miMetodo(UUID id) {
        log.debug("Iniciando miMetodo con id={}", id);
        // ... lógica
        log.info("miMetodo completado para id={}", id);
    }
}
```

Activar logs DEBUG en `application.yml`:
```yaml
logging:
  level:
    com.dian.prep: DEBUG
    org.springframework.security: DEBUG   # para problemas de auth
    org.flywaydb: DEBUG                   # para problemas de migración
```

## 5. Verificar la BD desde pgAdmin

- URL: http://localhost:5050
- Credenciales: `admin@gmail.com` / `admin123`
- Servidor: `dian-postgres` (host: `postgres`, puerto: `5432`)
- Queries útiles:
```sql
-- Ver todas las tablas
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

-- Ver migraciones aplicadas
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Ver usuarios registrados
SELECT id, nombre, email, created_at FROM usuarios;

-- Ver ítems de un concurso
SELECT i.*, c.nombre as concurso FROM items i JOIN concursos c ON i.concurso_id = c.id;
```

## 6. Herramientas de diagnóstico rápido

```powershell
# Ver si el backend está levantado
Invoke-WebRequest -Uri http://localhost:8080/actuator/health -UseBasicParsing

# Ver los endpoints disponibles (requiere auth)
Invoke-WebRequest -Uri http://localhost:8080/v3/api-docs -UseBasicParsing

# Probar login desde terminal
$body = '{"email":"test@test.com","password":"password123"}'
Invoke-WebRequest -Uri http://localhost:8080/api/auth/login -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
```

## 7. Checklist de diagnóstico

- [ ] ¿Docker está corriendo? (`docker ps`)
- [ ] ¿La BD responde? (pgAdmin o `\conninfo` en psql)
- [ ] ¿Flyway aplicó todas las migraciones? (`flyway_schema_history`)
- [ ] ¿El JWT no expiró? (JWT expira en 24h por defecto)
- [ ] ¿El DTO tiene las anotaciones `@NotBlank`/`@Valid` correctas?
- [ ] ¿El controller usa `@RequestBody @Valid` en el parámetro?
- [ ] ¿La excepción está capturada en `GlobalExceptionHandler`?
