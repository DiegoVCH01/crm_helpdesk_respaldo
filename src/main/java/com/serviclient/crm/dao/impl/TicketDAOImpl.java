package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.TicketDAO;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TicketDAOImpl implements TicketDAO {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Optional<Ticket> findById(Long id) {
    if (id == null) return Optional.empty();
    return Optional.ofNullable(entityManager.find(Ticket.class, id));
  }

  @Override
  public Optional<Ticket> findByIdAndEmpresaId(Long id, Long empresaId) {
    if (id == null || empresaId == null) {
      return Optional.empty();
    }

    List<Ticket> resultados =
        entityManager
            .createQuery(
                "SELECT t FROM Ticket t " + "WHERE t.id = :id AND t.empresa.id = :empresaId",
                Ticket.class)
            .setParameter("id", id)
            .setParameter("empresaId", empresaId)
            .setMaxResults(1)
            .getResultList();

    return resultados.stream().findFirst();
  }

  @Override
  public Optional<Ticket> findByNumeroTicket(String numeroTicket) {
    TypedQuery<Ticket> query =
        entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.numeroTicket = :numeroTicket", Ticket.class);
    query.setParameter("numeroTicket", numeroTicket);
    return query.getResultStream().findFirst();
  }

  @Override
  public Optional<Ticket> findByCodigo(String codigo) {
    TypedQuery<Ticket> query =
        entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.numeroTicket = :codigo", Ticket.class);
    query.setParameter("codigo", codigo);
    return query.getResultStream().findFirst();
  }

  @Override
  public List<Ticket> findByEmpresaId(Long empresaId) {
    TypedQuery<Ticket> query =
        entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.empresa.id = :empresaId", Ticket.class);
    query.setParameter("empresaId", empresaId);
    return query.getResultList();
  }

  @Override
  public List<Ticket> findByClienteId(Long clienteId) {
    TypedQuery<Ticket> query =
        entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.cliente.id = :clienteId", Ticket.class);
    query.setParameter("clienteId", clienteId);
    return query.getResultList();
  }

  @Override
  public List<Ticket> findByAgenteId(Long agenteId) {
    TypedQuery<Ticket> query =
        entityManager.createQuery(
            "SELECT t FROM Ticket t WHERE t.agente.id = :agenteId", Ticket.class);
    query.setParameter("agenteId", agenteId);
    return query.getResultList();
  }

  @Override
  public long countByEmpresaId(Long empresaId) {
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.empresa.id = :empresaId", Long.class);
    query.setParameter("empresaId", empresaId);
    Long count = query.getSingleResult();
    return count != null ? count : 0L;
  }

  @Override
  public long countByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado) {
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.empresa.id = :empresaId AND t.estado = :estado",
            Long.class);
    query.setParameter("empresaId", empresaId);
    query.setParameter("estado", estado);
    Long count = query.getSingleResult();
    return count != null ? count : 0L;
  }

  @Override
  public long countByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad) {
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.empresa.id = :empresaId AND t.prioridad ="
                + " :prioridad",
            Long.class);
    query.setParameter("empresaId", empresaId);
    query.setParameter("prioridad", prioridad);
    Long count = query.getSingleResult();
    return count != null ? count : 0L;
  }

  @Override
  public long countByClienteIdAndEstadoNot(Long clienteId, EstadoTicket estado) {
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.cliente.id = :clienteId AND t.estado <> :estado",
            Long.class);
    query.setParameter("clienteId", clienteId);
    query.setParameter("estado", estado);
    Long count = query.getSingleResult();
    return count != null ? count : 0L;
  }

  @Override
  public long countByClienteIdAndPrioridadAndEstadoNot(
      Long clienteId, PrioridadTicket prioridad, EstadoTicket estado) {
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.cliente.id = :clienteId AND t.prioridad ="
                + " :prioridad AND t.estado <> :estado",
            Long.class);
    query.setParameter("clienteId", clienteId);
    query.setParameter("prioridad", prioridad);
    query.setParameter("estado", estado);
    Long count = query.getSingleResult();
    return count != null ? count : 0L;
  }

  @Override
  public long countTicketsFueraDeSla(Long empresaId, LocalDateTime ahora) {
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.empresa.id = :empresaId AND t.estado NOT IN"
                + " (com.serviclient.crm.entity.enums.EstadoTicket.RESUELTO,"
                + " com.serviclient.crm.entity.enums.EstadoTicket.CERRADO) AND"
                + " t.slaResolucionLimite < :ahora",
            Long.class);
    query.setParameter("empresaId", empresaId);
    query.setParameter("ahora", ahora);
    Long count = query.getSingleResult();
    return count != null ? count : 0L;
  }

  @Override
  public Page<Ticket> buscarConFiltros(
      Long empresaId,
      String busqueda,
      EstadoTicket estado,
      PrioridadTicket prioridad,
      Pageable pageable) {
    String baseWhere =
        " WHERE t.empresa.id = :empresaId AND (:busqueda IS NULL OR :busqueda = '' OR"
            + " LOWER(t.numeroTicket) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR LOWER(t.asunto)"
            + " LIKE LOWER(CONCAT('%', :busqueda, '%')) OR LOWER(t.cliente.nombreComercial) LIKE"
            + " LOWER(CONCAT('%', :busqueda, '%')) OR (t.contacto IS NOT NULL AND"
            + " LOWER(t.contacto.email) LIKE LOWER(CONCAT('%', :busqueda, '%')))) AND (:estado IS"
            + " NULL OR t.estado = :estado) AND (:prioridad IS NULL OR t.prioridad = :prioridad)";

    String countJpql = "SELECT COUNT(t) FROM Ticket t" + baseWhere;
    TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
    countQuery.setParameter("empresaId", empresaId);
    countQuery.setParameter("busqueda", busqueda);
    countQuery.setParameter("estado", estado);
    countQuery.setParameter("prioridad", prioridad);
    Long total = countQuery.getSingleResult();
    long totalElements = total != null ? total : 0L;

    String selectJpql = "SELECT t FROM Ticket t" + baseWhere;
    TypedQuery<Ticket> query = entityManager.createQuery(selectJpql, Ticket.class);
    query.setParameter("empresaId", empresaId);
    query.setParameter("busqueda", busqueda);
    query.setParameter("estado", estado);
    query.setParameter("prioridad", prioridad);

    if (pageable != null && pageable.isPaged()) {
      query.setFirstResult((int) pageable.getOffset());
      query.setMaxResults(pageable.getPageSize());
    }

    List<Ticket> content = query.getResultList();
    return new PageImpl<>(content, pageable != null ? pageable : Pageable.unpaged(), totalElements);
  }

  @Override
  public Long contarTicketsTotalPorEmpresa(Long empresaId) {
    TypedQuery<Long> query =
        entityManager.createQuery(
            "SELECT COUNT(t) FROM Ticket t WHERE t.empresa.id = :empresaId", Long.class);
    query.setParameter("empresaId", empresaId);
    return query.getSingleResult();
  }

  @Override
  @Transactional
  public Ticket save(Ticket ticket) {
    if (ticket.getId() == null) {
      entityManager.persist(ticket);
      return ticket;
    } else {
      return entityManager.merge(ticket);
    }
  }

  @Override
  public List<Ticket> findAll() {
    return entityManager.createQuery("SELECT t FROM Ticket t", Ticket.class).getResultList();
  }

  @Override
  @Transactional
  public void deleteById(Long id) {
    findById(id).ifPresent(entityManager::remove);
  }

  @Override
  @Transactional
  public void delete(Ticket ticket) {
    if (ticket != null) {
      Ticket toRemove = entityManager.contains(ticket) ? ticket : entityManager.merge(ticket);
      entityManager.remove(toRemove);
    }
  }

  @Override
  public long count() {
    Long total =
        entityManager.createQuery("SELECT COUNT(t) FROM Ticket t", Long.class).getSingleResult();
    return total != null ? total : 0L;
  }
}
