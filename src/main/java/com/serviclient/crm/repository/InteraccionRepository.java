package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Interaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteraccionRepository extends JpaRepository<Interaccion, Long> {
    List<Interaccion> findByClienteIdOrderByFechaInteraccionDesc(Long clienteId);
}
