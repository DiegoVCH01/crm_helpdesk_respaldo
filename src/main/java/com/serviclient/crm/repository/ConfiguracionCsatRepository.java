package com.serviclient.crm.repository;

import com.serviclient.crm.entity.ConfiguracionCsat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionCsatRepository extends JpaRepository<ConfiguracionCsat, Long> {
    Optional<ConfiguracionCsat> findByEmpresaId(Long empresaId);
}
