package com.serviclient.crm.dao;

import com.serviclient.crm.entity.CuentaPortal;
import java.util.Optional;

public interface CuentaPortalDAO {

  Optional<CuentaPortal> findById(Long id);

  Optional<CuentaPortal> findByEmail(String email);

  Optional<CuentaPortal> findByContactoId(Long contactoId);

  CuentaPortal save(CuentaPortal cuentaPortal);
}
