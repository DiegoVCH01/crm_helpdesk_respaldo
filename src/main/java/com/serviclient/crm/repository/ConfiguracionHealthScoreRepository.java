package com.serviclient.crm.repository;

import com.serviclient.crm.entity.ConfiguracionHealthScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionHealthScoreRepository extends JpaRepository<ConfiguracionHealthScore, Long> {
    Optional<ConfiguracionHealthScore> findByEmpresaId(Long empresaId);
}
