package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.AuditoriaDAO;
import com.serviclient.crm.entity.Auditoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AuditoriaDAOImpl implements AuditoriaDAO {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<Auditoria> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId) {
    TypedQuery<Auditoria> query =
        entityManager.createQuery(
            """
            SELECT a
            FROM Auditoria a
            WHERE a.empresa.id = :empresaId
            ORDER BY a.createdAt DESC
            """,
            Auditoria.class);

    query.setParameter("empresaId", empresaId);

    return query.getResultList();
  }

  @Override
  @Transactional
  public Auditoria save(Auditoria auditoria) {
    if (auditoria.getId() == null) {
      entityManager.persist(auditoria);
      return auditoria;
    }

    return entityManager.merge(auditoria);
  }
}
