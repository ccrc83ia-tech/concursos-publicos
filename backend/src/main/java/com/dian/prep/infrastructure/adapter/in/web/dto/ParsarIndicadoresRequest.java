package com.dian.prep.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

/**
 * Petición para parsear indicadores en lote.
 *
 * <p>El perfil de clasificación es opcional: si no se envía, el sistema usa el
 * perfil por defecto (estructura típica CNSC). Los campos del perfil permiten
 * adaptar el parseo a cualquier concurso del SIMO.</p>
 */
public record ParsarIndicadoresRequest(
    @NotBlank(message = "El texto de indicadores es obligatorio")
    String textoIndicadores,

    // --- Perfil de clasificación opcional ---
    Set<String> palabrasIntegridad,          // p. ej. ["COMPROMISO","HONESTIDAD",...]
    String      palabraComportamental,       // palabra que marca un indicador como comportamental (default "NIVEL")
    Double      umbralFuncional,             // % mínimo de aprobación funcional (default 70)
    Double      umbralComportamentalIntegridad // % mínimo comportamental/integridad (default 60)
) {}
