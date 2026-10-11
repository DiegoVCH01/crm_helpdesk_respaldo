package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.EncuestaSatisfaccionDAO;
import com.serviclient.crm.entity.EncuestaSatisfaccion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class EncuestaSatisfaccionDAOImpl implements EncuestaSatisfaccionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public EncuestaSatisfaccion save(EncuestaSatisfaccion encuesta) {
        if (encuesta.getId() == null) {
            entityManager.persist(encuesta);
            return encuesta;
        } else {
            return entityManager.merge(encuesta);
        }
    }

    @Override
    public List<EncuestaSatisfaccion> findByClienteId(Long clienteId) {
        return entityManager.createQuery(
                "SELECT e FROM EncuestaSatisfaccion e WHERE e.cliente.id = :clienteId", EncuestaSatisfaccion.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }

    @Override
    public List<EncuestaSatisfaccion> findByTicketId(Long ticketId) {
        return entityManager.createQuery(
                "SELECT e FROM EncuestaSatisfaccion e WHERE e.ticket.id = :ticketId", EncuestaSatisfaccion.class)
                .setParameter("ticketId", ticketId)
                .getResultList();
    }

    @Override
    public Double obtenerPromedioCsatPorCliente(Long clienteId) {
        Double avg = entityManager.createQuery(
                "SELECT AVG(e.puntuacion) FROM EncuestaSatisfaccion e WHERE e.cliente.id = :clienteId", Double.class)
                .setParameter("clienteId", clienteId)
                .getSingleResult();
        return avg != null ? avg : 0.0;
    }

    @Override
    public Double obtenerPromedioCsatPorEmpresa(Long empresaId) {
        Double avg = entityManager.createQuery(
                "SELECT AVG(e.puntuacion) FROM EncuestaSatisfaccion e WHERE e.cliente.empresa.id = :empresaId", Double.class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();
        return avg != null ? avg : 0.0;
    }
}