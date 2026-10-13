package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.InteraccionDAO;
import com.serviclient.crm.entity.Interaccion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class InteraccionDAOImpl implements InteraccionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Interaccion> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(Interaccion.class, id));
    }

    @Override
    public List<Interaccion> findAll() {
        return entityManager.createQuery("SELECT i FROM Interaccion i", Interaccion.class).getResultList();
    }

    @Override
    public List<Interaccion> findByClienteId(Long clienteId) {
        return entityManager.createQuery(
                "SELECT i FROM Interaccion i WHERE i.cliente.id = :clienteId", Interaccion.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }

    @Override
    @Transactional
    public Interaccion save(Interaccion interaccion) {
        if (interaccion.getId() == null) {
            entityManager.persist(interaccion);
            return interaccion;
        } else {
            return entityManager.merge(interaccion);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(Interaccion interaccion) {
        if (interaccion != null) {
            Interaccion toRemove = entityManager.contains(interaccion) ? interaccion : entityManager.merge(interaccion);
            entityManager.remove(toRemove);
        }
    }
}