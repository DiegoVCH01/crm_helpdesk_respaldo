package com.serviclient.crm.dao;

import com.serviclient.crm.entity.ClienteServicio;

import java.util.List;
import java.util.Optional;

public interface ClienteServicioDAO {
    Optional<ClienteServicio> findById(Long id);
    List<ClienteServicio> findAll();
    List<ClienteServicio> findByClienteId(Long clienteId);
    ClienteServicio save(ClienteServicio clienteServicio);
    void deleteById(Long id);
    void delete(ClienteServicio clienteServicio);
}