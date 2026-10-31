package com.dian.prep.infrastructure.adapter.out.persistence.repository;

import com.dian.prep.infrastructure.adapter.out.persistence.entity.JobMantenimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobMantenimientoJpaRepository extends JpaRepository<JobMantenimientoEntity, String> {
}
