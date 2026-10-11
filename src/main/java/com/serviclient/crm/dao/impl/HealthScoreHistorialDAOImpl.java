package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.HealthScoreHistorialDAO;
import com.serviclient.crm.entity.HealthScoreHistorial;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class HealthScoreHistorialDAOImpl implements HealthScoreHistorialDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public HealthScoreHistorial save(HealthScoreHistorial historial) {
        if (historial.getId() == null) {
            entityManager.persist(historial);
            return historial;
        } else {
            return entityManager.merge(historial);
        }
    }

    @Override
    public List<HealthScoreHistorial> findByClienteIdOrderByFechaCalculoDesc(Long clienteId) {
        return entityManager.createQuery(
                "SELECT h FROM HealthScoreHistorial h WHERE h.cliente.id = :clienteId ORDER BY h.fechaCalculo DESC", HealthScoreHistorial.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }

    @Override
    public Optional<HealthScoreHistorial> findFirstByClienteIdOrderByFechaCalculoDesc(Long clienteId) {
        try {
            HealthScoreHistorial result = entityManager.createQuery(
                    "SELECT h FROM HealthScoreHistorial h WHERE h.cliente.id = :clienteId ORDER BY h.fechaCalculo DESC", HealthScoreHistorial.class)
                    .setParameter("clienteId", clienteId)
                    .setMaxResults(1) // Limitamos a 1 resultado para simular "findFirst"
                    .getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}