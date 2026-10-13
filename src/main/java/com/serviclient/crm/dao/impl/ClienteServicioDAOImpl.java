package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.ClienteServicioDAO;
import com.serviclient.crm.entity.ClienteServicio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ClienteServicioDAOImpl implements ClienteServicioDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ClienteServicio> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(ClienteServicio.class, id));
    }

    @Override
    public List<ClienteServicio> findAll() {
        return entityManager.createQuery("SELECT cs FROM ClienteServicio cs", ClienteServicio.class).getResultList();
    }

    @Override
    public List<ClienteServicio> findByClienteId(Long clienteId) {
        return entityManager.createQuery(
                "SELECT cs FROM ClienteServicio cs WHERE cs.cliente.id = :clienteId", ClienteServicio.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }

    @Override
    @Transactional
    public ClienteServicio save(ClienteServicio clienteServicio) {
        if (clienteServicio.getId() == null) {
            entityManager.persist(clienteServicio);
            return clienteServicio;
        } else {
            return entityManager.merge(clienteServicio);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(ClienteServicio clienteServicio) {
        if (clienteServicio != null) {
            ClienteServicio toRemove = entityManager.contains(clienteServicio) ? clienteServicio : entityManager.merge(clienteServicio);
            entityManager.remove(toRemove);
        }
    }
}