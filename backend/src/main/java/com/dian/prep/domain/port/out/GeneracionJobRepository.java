package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.GeneracionJob;

import java.util.Optional;
import java.util.UUID;

public interface GeneracionJobRepository {

    GeneracionJob guardar(GeneracionJob job);

    Optional<GeneracionJob> buscarPorId(UUID id);

    /** Último job de generación de un ítem (el más reciente). */
    Optional<GeneracionJob> buscarUltimoPorItemId(UUID itemId);
}
