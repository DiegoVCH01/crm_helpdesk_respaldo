package com.serviclient.crm.repository;

import com.serviclient.crm.entity.HealthScoreHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthScoreHistorialRepository extends JpaRepository<HealthScoreHistorial, Long> {
    List<HealthScoreHistorial> findByClienteIdOrderByFechaCalculoDesc(Long clienteId);
    Optional<HealthScoreHistorial> findFirstByClienteIdOrderByFechaCalculoDesc(Long clienteId);
}
