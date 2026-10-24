package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Ticket> findByNumeroTicket(String numeroTicket);

    @Query("SELECT t FROM Ticket t WHERE t.numeroTicket = :codigo")
    Optional<Ticket> findByCodigo(@Param("codigo") String codigo);

    List<Ticket> findByEmpresaId(Long empresaId);

    List<Ticket> findByClienteId(Long clienteId);

    List<Ticket> findByAgenteId(Long agenteId);

    long countByEmpresaId(Long empresaId);

    long countByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado);

    long countByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad);

    long countByClienteIdAndEstadoNot(Long clienteId, EstadoTicket estado);

    long countByClienteIdAndPrioridadAndEstadoNot(Long clienteId, PrioridadTicket prioridad, EstadoTicket estado);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.empresa.id = :empresaId AND t.estado <> 'CERRADO' AND t.slaResolucionLimite < :ahora")
    long countTicketsFueraDeSla(@Param("empresaId") Long empresaId, @Param("ahora") LocalDateTime ahora);

    @Query("SELECT t FROM Ticket t WHERE t.empresa.id = :empresaId " +
           "AND (:busqueda IS NULL OR :busqueda = '' OR LOWER(t.numeroTicket) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR LOWER(t.asunto) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
           "AND (:estado IS NULL OR t.estado = :estado) " +
           "AND (:prioridad IS NULL OR t.prioridad = :prioridad) " +
           "AND (:categoria IS NULL OR t.categoriaEntity.nombre = :categoria)")
    Page<Ticket> buscarConFiltros(@Param("empresaId") Long empresaId,
                                  @Param("busqueda") String busqueda,
                                  @Param("estado") EstadoTicket estado,
                                  @Param("prioridad") PrioridadTicket prioridad,
                                  @Param("categoria") CategoriaTicket categoria,
                                  Pageable pageable);

    default Long contarTicketsTotalPorEmpresa(Long empresaId) {
        return countByEmpresaId(empresaId);
    }
}
