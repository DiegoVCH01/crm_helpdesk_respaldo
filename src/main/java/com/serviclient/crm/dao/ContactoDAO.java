package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Contacto;

import java.util.List;
import java.util.Optional;

public interface ContactoDAO {
    Optional<Contacto> findById(Long id);
    List<Contacto> findAll();
    List<Contacto> findByClienteId(Long clienteId);
    Contacto save(Contacto contacto);
    void deleteById(Long id);
    void delete(Contacto contacto);
}