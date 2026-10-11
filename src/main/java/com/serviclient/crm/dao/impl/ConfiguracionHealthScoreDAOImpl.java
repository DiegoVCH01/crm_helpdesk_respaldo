package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.ConfiguracionHealthScoreDAO;
import com.serviclient.crm.entity.ConfiguracionHealthScore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class ConfiguracionHealthScoreDAOImpl implements ConfiguracionHealthScoreDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ConfiguracionHealthScore save(ConfiguracionHealthScore config) {
        if (config.getId() == null) {
            entityManager.persist(config);
            return config;
        } else {
            return entityManager.merge(config);
        }
    }

    @Override
    public Optional<ConfiguracionHealthScore> findByEmpresaId(Long empresaId) {
        try {
            ConfiguracionHealthScore result = entityManager.createQuery(
                    "SELECT c FROM ConfiguracionHealthScore c WHERE c.empresa.id = :empresaId", ConfiguracionHealthScore.class)
                    .setParameter("empresaId", empresaId)
                    .getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public long count() {
        Long total = entityManager.createQuery("SELECT COUNT(c) FROM ConfiguracionHealthScore c", Long.class)
                .getSingleResult();
        return total != null ? total : 0L;
    }
}