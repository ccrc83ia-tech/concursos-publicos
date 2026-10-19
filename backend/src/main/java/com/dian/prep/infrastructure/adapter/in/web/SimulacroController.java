package com.dian.prep.infrastructure.adapter.in.web;

import com.dian.prep.domain.model.Simulacro;
import com.dian.prep.domain.port.in.GestionarSimulacrosUseCase;
import com.dian.prep.domain.port.in.GestionarSimulacrosUseCase.*;
import com.dian.prep.infrastructure.adapter.in.web.dto.*;
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
@RequestMapping("/api/v1/simulacros")
@Tag(name = "Simulacros", description = "Simulacros cronometrados y desempeño por ítem")
@SecurityRequirement(name = "bearerAuth")
public class SimulacroController {

    private final GestionarSimulacrosUseCase useCase;

    public SimulacroController(GestionarSimulacrosUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Crear un simulacro con preguntas aleatorias")
    public ResponseEntity<SimulacroResponse> crear(@Valid @RequestBody CrearSimulacroRequest req,
                                                   Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        Simulacro.TipoSimulacro tipo = parseTipo(req.tipo());

        Simulacro s = useCase.crear(new CrearSimulacroCommand(
                usuarioId, req.concursoId(), tipo, req.itemIds(),
                req.preguntasPorItem(), req.tiempoLimiteSegundos()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SimulacroResponse.from(s, useCase.contenidoPreguntas(s.getId(), usuarioId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un simulacro con sus preguntas")
    public ResponseEntity<SimulacroResponse> obtener(@PathVariable UUID id, Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        Simulacro s = useCase.obtener(id, usuarioId);
        return ResponseEntity.ok(SimulacroResponse.from(s, useCase.contenidoPreguntas(id, usuarioId)));
    }

    @PostMapping("/{id}/responder")
    @Operation(summary = "Registrar la respuesta a una pregunta del simulacro")
    public ResponseEntity<Void> responder(@PathVariable UUID id,
                                          @Valid @RequestBody ResponderRequest req,
                                          Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        useCase.responder(id, usuarioId, new RespuestaCommand(
                req.simulacroPreguntaId(), req.respuesta(),
                req.marcadaParaRevision(), req.tiempoRespuestaSegundos()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar el simulacro y calcular el puntaje y desempeño")
    public ResponseEntity<SimulacroResponse> finalizar(@PathVariable UUID id,
                                                       @RequestParam(defaultValue = "0") int tiempoUsadoSegundos,
                                                       Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        Simulacro s = useCase.finalizar(id, usuarioId, tiempoUsadoSegundos);
        return ResponseEntity.ok(SimulacroResponse.from(s, useCase.contenidoPreguntas(id, usuarioId)));
    }

    @GetMapping("/{id}/desempeno")
    @Operation(summary = "Desempeño por ítem del simulacro")
    public ResponseEntity<List<DesempenoResponse>> desempeno(@PathVariable UUID id, Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(useCase.desempeno(id, usuarioId)
                .stream().map(DesempenoResponse::from).toList());
    }

    @GetMapping("/brechas/{concursoId}")
    @Operation(summary = "Dashboard de brechas: último desempeño por ítem del concurso")
    public ResponseEntity<List<DesempenoResponse>> brechas(@PathVariable UUID concursoId, Authentication auth) {
        UUID usuarioId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(useCase.brechasPorConcurso(concursoId, usuarioId)
                .stream().map(DesempenoResponse::from).toList());
    }

    private Simulacro.TipoSimulacro parseTipo(String v) {
        if (v == null || v.isBlank()) return Simulacro.TipoSimulacro.POR_ITEM;
        try {
            return Simulacro.TipoSimulacro.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Simulacro.TipoSimulacro.POR_ITEM;
        }
    }
}
