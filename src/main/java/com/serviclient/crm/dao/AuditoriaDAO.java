package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Auditoria;
import java.util.List;

public interface AuditoriaDAO {

  List<Auditoria> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);

  Auditoria save(Auditoria auditoria);
}
