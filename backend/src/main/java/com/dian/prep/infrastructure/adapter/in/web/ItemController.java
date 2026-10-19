package com.dian.prep.infrastructure.adapter.in.web;

import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.port.in.GestionarItemsUseCase;
import com.dian.prep.infrastructure.adapter.in.web.dto.ItemRequest;
import com.dian.prep.infrastructure.adapter.in.web.dto.ItemResponse;
import com.dian.prep.infrastructure.adapter.in.web.dto.ParsarIndicadoresRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/concursos/{concursoId}/items")
@Tag(name = "Ítems", description = "Gestión de indicadores por concurso")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final GestionarItemsUseCase gestionarItemsUseCase;

    public ItemController(GestionarItemsUseCase gestionarItemsUseCase) {
        this.gestionarItemsUseCase = gestionarItemsUseCase;
    }

    @PostMapping
    @Operation(summary = "Crear un ítem manualmente")
    public ResponseEntity<ItemResponse> crear(
            @PathVariable UUID concursoId,
            @Valid @RequestBody ItemRequest request) {

        Item item = gestionarItemsUseCase.crear(toCommand(concursoId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.from(item));
    }

    @PostMapping("/parsear")
    @Operation(summary = "Parsear texto libre con indicadores y crear todos los ítems automáticamente")
    public ResponseEntity<List<ItemResponse>> parsear(
            @PathVariable UUID concursoId,
            @Valid @RequestBody ParsarIndicadoresRequest request) {

        List<Item> items = gestionarItemsUseCase.parsarYCrear(
                new GestionarItemsUseCase.ParsarIndicadoresCommand(
                        concursoId, request.textoIndicadores()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(items.stream().map(ItemResponse::from).toList());
    }

    @GetMapping
    @Operation(summary = "Listar ítems de un concurso")
    public ResponseEntity<List<ItemResponse>> listar(@PathVariable UUID concursoId) {
        return ResponseEntity.ok(
                gestionarItemsUseCase.listarPorConcurso(concursoId)
                        .stream().map(ItemResponse::from).toList());
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Actualizar un ítem (cantidad de preguntas, dominio previo, etc.)")
    public ResponseEntity<ItemResponse> actualizar(
            @PathVariable UUID concursoId,
            @PathVariable UUID itemId,
            @Valid @RequestBody ItemRequest request) {

        Item item = gestionarItemsUseCase.actualizar(itemId, toCommand(concursoId, request));
        return ResponseEntity.ok(ItemResponse.from(item));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Eliminar un ítem")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID concursoId,
            @PathVariable UUID itemId) {
        gestionarItemsUseCase.eliminar(itemId);
        return ResponseEntity.noContent().build();
    }

    private GestionarItemsUseCase.CrearItemCommand toCommand(UUID concursoId, ItemRequest r) {
        return new GestionarItemsUseCase.CrearItemCommand(
                concursoId,
                r.nombre(),
                r.descripcion(),
                r.tipoPrueba(),
                r.nivelCompetencia(),
                r.pesoPorcentual(),
                r.preguntasSolicitadas(),
                r.dominioPrevio()
        );
    }
}
