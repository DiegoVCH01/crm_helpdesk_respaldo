package com.serviclient.crm.dao.impl;

import java.util.List;
import com.serviclient.crm.dao.NotificacionDAO;
import com.serviclient.crm.entity.Notificacion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NotificacionDAOImpl implements NotificacionDAO {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @Transactional
  public Notificacion save(Notificacion notificacion) {
    if (notificacion.getId() == null) {
      entityManager.persist(notificacion);
      return notificacion;
    }

    return entityManager.merge(notificacion);
  }

  @Override
  public List<Notificacion> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId) {
    return entityManager.createQuery(
        "SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId ORDER BY n.createdAt DESC", Notificacion.class)
        .setParameter("usuarioId", usuarioId)
        .getResultList();
  }

  @Override
  public List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByCreatedAtDesc(Long usuarioId) {
    return entityManager.createQuery(
        "SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leida = false ORDER BY n.createdAt DESC",
        Notificacion.class)
        .setParameter("usuarioId", usuarioId)
        .getResultList();
  }

  @Override
  public long countByUsuarioIdAndLeidaFalse(Long usuarioId) {
    Long count = entityManager.createQuery(
        "SELECT COUNT(n) FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leida = false", Long.class)
        .setParameter("usuarioId", usuarioId)
        .getSingleResult();
    return count != null ? count : 0L;
  }
}
