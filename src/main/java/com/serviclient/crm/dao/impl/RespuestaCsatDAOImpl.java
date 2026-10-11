package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.RespuestaCsatDAO;
import com.serviclient.crm.entity.RespuestaCsat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class RespuestaCsatDAOImpl implements RespuestaCsatDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public RespuestaCsat save(RespuestaCsat respuesta) {
        if (respuesta.getId() == null) {
            entityManager.persist(respuesta);
            return respuesta;
        } else {
            return entityManager.merge(respuesta);
        }
    }

    @Override
    public Optional<RespuestaCsat> findByEncuestaId(Long encuestaId) {
        try {
            RespuestaCsat result = entityManager.createQuery("SELECT r FROM RespuestaCsat r WHERE r.encuesta.id = :encuestaId", RespuestaCsat.class)
                    .setParameter("encuestaId", encuestaId)
                    .getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}