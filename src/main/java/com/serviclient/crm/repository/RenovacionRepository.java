package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RenovacionRepository extends JpaRepository<Renovacion, Long> {

    List<Renovacion> findByEmpresaId(Long empresaId);

    List<Renovacion> findByClienteId(Long clienteId);

    List<Renovacion> findByEmpresaIdOrderByFechaVencimientoAsc(Long empresaId);

    long countByEmpresaIdAndEstado(Long empresaId, EstadoRenovacion estado);

    @Query("SELECT COUNT(r) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.estado NOT IN " +
           "(com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO) " +
           "AND r.fechaVencimiento BETWEEN :desde AND :hasta")
    long countRenovacionesEnRango(@Param("empresaId") Long empresaId,
                                  @Param("desde") LocalDate desde,
                                  @Param("hasta") LocalDate hasta);

    @Query("SELECT COUNT(r) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.estado NOT IN " +
           "(com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO) " +
           "AND r.fechaVencimiento > :fecha")
    long countRenovacionesMasDeDias(@Param("empresaId") Long empresaId,
                                    @Param("fecha") LocalDate fecha);

    @Query("SELECT COUNT(DISTINCT r.cliente.id) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.fechaVencimiento <= :hasta " +
           "AND r.estado NOT IN (com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO)")
    long countClientesEnRiesgoRenovacionProxima(@Param("empresaId") Long empresaId,
                                               @Param("hasta") LocalDate hasta);

    @Query("SELECT r FROM Renovacion r WHERE r.empresa.id = :empresaId " +
           "AND (:busqueda IS NULL OR :busqueda = '' OR LOWER(r.cliente.nombreComercial) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
           "AND (:estado IS NULL OR r.estado = :estado) " +
           "ORDER BY r.fechaVencimiento ASC")
    List<Renovacion> buscarConFiltros(@Param("empresaId") Long empresaId,
                                      @Param("busqueda") String busqueda,
                                      @Param("estado") EstadoRenovacion estado);
}
