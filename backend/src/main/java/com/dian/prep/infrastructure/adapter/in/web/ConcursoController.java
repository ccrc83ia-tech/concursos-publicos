package com.dian.prep.infrastructure.adapter.in.web;

import com.dian.prep.domain.model.Concurso;
import com.dian.prep.domain.port.in.RegistrarConcursoUseCase;
import com.dian.prep.infrastructure.adapter.in.web.dto.ConcursoRequest;
import com.dian.prep.infrastructure.adapter.in.web.dto.ConcursoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/concursos")
@Tag(name = "Concursos", description = "Gestión de concursos de méritos")
@SecurityRequirement(name = "bearerAuth")
public class ConcursoController {

    private final RegistrarConcursoUseCase registrarConcursoUseCase;

    public ConcursoController(RegistrarConcursoUseCase registrarConcursoUseCase) {
        this.registrarConcursoUseCase = registrarConcursoUseCase;
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo concurso")
    public ResponseEntity<ConcursoResponse> crear(
            @Valid @RequestBody ConcursoRequest request,
            Authentication auth) {

        UUID usuarioId = (UUID) auth.getPrincipal();
        Concurso concurso = registrarConcursoUseCase.registrar(
                new RegistrarConcursoUseCase.RegistrarConcursoCommand(
                        usuarioId,
                        request.nombre(),
                        request.procesoSeleccion(),
                        request.numeroInscripcion(),
                        request.numeroEmpleo(),
                        request.nivelCargo(),
                        request.denominacion(),
                        request.fechaExamen()
                ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ConcursoResponse.from(concurso));
    }

    @GetMapping
    @Operation(summary = "Listar concursos del usuario autenticado")
    public ResponseEntity<List<ConcursoResponse>> listar(Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        List<ConcursoResponse> respuesta = registrarConcursoUseCase
                .listarPorUsuario(usuarioId)
                .stream()
                .map(ConcursoResponse::from)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener concurso por ID")
    public ResponseEntity<ConcursoResponse> obtener(
            @PathVariable UUID id,
            Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        Concurso concurso = registrarConcursoUseCase.buscarPorId(id, usuarioId);
        return ResponseEntity.ok(ConcursoResponse.from(concurso));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar concurso")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        registrarConcursoUseCase.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
