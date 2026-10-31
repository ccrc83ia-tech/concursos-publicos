package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.port.out.JobMantenimientoRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.JobMantenimientoEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.JobMantenimientoJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class JobMantenimientoJpaAdapter implements JobMantenimientoRepository {

    private final JobMantenimientoJpaRepository repo;

    public JobMantenimientoJpaAdapter(JobMantenimientoJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<LocalDate> ultimaCorrida(String jobId) {
        return repo.findById(jobId).map(JobMantenimientoEntity::getUltimaCorrida);
    }

    @Override
    public void marcarCorrida(String jobId, LocalDate fecha, String resumen) {
        JobMantenimientoEntity e = repo.findById(jobId).orElseGet(JobMantenimientoEntity::new);
        e.setId(jobId);
        e.setUltimaCorrida(fecha);
        e.setResumen(resumen);
        e.setUpdatedAt(LocalDateTime.now());
        repo.save(e);
    }
}
