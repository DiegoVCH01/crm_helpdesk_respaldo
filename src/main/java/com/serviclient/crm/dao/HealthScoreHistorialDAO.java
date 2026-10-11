package com.serviclient.crm.dao;

import com.serviclient.crm.entity.HealthScoreHistorial;
import java.util.List;
import java.util.Optional;

public interface HealthScoreHistorialDAO {
    HealthScoreHistorial save(HealthScoreHistorial historial);
    List<HealthScoreHistorial> findByClienteIdOrderByFechaCalculoDesc(Long clienteId);
    Optional<HealthScoreHistorial> findFirstByClienteIdOrderByFechaCalculoDesc(Long clienteId);
}