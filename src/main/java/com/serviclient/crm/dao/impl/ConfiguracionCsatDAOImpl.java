package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.ConfiguracionCsatDAO;
import com.serviclient.crm.entity.ConfiguracionCsat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class ConfiguracionCsatDAOImpl implements ConfiguracionCsatDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ConfiguracionCsat save(ConfiguracionCsat config) {
        if (config.getId() == null) {
            entityManager.persist(config);
            return config;
        } else {
            return entityManager.merge(config);
        }
    }

    @Override
    public Optional<ConfiguracionCsat> findByEmpresaId(Long empresaId) {
        try {
            ConfiguracionCsat result = entityManager.createQuery(
                    "SELECT c FROM ConfiguracionCsat c WHERE c.empresa.id = :empresaId", ConfiguracionCsat.class)
                    .setParameter("empresaId", empresaId)
                    .getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public long count() {
        Long total = entityManager.createQuery("SELECT COUNT(c) FROM ConfiguracionCsat c", Long.class)
                .getSingleResult();
        return total != null ? total : 0L;
    }
}