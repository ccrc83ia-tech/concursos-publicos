package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.port.out.ItemRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.ConcursoEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.ItemEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.ConcursoJpaRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.ItemJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ItemJpaAdapter implements ItemRepository {

    private final ItemJpaRepository    itemJpaRepository;
    private final ConcursoJpaRepository concursoJpaRepository;

    public ItemJpaAdapter(ItemJpaRepository itemJpaRepository,
                          ConcursoJpaRepository concursoJpaRepository) {
        this.itemJpaRepository    = itemJpaRepository;
        this.concursoJpaRepository = concursoJpaRepository;
    }

    @Override
    public Item guardar(Item item) {
        ItemEntity entity = toEntity(item);
        return toDomain(itemJpaRepository.save(entity));
    }

    @Override
    public List<Item> guardarTodos(List<Item> items) {
        List<ItemEntity> entities = items.stream().map(this::toEntity).toList();
        return itemJpaRepository.saveAll(entities).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Item> buscarPorId(UUID id) {
        return itemJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Item> buscarPorConcursoId(UUID concursoId) {
        return itemJpaRepository.findByConcurso_IdOrderByOrdenAsc(concursoId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void eliminar(UUID id) {
        itemJpaRepository.deleteById(id);
    }

    // --- Mappers ---

    private ItemEntity toEntity(Item item) {
        ItemEntity e = new ItemEntity();
        if (item.getId() != null) e.setId(item.getId());

        ConcursoEntity concursoRef = concursoJpaRepository.getReferenceById(item.getConcursoId());
        e.setConcurso(concursoRef);

        e.setNombre(item.getNombre());
        e.setDescripcion(item.getDescripcion());
        e.setTipoPrueba(item.getTipoPrueba().name());
        e.setNivelCompetencia(item.getNivelCompetencia());
        e.setPesoPorcentual(item.getPesoPorcentual());
        e.setPreguntasSolicitadas(item.getPreguntasSolicitadas());
        e.setDominioPrevio(item.getDominioPrevio().name());
        e.setUmbralBrecha(item.getUmbralBrecha());
        e.setOrden(item.getOrden() != null ? item.getOrden() : 0);
        if (item.getCreatedAt() != null) e.setCreatedAt(item.getCreatedAt());
        return e;
    }

    private Item toDomain(ItemEntity e) {
        Item item = new Item();
        item.setId(e.getId());
        item.setConcursoId(e.getConcurso().getId());
        item.setNombre(e.getNombre());
        item.setDescripcion(e.getDescripcion());
        item.setTipoPrueba(Item.TipoPrueba.valueOf(e.getTipoPrueba()));
        item.setNivelCompetencia(e.getNivelCompetencia());
        item.setPesoPorcentual(e.getPesoPorcentual());
        item.setPreguntasSolicitadas(e.getPreguntasSolicitadas());
        item.setDominioPrevio(Item.DominioPrevio.valueOf(e.getDominioPrevio()));
        item.setUmbralBrecha(e.getUmbralBrecha());
        item.setOrden(e.getOrden());
        item.setCreatedAt(e.getCreatedAt());
        return item;
    }
}
