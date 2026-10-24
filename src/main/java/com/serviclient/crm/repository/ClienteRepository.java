package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Cliente> findByEmpresaIdAndRuc(Long empresaId, String ruc);

    List<Cliente> findByEmpresaId(Long empresaId);

    long countByEmpresaId(Long empresaId);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND " +
           "(:busqueda IS NULL OR :busqueda = '' OR " +
           "LOWER(c.nombreComercial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.ruc) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.responsable.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.responsable.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Cliente> buscarConFiltros(@Param("empresaId") Long empresaId,
                                   @Param("busqueda") String busqueda,
                                   Pageable pageable);
}
