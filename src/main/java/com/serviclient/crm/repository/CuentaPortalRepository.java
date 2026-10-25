package com.serviclient.crm.repository;

import com.serviclient.crm.entity.CuentaPortal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaPortalRepository extends JpaRepository<CuentaPortal, Long> {

    // Búsqueda directa por JPQL para no depender de nombres de convención
    @Query("SELECT c FROM CuentaPortal c WHERE LOWER(c.email) = LOWER(:email)")
    Optional<CuentaPortal> findByEmail(@Param("email") String email);

    Optional<CuentaPortal> findByContactoId(Long contactoId);
}