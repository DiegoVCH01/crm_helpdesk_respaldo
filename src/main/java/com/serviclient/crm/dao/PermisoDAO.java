package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Permiso;
import java.util.List;

public interface PermisoDAO {

  List<Permiso> findAll();

  Permiso save(Permiso permiso);

  long count();
}
