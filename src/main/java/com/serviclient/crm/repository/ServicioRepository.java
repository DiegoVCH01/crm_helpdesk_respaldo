package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByEmpresaId(Long empresaId);
    Optional<Servicio> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
    Optional<Servicio> findByCodigo(String codigo);
}
