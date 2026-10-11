package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.PermisoDAO;
import com.serviclient.crm.entity.Permiso;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PermisoDAOImpl implements PermisoDAO {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<Permiso> findAll() {
    return entityManager.createQuery("SELECT p FROM Permiso p", Permiso.class).getResultList();
  }

  @Override
  @Transactional
  public Permiso save(Permiso permiso) {
    if (permiso.getId() == null) {
      entityManager.persist(permiso);
      return permiso;
    }

    return entityManager.merge(permiso);
  }

  @Override
  public long count() {
    TypedQuery<Long> query =
        entityManager.createQuery("SELECT COUNT(p) FROM Permiso p", Long.class);

    Long total = query.getSingleResult();
    return total != null ? total : 0L;
  }
}
