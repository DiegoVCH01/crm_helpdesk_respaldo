package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Empresa;

import java.util.List;
import java.util.Optional;

public interface EmpresaDAO {

    Optional<Empresa> findById(Long id);

    Optional<Empresa> findFirstByOrderByIdAsc();

    List<Empresa> findAll();

    Empresa save(Empresa empresa);
}