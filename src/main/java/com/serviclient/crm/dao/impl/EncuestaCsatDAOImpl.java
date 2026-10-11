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

@Repository // Obligatorio según la guía de tu equipo
public class EncuestaCsatDAOImpl implements EncuestaCsatDAO {

    @PersistenceContext // Inyección de dependencia para controlar la DB
    private EntityManager entityManager;

    @Override
    @Transactional // Solo en métodos que modifican datos (escritura)
    public EncuestaCsat save(EncuestaCsat encuestaCsat) {
        if (encuestaCsat.getId() == null) {
            entityManager.persist(encuestaCsat); // INSERT
            return encuestaCsat;
        } else {
            return entityManager.merge(encuestaCsat); // UPDATE
        }
    }

    @Override
    public List<EncuestaCsat> findByEmpresaId(Long empresaId) {
        // e.empresa.id navega por las entidades Java, no por columnas SQL
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
            // getSingleResult() lanza un error si no encuentra nada. Lo capturamos y devolvems un Optional vacío.
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
}