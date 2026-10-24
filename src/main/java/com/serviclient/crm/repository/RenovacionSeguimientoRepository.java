package com.serviclient.crm.repository;

import com.serviclient.crm.entity.RenovacionSeguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RenovacionSeguimientoRepository extends JpaRepository<RenovacionSeguimiento, Long> {
    List<RenovacionSeguimiento> findByRenovacionIdOrderByFechaDesc(Long renovacionId);
}
