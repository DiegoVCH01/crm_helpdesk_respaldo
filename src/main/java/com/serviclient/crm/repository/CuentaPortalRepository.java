package com.serviclient.crm.repository;

import com.serviclient.crm.entity.CuentaPortal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaPortalRepository extends JpaRepository<CuentaPortal, Long> {
    Optional<CuentaPortal> findByEmail(String email);
    Optional<CuentaPortal> findByContactoId(Long contactoId);
}
