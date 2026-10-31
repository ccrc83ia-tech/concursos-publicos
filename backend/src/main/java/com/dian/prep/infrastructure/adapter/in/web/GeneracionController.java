package com.dian.prep.infrastructure.adapter.in.web;

import com.dian.prep.domain.port.in.GenerarPreguntasUseCase;
import com.dian.prep.domain.service.BancoPreguntasService;
import com.dian.prep.infrastructure.adapter.in.web.dto.BancoEstadoResponse;
import com.dian.prep.infrastructure.adapter.in.web.dto.GeneracionJobResponse;
import com.dian.prep.infrastructure.adapter.in.web.dto.PreguntaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items/{itemId}")
@Tag(name = "Generación IA", description = "Generación de preguntas con IA por ítem")
@SecurityRequirement(name = "bearerAuth")
public class GeneracionController {

    private final GenerarPreguntasUseCase generarPreguntasUseCase;
    private final BancoPreguntasService bancoPreguntas;

    public GeneracionController(GenerarPreguntasUseCase generarPreguntasUseCase,
                                BancoPreguntasService bancoPreguntas) {
        this.generarPreguntasUseCase = generarPreguntasUseCase;
        this.bancoPreguntas          = bancoPreguntas;
    }

    @PostMapping("/generar")
    @Operation(summary = "Inicia la generación asíncrona de preguntas para el ítem. "
            + "Si se envía 'cantidad', prevalece sobre la registrada en el ítem.")
    public ResponseEntity<GeneracionJobResponse> generar(
            @PathVariable UUID itemId,
            @RequestParam(required = false) Integer cantidad) {
        var job = generarPreguntasUseCase.iniciarGeneracion(itemId, cantidad);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(GeneracionJobResponse.from(job));
    }

    @GetMapping("/generacion/estado")
    @Operation(summary = "Consulta el progreso de la generación del ítem")
    public ResponseEntity<GeneracionJobResponse> estado(@PathVariable UUID itemId) {
        return ResponseEntity.ok(GeneracionJobResponse.from(
                generarPreguntasUseCase.consultarEstado(itemId)));
    }

    @GetMapping("/preguntas")
    @Operation(summary = "Lista las preguntas generadas del ítem")
    public ResponseEntity<List<PreguntaResponse>> preguntas(@PathVariable UUID itemId) {
        return ResponseEntity.ok(generarPreguntasUseCase.listarPreguntas(itemId)
                .stream().map(PreguntaResponse::from).toList());
    }

    @GetMapping("/banco/estado")
    @Operation(summary = "Estado del banco del ítem: conteos por estado de calidad y por origen de modelo")
    public ResponseEntity<BancoEstadoResponse> estadoBanco(@PathVariable UUID itemId) {
        return ResponseEntity.ok(BancoEstadoResponse.from(bancoPreguntas.estadoBanco(itemId)));
    }
}
