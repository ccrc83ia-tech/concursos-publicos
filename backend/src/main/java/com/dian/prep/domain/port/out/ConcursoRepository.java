package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Concurso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConcursoRepository {

    Concurso guardar(Concurso concurso);

    Optional<Concurso> buscarPorId(UUID id);

    List<Concurso> buscarPorUsuarioId(UUID usuarioId);

    void eliminar(UUID id);

    boolean existePorIdYUsuarioId(UUID id, UUID usuarioId);
}
