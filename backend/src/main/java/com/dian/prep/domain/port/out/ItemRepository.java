package com.dian.prep.domain.port.out;

import com.dian.prep.domain.model.Item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

    Item guardar(Item item);

    List<Item> guardarTodos(List<Item> items);

    Optional<Item> buscarPorId(UUID id);

    List<Item> buscarPorConcursoId(UUID concursoId);

    /** Todos los ítems del sistema (lo usa el job de mantenimiento del banco). */
    List<Item> buscarTodos();

    void eliminar(UUID id);
}
