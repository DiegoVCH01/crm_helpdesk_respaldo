package com.serviclient.crm.dao;

import com.serviclient.crm.entity.ConfiguracionHealthScore;
import java.util.Optional;

public interface ConfiguracionHealthScoreDAO {
    ConfiguracionHealthScore save(ConfiguracionHealthScore config);
    Optional<ConfiguracionHealthScore> findByEmpresaId(Long empresaId);
    long count();
}