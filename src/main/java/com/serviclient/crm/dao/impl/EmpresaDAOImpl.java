package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.EmpresaDAO;
import com.serviclient.crm.entity.Empresa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EmpresaDAOImpl implements EmpresaDAO {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<Empresa> findById(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(entityManager.find(Empresa.class, id));
  }

  @Override
  public Optional<Empresa> findFirstByOrderByIdAsc() {
    TypedQuery<Empresa> query = entityManager.createQuery("SELECT e FROM Empresa e ORDER BY e.id ASC", Empresa.class);

    query.setMaxResults(1);

    return query.getResultStream().findFirst();
  }

  @Override
  public List<Empresa> findAll() {
    return entityManager.createQuery("SELECT e FROM Empresa e", Empresa.class).getResultList();
  }

  @Override
  @Transactional
  public Empresa save(Empresa empresa) {
    if (empresa.getId() == null) {
      entityManager.persist(empresa);
      return empresa;
    }

    return entityManager.merge(empresa);
  }

  @Override
  public Optional<Empresa> findByRuc(String ruc) {
    List<Empresa> result = entityManager.createQuery(
        "SELECT e FROM Empresa e WHERE e.ruc = :ruc", Empresa.class)
        .setParameter("ruc", ruc)
        .setMaxResults(1)
        .getResultList();
    return result.stream().findFirst();
  }
}
