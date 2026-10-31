package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.port.out.CupoIaRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.CupoIaDiarioJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class CupoIaJpaAdapter implements CupoIaRepository {

    private final CupoIaDiarioJpaRepository repo;

    public CupoIaJpaAdapter(CupoIaDiarioJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public int requestsUsadas(LocalDate fecha, String modelo) {
        return repo.findByFechaAndModelo(fecha, modelo)
                .map(e -> e.getRequestsUsadas())
                .orElse(0);
    }

    @Override
    @Transactional
    public int registrarUso(LocalDate fecha, String modelo, int cantidad) {
        if (cantidad <= 0) return requestsUsadas(fecha, modelo);
        repo.upsertSuma(fecha, modelo, cantidad);
        return requestsUsadas(fecha, modelo);
    }
}
