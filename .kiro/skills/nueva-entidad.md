---
name: nueva-entidad
description: Guía paso a paso para agregar una nueva entidad al proyecto siguiendo arquitectura hexagonal
---

# Skill: Agregar Nueva Entidad

Cuando el usuario pida agregar una entidad nueva (ej: Pregunta, Simulacro, Resultado), sigue este orden exacto:

## Orden de creación

### 1. Modelo de dominio
`domain/model/NombreEntidad.java`
- Clase Java pura, sin anotaciones Spring ni JPA
- Incluir factory method estático `crear(...)`
- Incluir enums internos si aplica
- Getters y setters manuales (sin Lombok en dominio)

### 2. Puerto de salida
`domain/port/out/NombreEntidadRepository.java`
- Interface con métodos: `guardar`, `buscarPorId`, `buscarPor...`, `eliminar`
- Usa tipos del dominio, nunca entidades JPA

### 3. Puerto de entrada (caso de uso)
`domain/port/in/GestionarNombreEntidadUseCase.java`
- Interface con Records para Commands
- Métodos: crear, listar, buscar, actualizar, eliminar según necesidad

### 4. Servicio de dominio
`domain/service/NombreEntidadService.java`
- Implementa el UseCase
- Inyecta solo interfaces de `port/out`
- Anotación `@Service`

### 5. Migración Flyway
`resources/db/migration/V{N}__crear_tabla_nombre.sql`
- Siguiente número de versión disponible
- Incluir comentarios en columnas importantes
- Usar `gen_random_uuid()` para IDs
- Crear índices para columnas de búsqueda frecuente

### 6. Entidad JPA
`infrastructure/adapter/out/persistence/entity/NombreEntidadEntity.java`
- `@Entity`, `@Table(name = "nombre_tabla")`
- `@PreUpdate` para actualizar `updatedAt`
- Getters y setters manuales

### 7. Repositorio JPA
`infrastructure/adapter/out/persistence/repository/NombreEntidadJpaRepository.java`
- Extiende `JpaRepository<NombreEntidadEntity, UUID>`
- Métodos de consulta con convención Spring Data

### 8. Adaptador JPA
`infrastructure/adapter/out/persistence/NombreEntidadJpaAdapter.java`
- Implementa `NombreEntidadRepository` (puerto de salida)
- `@Component`
- Métodos privados `toEntity()` y `toDomain()` para mapeo

### 9. DTOs
`infrastructure/adapter/in/web/dto/NombreEntidadRequest.java`
`infrastructure/adapter/in/web/dto/NombreEntidadResponse.java`
- Request: validaciones `@NotBlank`, `@Size`, etc.
- Response: Record con método estático `from(NombreEntidad entidad)`

### 10. Controller REST
`infrastructure/adapter/in/web/NombreEntidadController.java`
- `@RestController`
- `@RequestMapping("/api/v1/nombre-entidades")`
- `@SecurityRequirement(name = "bearerAuth")`
- Inyecta solo el UseCase (interfaz, no implementación)
- Extrae `UUID usuarioId` de `Authentication auth`

## Checklist de validación
- [ ] El dominio no importa nada de infrastructure
- [ ] El controller no accede directamente al repositorio
- [ ] Los DTOs tienen validaciones apropiadas
- [ ] La migración Flyway tiene el número de versión correcto
- [ ] El adaptador implementa el puerto de salida
