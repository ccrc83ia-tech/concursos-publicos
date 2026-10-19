package com.dian.prep.domain.port.in;

import com.dian.prep.domain.model.Concurso;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RegistrarConcursoUseCase {

    record RegistrarConcursoCommand(
            UUID   usuarioId,
            String nombre,
            String procesoSeleccion,
            String numeroInscripcion,
            String numeroEmpleo,
            String nivelCargo,
            String denominacion,
            LocalDate fechaExamen
    ) {}

    Concurso registrar(RegistrarConcursoCommand command);

    List<Concurso> listarPorUsuario(UUID usuarioId);

    Concurso buscarPorId(UUID id, UUID usuarioId);

    void eliminar(UUID id, UUID usuarioId);
}
