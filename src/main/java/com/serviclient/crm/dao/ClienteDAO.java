package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteDAO {
  Optional<Cliente> findById(Long id);
  Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);
  List<Cliente> findByEmpresaId(Long empresaId);
  long countByEmpresaId(Long empresaId);
  Page<Cliente> buscarConFiltros(Long empresaId, String busqueda, Pageable pageable);
  Cliente save(Cliente cliente);
  List<Cliente> findAll();
  void deleteById(Long id);
  void delete(Cliente cliente);
  long count();
}
