package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.DesempenoItem;
import com.dian.prep.domain.model.DesempenoItem.NivelDominio;
import com.dian.prep.domain.port.out.DesempenoRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.DesempenoItemEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.DesempenoItemJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DesempenoJpaAdapter implements DesempenoRepository {

    private final DesempenoItemJpaRepository repo;

    public DesempenoJpaAdapter(DesempenoItemJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<DesempenoItem> guardarTodos(List<DesempenoItem> desempenos) {
        List<DesempenoItemEntity> entities = desempenos.stream().map(this::toEntity).toList();
        return repo.saveAll(entities).stream().map(this::toDomain).toList();
    }

    @Override
    public List<DesempenoItem> buscarPorSimulacroId(UUID simulacroId) {
        return repo.findBySimulacroId(simulacroId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<DesempenoItem> buscarUltimoPorConcurso(UUID concursoId, UUID usuarioId) {
        return repo.findUltimoPorConcurso(concursoId, usuarioId).stream().map(this::toDomain).toList();
    }

    private DesempenoItemEntity toEntity(DesempenoItem d) {
        DesempenoItemEntity e = new DesempenoItemEntity();
        if (d.getId() != null) e.setId(d.getId());
        e.setUsuarioId(d.getUsuarioId());
        e.setItemId(d.getItemId());
        e.setSimulacroId(d.getSimulacroId());
        e.setPreguntasCorrectas(d.getPreguntasCorrectas());
        e.setTotalPreguntas(d.getTotalPreguntas());
        e.setPuntajePorcentual(d.getPuntajePorcentual());
        e.setNivelDominio(d.getNivelDominio().name());
        if (d.getCreatedAt() != null) e.setCreatedAt(d.getCreatedAt());
        return e;
    }

    private DesempenoItem toDomain(DesempenoItemEntity e) {
        DesempenoItem d = new DesempenoItem();
        d.setId(e.getId());
        d.setUsuarioId(e.getUsuarioId());
        d.setItemId(e.getItemId());
        d.setSimulacroId(e.getSimulacroId());
        d.setPreguntasCorrectas(e.getPreguntasCorrectas());
        d.setTotalPreguntas(e.getTotalPreguntas());
        d.setPuntajePorcentual(e.getPuntajePorcentual());
        d.setNivelDominio(NivelDominio.valueOf(e.getNivelDominio()));
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }
}
