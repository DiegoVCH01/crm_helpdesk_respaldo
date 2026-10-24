package com.serviclient.crm.repository;

import com.serviclient.crm.entity.ClienteServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteServicioRepository extends JpaRepository<ClienteServicio, Long> {
    List<ClienteServicio> findByClienteId(Long clienteId);
}
