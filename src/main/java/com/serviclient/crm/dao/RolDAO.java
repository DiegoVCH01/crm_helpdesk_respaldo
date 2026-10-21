package com.serviclient.crm.dao;

import com.serviclient.crm.entity.RolEntity;

import java.util.List;
import java.util.Optional;

public interface RolDAO {

    List<RolEntity> findAll();

    RolEntity save(RolEntity rol);

    List<RolEntity> findByEmpresaId(Long empresaId);

    Optional<RolEntity> findByEmpresaIdAndNombre(Long empresaId, String nombre);
}