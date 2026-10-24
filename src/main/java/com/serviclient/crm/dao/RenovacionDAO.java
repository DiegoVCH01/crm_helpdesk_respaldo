package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.enums.EstadoRenovacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RenovacionDAO {
    Optional<Renovacion> findById(Long id);
    List<Renovacion> findByEmpresaId(Long empresaId);
    List<Renovacion> findByClienteId(Long clienteId);
    List<Renovacion> findByEmpresaIdOrderByFechaVencimientoAsc(Long empresaId);
    long countByEmpresaIdAndEstado(Long empresaId, EstadoRenovacion estado);
    long countRenovacionesEnRango(Long empresaId, LocalDate desde, LocalDate hasta);
    long countRenovacionesMasDeDias(Long empresaId, LocalDate fecha);
    long countClientesEnRiesgoRenovacionProxima(Long empresaId, LocalDate hasta);
    Renovacion save(Renovacion renovacion);
    List<Renovacion> findAll();
    void deleteById(Long id);
    void delete(Renovacion renovacion);
    List<Renovacion> buscarConFiltros(Long empresaId, String busqueda, EstadoRenovacion estado);
}