package com.dian.prep.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ConcursoRequest(
    @NotBlank(message = "El nombre del concurso es obligatorio")
    @Size(max = 200)
    String nombre,

    @Size(max = 50)
    String procesoSeleccion,

    @Size(max = 50)
    String numeroInscripcion,

    @Size(max = 50)
    String numeroEmpleo,

    @Size(max = 50)
    String nivelCargo,

    @Size(max = 100)
    String denominacion,

    LocalDate fechaExamen
) {}
