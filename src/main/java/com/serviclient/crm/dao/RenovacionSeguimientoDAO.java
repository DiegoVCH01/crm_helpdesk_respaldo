package com.serviclient.crm.dao;

import com.serviclient.crm.entity.RenovacionSeguimiento;

import java.util.List;
import java.util.Optional;

public interface RenovacionSeguimientoDAO {
    Optional<RenovacionSeguimiento> findById(Long id);
    List<RenovacionSeguimiento> findByRenovacionIdOrderByFechaDesc(Long renovacionId);
    RenovacionSeguimiento save(RenovacionSeguimiento seguimiento);
    List<RenovacionSeguimiento> findAll();
    void deleteById(Long id);
    void delete(RenovacionSeguimiento seguimiento);
}