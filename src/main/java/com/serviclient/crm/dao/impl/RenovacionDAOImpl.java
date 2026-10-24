package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.RenovacionDAO;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class RenovacionDAOImpl implements RenovacionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Renovacion> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(Renovacion.class, id));
    }

    @Override
    public List<Renovacion> findByEmpresaId(Long empresaId) {
        TypedQuery<Renovacion> query = entityManager.createQuery(
                "SELECT r FROM Renovacion r WHERE r.empresa.id = :empresaId", Renovacion.class);
        query.setParameter("empresaId", empresaId);
        return query.getResultList();
    }

    @Override
    public List<Renovacion> findByClienteId(Long clienteId) {
        TypedQuery<Renovacion> query = entityManager.createQuery(
                "SELECT r FROM Renovacion r WHERE r.cliente.id = :clienteId", Renovacion.class);
        query.setParameter("clienteId", clienteId);
        return query.getResultList();
    }

    @Override
    public List<Renovacion> findByEmpresaIdOrderByFechaVencimientoAsc(Long empresaId) {
        TypedQuery<Renovacion> query = entityManager.createQuery(
                "SELECT r FROM Renovacion r WHERE r.empresa.id = :empresaId ORDER BY r.fechaVencimiento ASC", Renovacion.class);
        query.setParameter("empresaId", empresaId);
        return query.getResultList();
    }

    @Override
    public long countByEmpresaIdAndEstado(Long empresaId, EstadoRenovacion estado) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.estado = :estado", Long.class);
        query.setParameter("empresaId", empresaId);
        query.setParameter("estado", estado);
        Long count = query.getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public long countRenovacionesEnRango(Long empresaId, LocalDate desde, LocalDate hasta) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.estado NOT IN " +
                "(com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO) " +
                "AND r.fechaVencimiento BETWEEN :desde AND :hasta", Long.class);
        query.setParameter("empresaId", empresaId);
        query.setParameter("desde", desde);
        query.setParameter("hasta", hasta);
        Long count = query.getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public long countRenovacionesMasDeDias(Long empresaId, LocalDate fecha) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.estado NOT IN " +
                "(com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO) " +
                "AND r.fechaVencimiento > :fecha", Long.class);
        query.setParameter("empresaId", empresaId);
        query.setParameter("fecha", fecha);
        Long count = query.getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    public long countClientesEnRiesgoRenovacionProxima(Long empresaId, LocalDate hasta) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(DISTINCT r.cliente.id) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.fechaVencimiento <= :hasta " +
                "AND r.estado NOT IN (com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO)", Long.class);
        query.setParameter("empresaId", empresaId);
        query.setParameter("hasta", hasta);
        Long count = query.getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public Renovacion save(Renovacion renovacion) {
        if (renovacion.getId() == null) {
            entityManager.persist(renovacion);
            return renovacion;
        } else {
            return entityManager.merge(renovacion);
        }
    }

    @Override
    public List<Renovacion> findAll() {
        return entityManager.createQuery("SELECT r FROM Renovacion r", Renovacion.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(Renovacion renovacion) {
        if (renovacion != null) {
            Renovacion toRemove = entityManager.contains(renovacion) ? renovacion : entityManager.merge(renovacion);
            entityManager.remove(toRemove);
        }
    }

    @Override
    public List<Renovacion> buscarConFiltros(Long empresaId, String busqueda, EstadoRenovacion estado) {
        StringBuilder jpql = new StringBuilder("SELECT r FROM Renovacion r WHERE r.empresa.id = :empresaId");

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            jpql.append(" AND LOWER(r.cliente.nombreComercial) LIKE LOWER(:busqueda)");
        }
        if (estado != null) {
            jpql.append(" AND r.estado = :estado");
        }
        jpql.append(" ORDER BY r.fechaVencimiento ASC");

        TypedQuery<Renovacion> query = entityManager.createQuery(jpql.toString(), Renovacion.class);
        query.setParameter("empresaId", empresaId);

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            query.setParameter("busqueda", "%" + busqueda.trim() + "%");
        }
        if (estado != null) {
            query.setParameter("estado", estado);
        }

        return query.getResultList();
    }
}