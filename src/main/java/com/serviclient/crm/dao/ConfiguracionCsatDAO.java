package com.serviclient.crm.dao;

import com.serviclient.crm.entity.ConfiguracionCsat;
import java.util.Optional;

public interface ConfiguracionCsatDAO {
    ConfiguracionCsat save(ConfiguracionCsat config);
    Optional<ConfiguracionCsat> findByEmpresaId(Long empresaId);
    long count();
}