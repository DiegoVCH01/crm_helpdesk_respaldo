package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.EncuestaCsatDAO;
import com.serviclient.crm.entity.EncuestaCsat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository 
public class EncuestaCsatDAOImpl implements EncuestaCsatDAO {

    @PersistenceContext 
    private EntityManager entityManager;

    @Override
    @Transactional 
    public EncuestaCsat save(EncuestaCsat encuestaCsat) {
        if (encuestaCsat.getId() == null) {
            entityManager.persist(encuestaCsat); 
            return encuestaCsat;
        } else {
            return entityManager.merge(encuestaCsat);
        }
    }

    @Override
    public List<EncuestaCsat> findByEmpresaId(Long empresaId) {
        return entityManager.createQuery("SELECT e FROM EncuestaCsat e WHERE e.empresa.id = :empresaId", EncuestaCsat.class)
                .setParameter("empresaId", empresaId)
                .getResultList();
    }

    @Override
    public List<EncuestaCsat> findByClienteId(Long clienteId) {
        return entityManager.createQuery("SELECT e FROM EncuestaCsat e WHERE e.cliente.id = :clienteId", EncuestaCsat.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }

    @Override
    public Optional<EncuestaCsat> findByTicketId(Long ticketId) {
        try {
            EncuestaCsat result = entityManager.createQuery("SELECT e FROM EncuestaCsat e WHERE e.ticket.id = :ticketId", EncuestaCsat.class)
                    .setParameter("ticketId", ticketId)
                    .getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty(); 
        }
    }

    @Override
    public Double findAverageScoreByClienteId(Long clienteId) {
        Double avg = entityManager.createQuery("SELECT AVG(r.puntuacion) FROM RespuestaCsat r WHERE r.encuesta.cliente.id = :clienteId", Double.class)
                .setParameter("clienteId", clienteId)
                .getSingleResult();
        return avg != null ? avg : 0.0;
    }

    @Override
    public Double findAverageScoreByEmpresaId(Long empresaId) {
        Double avg = entityManager.createQuery("SELECT AVG(r.puntuacion) FROM RespuestaCsat r WHERE r.encuesta.empresa.id = :empresaId", Double.class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();
        return avg != null ? avg : 0.0;
    }

    @Override
    public boolean existsByTicketId(Long ticketId) {
        Long count = entityManager.createQuery(
                "SELECT COUNT(e) FROM EncuestaCsat e WHERE e.ticket.id = :ticketId", Long.class)
                .setParameter("ticketId", ticketId)
                .getSingleResult();
        return count > 0;
    }

}