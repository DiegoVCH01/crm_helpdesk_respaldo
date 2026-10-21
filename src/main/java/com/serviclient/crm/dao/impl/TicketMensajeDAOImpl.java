package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.TicketMensajeDAO;
import com.serviclient.crm.entity.TicketMensaje;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class TicketMensajeDAOImpl implements TicketMensajeDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TicketMensaje> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(TicketMensaje.class, id));
    }

    @Override
    public List<TicketMensaje> findByTicketIdOrderByCreatedAtAsc(Long ticketId) {
        TypedQuery<TicketMensaje> query = entityManager.createQuery(
                "SELECT m FROM TicketMensaje m WHERE m.ticket.id = :ticketId ORDER BY m.createdAt ASC", TicketMensaje.class);
        query.setParameter("ticketId", ticketId);
        return query.getResultList();
    }

    @Override
    public List<TicketMensaje> findByTicketIdOrderByFechaCreacionAsc(Long ticketId) {
        return entityManager.createQuery(
            "SELECT m FROM TicketMensaje m WHERE m.ticket.id = :ticketId ORDER BY m.createdAt ASC", TicketMensaje.class)
            .setParameter("ticketId", ticketId).getResultList();
    }

    @Override
    @Transactional
    public TicketMensaje save(TicketMensaje mensaje) {
        if (mensaje.getId() == null) {
            entityManager.persist(mensaje);
            return mensaje;
        } else {
            return entityManager.merge(mensaje);
        }
    }

    @Override
    public List<TicketMensaje> findAll() {
        return entityManager.createQuery("SELECT m FROM TicketMensaje m", TicketMensaje.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(TicketMensaje mensaje) {
        if (mensaje != null) {
            TicketMensaje toRemove = entityManager.contains(mensaje) ? mensaje : entityManager.merge(mensaje);
            entityManager.remove(toRemove);
        }
    }
}
