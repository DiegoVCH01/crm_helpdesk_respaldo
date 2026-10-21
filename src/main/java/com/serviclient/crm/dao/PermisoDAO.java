package com.serviclient.crm.dao;

import java.util.Optional;
import com.serviclient.crm.entity.Permiso;
import java.util.List;

public interface PermisoDAO {

  List<Permiso> findAll();

  Permiso save(Permiso permiso);

  long count();

  Optional<Permiso> findByNombre(String nombre);

  List<Permiso> findByModulo(String modulo);
}
