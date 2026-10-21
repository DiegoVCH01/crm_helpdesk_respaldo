package com.serviclient.crm.dao;

import com.serviclient.crm.entity.ConfiguracionSla;
import com.serviclient.crm.entity.enums.PrioridadTicket;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionSlaDAO {
    Optional<ConfiguracionSla> findById(Long id);
    List<ConfiguracionSla> findByEmpresaId(Long empresaId);
    Optional<ConfiguracionSla> findByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad);
    ConfiguracionSla save(ConfiguracionSla configuracionSla);
    List<ConfiguracionSla> findAll();
    void deleteById(Long id);
    void delete(ConfiguracionSla configuracionSla);
    long count();
}
