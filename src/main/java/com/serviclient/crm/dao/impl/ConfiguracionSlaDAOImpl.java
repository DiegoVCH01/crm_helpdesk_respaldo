package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.ConfiguracionSlaDAO;
import com.serviclient.crm.entity.ConfiguracionSla;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ConfiguracionSlaDAOImpl implements ConfiguracionSlaDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ConfiguracionSla> findById(Long id) {
        if (id == null)
            return Optional.empty();
        return Optional.ofNullable(entityManager.find(ConfiguracionSla.class, id));
    }

    @Override
    public List<ConfiguracionSla> findByEmpresaId(Long empresaId) {
        TypedQuery<ConfiguracionSla> query = entityManager.createQuery(
                "SELECT c FROM ConfiguracionSla c WHERE c.empresa.id = :empresaId", ConfiguracionSla.class);
        query.setParameter("empresaId", empresaId);
        return query.getResultList();
    }

    @Override
    public Optional<ConfiguracionSla> findByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad) {
        TypedQuery<ConfiguracionSla> query = entityManager.createQuery(
                "SELECT c FROM ConfiguracionSla c WHERE c.empresa.id = :empresaId AND c.prioridad = :prioridad",
                ConfiguracionSla.class);
        query.setParameter("empresaId", empresaId);
        query.setParameter("prioridad", prioridad);
        return query.getResultStream().findFirst();
    }

    @Override
    @Transactional
    public ConfiguracionSla save(ConfiguracionSla configuracionSla) {
        if (configuracionSla.getId() == null) {
            entityManager.persist(configuracionSla);
            return configuracionSla;
        } else {
            return entityManager.merge(configuracionSla);
        }
    }

    @Override
    public List<ConfiguracionSla> findAll() {
        return entityManager.createQuery("SELECT c FROM ConfiguracionSla c", ConfiguracionSla.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(ConfiguracionSla configuracionSla) {
        if (configuracionSla != null) {
            ConfiguracionSla toRemove = entityManager.contains(configuracionSla) ? configuracionSla
                    : entityManager.merge(configuracionSla);
            entityManager.remove(toRemove);
        }
    }

    @Override
    public long count() {
        Long total = entityManager.createQuery("SELECT COUNT(c) FROM ConfiguracionSla c", Long.class).getSingleResult();
        return total != null ? total : 0L;
    }
}
