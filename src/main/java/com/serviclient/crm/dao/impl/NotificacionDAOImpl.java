package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.NotificacionDAO;
import com.serviclient.crm.entity.Notificacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NotificacionDAOImpl implements NotificacionDAO {

  @PersistenceContext private EntityManager entityManager;

  @Override
  @Transactional
  public Notificacion save(Notificacion notificacion) {
    if (notificacion.getId() == null) {
      entityManager.persist(notificacion);
      return notificacion;
    }

    return entityManager.merge(notificacion);
  }
}
