package com.dian.prep.infrastructure.adapter.in.web;

import com.dian.prep.domain.port.in.GenerarRecomendacionesUseCase;
import com.dian.prep.infrastructure.adapter.in.web.dto.RecomendacionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recomendaciones")
@Tag(name = "Recomendaciones", description = "Plan de estudio con IA a partir de las brechas")
@SecurityRequirement(name = "bearerAuth")
public class RecomendacionController {

    private final GenerarRecomendacionesUseCase useCase;

    public RecomendacionController(GenerarRecomendacionesUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/simulacro/{simulacroId}")
    @Operation(summary = "Generar (con IA) recomendaciones de estudio para las brechas de un simulacro")
    public ResponseEntity<List<RecomendacionResponse>> generar(@PathVariable UUID simulacroId,
                                                               Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        List<RecomendacionResponse> body = useCase.generarParaSimulacro(simulacroId, usuarioId)
                .stream().map(RecomendacionResponse::from).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/concurso/{concursoId}")
    @Operation(summary = "Plan de estudio consolidado: última recomendación por ítem del concurso")
    public ResponseEntity<List<RecomendacionResponse>> listarPorConcurso(@PathVariable UUID concursoId,
                                                                         Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(useCase.listarPorConcurso(concursoId, usuarioId)
                .stream().map(RecomendacionResponse::from).toList());
    }
}
