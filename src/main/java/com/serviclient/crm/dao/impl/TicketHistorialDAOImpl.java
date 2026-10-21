package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.TicketHistorialDAO;
import com.serviclient.crm.entity.TicketHistorial;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketHistorialDAOImpl implements TicketHistorialDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TicketHistorial> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(TicketHistorial.class, id));
    }

    @Override
    public List<TicketHistorial> findByTicketIdOrderByCreatedAtDesc(Long ticketId) {
        TypedQuery<TicketHistorial> query = entityManager.createQuery(
                "SELECT h FROM TicketHistorial h WHERE h.ticket.id = :ticketId ORDER BY h.createdAt DESC", TicketHistorial.class);
        query.setParameter("ticketId", ticketId);
        return query.getResultList();
    }

    @Override
    public List<TicketHistorial> findByTicketIdOrderByFechaCreacionDesc(Long ticketId) {
        return entityManager.createQuery(
        "SELECT h FROM TicketHistorial h WHERE h.ticket.id = :ticketId ORDER BY h.createdAt DESC", TicketHistorial.class)
        .setParameter("ticketId", ticketId).getResultList();
}

    @Override
    @Transactional
    public TicketHistorial save(TicketHistorial historial) {
        if (historial.getId() == null) {
            entityManager.persist(historial);
            return historial;
        } else {
            return entityManager.merge(historial);
        }
    }

    @Override
    public List<TicketHistorial> findAll() {
        return entityManager.createQuery("SELECT h FROM TicketHistorial h", TicketHistorial.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(TicketHistorial historial) {
        if (historial != null) {
            TicketHistorial toRemove = entityManager.contains(historial) ? historial : entityManager.merge(historial);
            entityManager.remove(toRemove);
        }
    }
}
