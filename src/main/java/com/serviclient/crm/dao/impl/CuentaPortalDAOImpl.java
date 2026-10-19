package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.CuentaPortalDAO;
import com.serviclient.crm.entity.CuentaPortal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CuentaPortalDAOImpl implements CuentaPortalDAO {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Optional<CuentaPortal> findById(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(entityManager.find(CuentaPortal.class, id));
  }

  @Override
  public Optional<CuentaPortal> findByEmail(String email) {
    return entityManager
        .createQuery(
            """
            SELECT cp
            FROM CuentaPortal cp
            WHERE cp.email = :email
            """,
            CuentaPortal.class)
        .setParameter("email", email)
        .getResultStream()
        .findFirst();
  }

  @Override
  public Optional<CuentaPortal> findByContactoId(Long contactoId) {
    return entityManager
        .createQuery(
            """
            SELECT cp
            FROM CuentaPortal cp
            WHERE cp.contacto.id = :contactoId
            """,
            CuentaPortal.class)
        .setParameter("contactoId", contactoId)
        .getResultStream()
        .findFirst();
  }

  @Override
  @Transactional
  public CuentaPortal save(CuentaPortal cuentaPortal) {

    if (cuentaPortal.getId() == null) {
      entityManager.persist(cuentaPortal);
      return cuentaPortal;
    }

    return entityManager.merge(cuentaPortal);
  }
}
