package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.ClienteDAO;
import com.serviclient.crm.entity.Cliente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ClienteDAOImpl implements ClienteDAO {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Optional<Cliente> findById(Long id) {
    if (id == null) return Optional.empty();
    return Optional.ofNullable(entityManager.find(Cliente.class, id));
  }

  @Override
  public Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId) {
    if (id == null || empresaId == null) {
      return Optional.empty();
    }

    List<Cliente> resultados =
        entityManager
            .createQuery(
                "SELECT c FROM Cliente c " + "WHERE c.id = :id AND c.empresa.id = :empresaId",
                Cliente.class)
            .setParameter("id", id)
            .setParameter("empresaId", empresaId)
            .setMaxResults(1)
            .getResultList();

    return resultados.stream().findFirst();
  }

  @Override
  public List<Cliente> findByEmpresaId(Long empresaId) {
    TypedQuery<Cliente> query =
        entityManager.createQuery(
            "SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId", Cliente.class);
    query.setParameter("empresaId", empresaId);
    return query.getResultList();
  }

  @Override
  public long countByEmpresaId(Long empresaId) {
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(c) FROM Cliente c WHERE c.empresa.id = :empresaId", Long.class);
    query.setParameter("empresaId", empresaId);
    Long count = query.getSingleResult();
    return count != null ? count : 0L;
  }

  @Override
  public Page<Cliente> buscarConFiltros(Long empresaId, String busqueda, Pageable pageable) {
    String baseWhere =
        " WHERE c.empresa.id = :empresaId AND "
            + "(:busqueda IS NULL OR :busqueda = '' OR "
            + "LOWER(c.nombreComercial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR "
            + "LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR "
            + "LOWER(c.ruc) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR "
            + "LOWER(c.responsable.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR "
            + "LOWER(c.responsable.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')))";

    String countJpql = "SELECT COUNT(c) FROM Cliente c" + baseWhere;
    TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
    countQuery.setParameter("empresaId", empresaId);
    countQuery.setParameter("busqueda", busqueda);
    Long total = countQuery.getSingleResult();
    long totalElements = total != null ? total : 0L;

    String selectJpql = "SELECT c FROM Cliente c" + baseWhere;
    TypedQuery<Cliente> query = entityManager.createQuery(selectJpql, Cliente.class);
    query.setParameter("empresaId", empresaId);
    query.setParameter("busqueda", busqueda);

    if (pageable != null && pageable.isPaged()) {
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
    }

    List<Cliente> content = query.getResultList();
    return new PageImpl<>(content, pageable != null ? pageable : Pageable.unpaged(), totalElements);
  }

  @Override
  @Transactional
  public Cliente save(Cliente cliente) {
    if (cliente.getId() == null) {
      entityManager.persist(cliente);
      return cliente;
    } else {
      return entityManager.merge(cliente);
    }
  }

  @Override
  public List<Cliente> findAll() {
    return entityManager.createQuery("SELECT c FROM Cliente c", Cliente.class).getResultList();
  }

  @Override
  @Transactional
  public void deleteById(Long id) {
    findById(id).ifPresent(entityManager::remove);
  }

  @Override
  @Transactional
  public void delete(Cliente cliente) {
    if (cliente != null) {
      Cliente toRemove = entityManager.contains(cliente) ? cliente : entityManager.merge(cliente);
      entityManager.remove(toRemove);
    }
  }

  @Override
  public long count() {
    Long total =
        entityManager.createQuery("SELECT COUNT(c) FROM Cliente c", Long.class).getSingleResult();
    return total != null ? total : 0L;
  }
}
