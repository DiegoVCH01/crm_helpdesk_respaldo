package com.serviclient.crm.repository;

import com.serviclient.crm.entity.ConfiguracionSla;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionSlaRepository extends JpaRepository<ConfiguracionSla, Long> {
    List<ConfiguracionSla> findByEmpresaId(Long empresaId);
    Optional<ConfiguracionSla> findByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad);
}
