package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.RolDAO;
import com.serviclient.crm.entity.RolEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RolDAOImpl implements RolDAO {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<RolEntity> findAll() {
    return entityManager.createQuery("SELECT r FROM RolEntity r", RolEntity.class).getResultList();
  }

  @Override
  @Transactional
  public RolEntity save(RolEntity rol) {
    if (rol.getId() == null) {
      entityManager.persist(rol);
      return rol;
    }

    return entityManager.merge(rol);
  }

  @Override
  public Optional<RolEntity> findByNombre(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      return Optional.empty();
    }

    return entityManager
        .createQuery(
            "SELECT r FROM RolEntity r WHERE r.nombre = :nombre",
            RolEntity.class)
        .setParameter("nombre", nombre)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }
}
