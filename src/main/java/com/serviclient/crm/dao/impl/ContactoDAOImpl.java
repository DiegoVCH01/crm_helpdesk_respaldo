package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.ContactoDAO;
import com.serviclient.crm.entity.Contacto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ContactoDAOImpl implements ContactoDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Contacto> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(Contacto.class, id));
    }

    @Override
    public List<Contacto> findAll() {
        return entityManager.createQuery("SELECT c FROM Contacto c", Contacto.class).getResultList();
    }

    @Override
    public List<Contacto> findByClienteId(Long clienteId) {
        return entityManager.createQuery(
                "SELECT c FROM Contacto c WHERE c.cliente.id = :clienteId", Contacto.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }

    @Override
    @Transactional
    public Contacto save(Contacto contacto) {
        if (contacto.getId() == null) {
            entityManager.persist(contacto);
            return contacto;
        } else {
            return entityManager.merge(contacto);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(Contacto contacto) {
        if (contacto != null) {
            Contacto toRemove = entityManager.contains(contacto) ? contacto : entityManager.merge(contacto);
            entityManager.remove(toRemove);
        }
    }
}