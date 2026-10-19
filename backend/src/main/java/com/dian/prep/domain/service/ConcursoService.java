package com.dian.prep.domain.service;

import com.dian.prep.domain.model.Concurso;
import com.dian.prep.domain.port.in.RegistrarConcursoUseCase;
import com.dian.prep.domain.port.out.ConcursoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConcursoService implements RegistrarConcursoUseCase {

    private final ConcursoRepository concursoRepository;

    public ConcursoService(ConcursoRepository concursoRepository) {
        this.concursoRepository = concursoRepository;
    }

    @Override
    public Concurso registrar(RegistrarConcursoCommand cmd) {
        Concurso concurso = Concurso.crear(cmd.usuarioId(), cmd.nombre());
        concurso.setProcesoSeleccion(cmd.procesoSeleccion());
        concurso.setNumeroInscripcion(cmd.numeroInscripcion());
        concurso.setNumeroEmpleo(cmd.numeroEmpleo());
        concurso.setNivelCargo(cmd.nivelCargo());
        concurso.setDenominacion(cmd.denominacion());
        concurso.setFechaExamen(cmd.fechaExamen());
        return concursoRepository.guardar(concurso);
    }

    @Override
    public List<Concurso> listarPorUsuario(UUID usuarioId) {
        return concursoRepository.buscarPorUsuarioId(usuarioId);
    }

    @Override
    public Concurso buscarPorId(UUID id, UUID usuarioId) {
        Concurso concurso = concursoRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Concurso no encontrado: " + id));
        if (!concurso.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("No tiene permiso para acceder a este concurso");
        }
        return concurso;
    }

    @Override
    public void eliminar(UUID id, UUID usuarioId) {
        buscarPorId(id, usuarioId);
        concursoRepository.eliminar(id);
    }
}
