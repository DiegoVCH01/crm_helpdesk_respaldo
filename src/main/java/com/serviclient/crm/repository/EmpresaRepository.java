package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findFirstByOrderByIdAsc();
    Optional<Empresa> findByRuc(String ruc);
}
