package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Servicio;

import java.util.List;
import java.util.Optional;

public interface ServicioDAO {
    Optional<Servicio> findById(Long id);
    List<Servicio> findAll();
    Servicio save(Servicio servicio);
    void deleteById(Long id);
    void delete(Servicio servicio);
    List<Servicio> findByEmpresaId(Long empresaId);
    Optional<Servicio> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    Optional<Servicio> findByCodigo(String codigo);
}