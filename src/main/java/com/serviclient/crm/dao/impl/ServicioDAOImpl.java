package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.ServicioDAO;
import com.serviclient.crm.entity.Servicio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ServicioDAOImpl implements ServicioDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Servicio> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(Servicio.class, id));
    }

    @Override
    public List<Servicio> findAll() {
        return entityManager.createQuery("SELECT s FROM Servicio s", Servicio.class).getResultList();
    }

    @Override
    @Transactional
    public Servicio save(Servicio servicio) {
        if (servicio.getId() == null) {
            entityManager.persist(servicio);
            return servicio;
        } else {
            return entityManager.merge(servicio);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(Servicio servicio) {
        if (servicio != null) {
            Servicio toRemove = entityManager.contains(servicio) ? servicio : entityManager.merge(servicio);
            entityManager.remove(toRemove);
        }
    }

    @Override
    public List<Servicio> findByEmpresaId(Long empresaId) {
        return entityManager.createQuery(
                "SELECT s FROM Servicio s WHERE s.empresa.id = :empresaId", Servicio.class)
                .setParameter("empresaId", empresaId)
                .getResultList();
    }

    @Override
    public Optional<Servicio> findByEmpresaIdAndCodigo(Long empresaId, String codigo) {
        List<Servicio> resultados = entityManager.createQuery(
                "SELECT s FROM Servicio s WHERE s.empresa.id = :empresaId AND s.codigo = :codigo", Servicio.class)
                .setParameter("empresaId", empresaId)
                .setParameter("codigo", codigo)
                .getResultList();
        return resultados.stream().findFirst();
    }

    @Override
    public Optional<Servicio> findByCodigo(String codigo) {
        List<Servicio> resultados = entityManager.createQuery(
                "SELECT s FROM Servicio s WHERE s.codigo = :codigo", Servicio.class)
                .setParameter("codigo", codigo)
                .getResultList();
        return resultados.stream().findFirst();
    }
}