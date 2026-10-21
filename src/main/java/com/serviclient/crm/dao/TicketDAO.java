package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketDAO {
    Optional<Ticket> findById(Long id);
    Optional<Ticket> findByNumeroTicket(String numeroTicket);
    Optional<Ticket> findByCodigo(String codigo);
    List<Ticket> findByEmpresaId(Long empresaId);
    List<Ticket> findByClienteId(Long clienteId);
    List<Ticket> findByAgenteId(Long agenteId);
    long countByEmpresaId(Long empresaId);
    long countByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado);
    long countByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad);
    long countByClienteIdAndEstadoNot(Long clienteId, EstadoTicket estado);
    long countByClienteIdAndPrioridadAndEstadoNot(Long clienteId, PrioridadTicket prioridad, EstadoTicket estado);
    long countTicketsFueraDeSla(Long empresaId, LocalDateTime ahora);
    Page<Ticket> buscarConFiltros(Long empresaId, String busqueda, EstadoTicket estado, PrioridadTicket prioridad, Pageable pageable);
    Long contarTicketsTotalPorEmpresa(Long empresaId);
    Ticket save(Ticket ticket);
    List<Ticket> findAll();
    void deleteById(Long id);
    void delete(Ticket ticket);
    long count();
}
