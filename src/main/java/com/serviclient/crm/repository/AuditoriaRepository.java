package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);
}
