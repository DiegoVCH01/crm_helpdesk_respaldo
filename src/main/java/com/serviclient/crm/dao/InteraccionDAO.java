package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Interaccion;

import java.util.List;
import java.util.Optional;

public interface InteraccionDAO {
    Optional<Interaccion> findById(Long id);
    List<Interaccion> findAll();
    List<Interaccion> findByClienteId(Long clienteId);
    Interaccion save(Interaccion interaccion);
    void deleteById(Long id);
    void delete(Interaccion interaccion);
}