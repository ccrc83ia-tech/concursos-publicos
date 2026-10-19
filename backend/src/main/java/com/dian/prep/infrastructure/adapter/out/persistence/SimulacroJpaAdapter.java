package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.Pregunta.OpcionRespuesta;
import com.dian.prep.domain.model.Simulacro;
import com.dian.prep.domain.model.SimulacroPregunta;
import com.dian.prep.domain.port.out.SimulacroRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.SimulacroEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.SimulacroPreguntaEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.SimulacroJpaRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.SimulacroPreguntaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SimulacroJpaAdapter implements SimulacroRepository {

    private final SimulacroJpaRepository simulacroRepo;
    private final SimulacroPreguntaJpaRepository preguntaRepo;

    public SimulacroJpaAdapter(SimulacroJpaRepository simulacroRepo,
                               SimulacroPreguntaJpaRepository preguntaRepo) {
        this.simulacroRepo = simulacroRepo;
        this.preguntaRepo  = preguntaRepo;
    }

    @Override
    public Simulacro guardar(Simulacro simulacro) {
        SimulacroEntity e = toEntity(simulacro);
        return toDomain(simulacroRepo.save(e));
    }

    @Override
    public Optional<Simulacro> buscarPorId(UUID id) {
        return simulacroRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<SimulacroPregunta> buscarPreguntaPorId(UUID simulacroPreguntaId) {
        return preguntaRepo.findById(simulacroPreguntaId).map(this::toDomainPregunta);
    }

    @Override
    public SimulacroPregunta guardarPregunta(SimulacroPregunta pregunta) {
        SimulacroPreguntaEntity e = preguntaRepo.findById(pregunta.getId())
                .orElseThrow(() -> new IllegalArgumentException("Pregunta de simulacro no encontrada"));
        e.setRespuestaDada(pregunta.getRespuestaDada() != null ? pregunta.getRespuestaDada().name() : null);
        e.setEsCorrecta(pregunta.getEsCorrecta());
        e.setTiempoRespuestaSegundos(pregunta.getTiempoRespuestaSegundos());
        e.setMarcadaParaRevision(pregunta.isMarcadaParaRevision());
        return toDomainPregunta(preguntaRepo.save(e));
    }

    // --- Mappers ---

    private SimulacroEntity toEntity(Simulacro s) {
        SimulacroEntity e = new SimulacroEntity();
        if (s.getId() != null) e.setId(s.getId());
        e.setUsuarioId(s.getUsuarioId());
        e.setConcursoId(s.getConcursoId());
        e.setTipo(s.getTipo().name());
        e.setEstado(s.getEstado().name());
        e.setTiempoLimiteSegundos(s.getTiempoLimiteSegundos());
        e.setTiempoUsadoSegundos(s.getTiempoUsadoSegundos());
        e.setPuntajeFinal(s.getPuntajeFinal());
        if (s.getCreatedAt() != null) e.setCreatedAt(s.getCreatedAt());
        e.setFinalizadoAt(s.getFinalizadoAt());

        List<SimulacroPreguntaEntity> preguntas = new ArrayList<>();
        for (SimulacroPregunta sp : s.getPreguntas()) {
            SimulacroPreguntaEntity pe = new SimulacroPreguntaEntity();
            if (sp.getId() != null) pe.setId(sp.getId());
            pe.setSimulacro(e);
            pe.setPreguntaId(sp.getPreguntaId());
            pe.setItemId(sp.getItemId());
            pe.setRespuestaDada(sp.getRespuestaDada() != null ? sp.getRespuestaDada().name() : null);
            pe.setEsCorrecta(sp.getEsCorrecta());
            pe.setTiempoRespuestaSegundos(sp.getTiempoRespuestaSegundos());
            pe.setMarcadaParaRevision(sp.isMarcadaParaRevision());
            pe.setOrden(sp.getOrden());
            preguntas.add(pe);
        }
        e.setPreguntas(preguntas);
        return e;
    }

    private Simulacro toDomain(SimulacroEntity e) {
        Simulacro s = new Simulacro();
        s.setId(e.getId());
        s.setUsuarioId(e.getUsuarioId());
        s.setConcursoId(e.getConcursoId());
        s.setTipo(Simulacro.TipoSimulacro.valueOf(e.getTipo()));
        s.setEstado(Simulacro.EstadoSimulacro.valueOf(e.getEstado()));
        s.setTiempoLimiteSegundos(e.getTiempoLimiteSegundos());
        s.setTiempoUsadoSegundos(e.getTiempoUsadoSegundos());
        s.setPuntajeFinal(e.getPuntajeFinal());
        s.setCreatedAt(e.getCreatedAt());
        s.setFinalizadoAt(e.getFinalizadoAt());
        s.setPreguntas(e.getPreguntas().stream().map(this::toDomainPregunta).toList());
        return s;
    }

    private SimulacroPregunta toDomainPregunta(SimulacroPreguntaEntity e) {
        SimulacroPregunta sp = new SimulacroPregunta();
        sp.setId(e.getId());
        sp.setSimulacroId(e.getSimulacro() != null ? e.getSimulacro().getId() : null);
        sp.setPreguntaId(e.getPreguntaId());
        sp.setItemId(e.getItemId());
        if (e.getRespuestaDada() != null) {
            sp.setRespuestaDada(OpcionRespuesta.valueOf(e.getRespuestaDada()));
        }
        sp.setEsCorrecta(e.getEsCorrecta());
        sp.setTiempoRespuestaSegundos(e.getTiempoRespuestaSegundos());
        sp.setMarcadaParaRevision(e.isMarcadaParaRevision());
        sp.setOrden(e.getOrden());
        return sp;
    }
}
