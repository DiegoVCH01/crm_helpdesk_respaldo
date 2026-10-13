package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.RenovacionSeguimientoDAO;
import com.serviclient.crm.entity.RenovacionSeguimiento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class RenovacionSeguimientoDAOImpl implements RenovacionSeguimientoDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<RenovacionSeguimiento> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(RenovacionSeguimiento.class, id));
    }

    @Override
    public List<RenovacionSeguimiento> findByRenovacionIdOrderByFechaDesc(Long renovacionId) {
        TypedQuery<RenovacionSeguimiento> query = entityManager.createQuery(
                "SELECT s FROM RenovacionSeguimiento s WHERE s.renovacion.id = :renovacionId ORDER BY s.fecha DESC",
                RenovacionSeguimiento.class);
        query.setParameter("renovacionId", renovacionId);
        return query.getResultList();
    }

    @Override
    @Transactional
    public RenovacionSeguimiento save(RenovacionSeguimiento seguimiento) {
        if (seguimiento.getId() == null) {
            entityManager.persist(seguimiento);
            return seguimiento;
        } else {
            return entityManager.merge(seguimiento);
        }
    }

    @Override
    public List<RenovacionSeguimiento> findAll() {
        return entityManager.createQuery("SELECT s FROM RenovacionSeguimiento s", RenovacionSeguimiento.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(RenovacionSeguimiento seguimiento) {
        if (seguimiento != null) {
            RenovacionSeguimiento toRemove = entityManager.contains(seguimiento) ? seguimiento : entityManager.merge(seguimiento);
            entityManager.remove(toRemove);
        }
    }
}