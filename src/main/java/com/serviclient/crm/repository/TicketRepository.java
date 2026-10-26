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

/**
 * Repositorio Spring Data JPA para la entidad {@link Ticket}.
 * <p>
 * Facilita operaciones de conteo, verificación de SLAs vencidos, consulta por estado/prioridad y
 * búsqueda paginada multi-criterio.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Busca un ticket por su identificador primario restringiendo la consulta a la empresa tenant activa.
     *
     * @param id identificador del ticket
     * @param empresaId identificador de la empresa
     * @return {@link Optional} con el ticket encontrado
     */
    Optional<Ticket> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Busca un ticket por su código alfanumérico público (ej: "TCK-1001").
     *
     * @param numeroTicket código del ticket
     * @return {@link Optional} con el ticket encontrado
     */
    Optional<Ticket> findByNumeroTicket(String numeroTicket);

    /**
     * Busca un ticket por su código mediante consulta personalizada.
     *
     * @param codigo código del ticket
     * @return {@link Optional} con el ticket encontrado
     */
    @Query("SELECT t FROM Ticket t WHERE t.numeroTicket = :codigo")
    Optional<Ticket> findByCodigo(@Param("codigo") String codigo);

    /**
     * Lista los tickets registrados para una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return lista de tickets
     */
    List<Ticket> findByEmpresaId(Long empresaId);

    /**
     * Lista los tickets emitidos por un cliente específico.
     *
     * @param clienteId identificador del cliente
     * @return lista de tickets del cliente
     */
    List<Ticket> findByClienteId(Long clienteId);

    /**
     * Lista los tickets asignados a un agente.
     *
     * @param agenteId identificador del usuario agente
     * @return lista de tickets asignados
     */
    List<Ticket> findByAgenteId(Long agenteId);

    /**
     * Cuenta el total de tickets de una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return número de tickets
     */
    long countByEmpresaId(Long empresaId);

    /**
     * Cuenta tickets de una empresa filtrados por su estado operativo.
     *
     * @param empresaId identificador de la empresa
     * @param estado estado a evaluar
     * @return cantidad de tickets
     */
    long countByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado);

    /**
     * Cuenta tickets de una empresa filtrados por su nivel de prioridad.
     *
     * @param empresaId identificador de la empresa
     * @param prioridad nivel de prioridad
     * @return cantidad de tickets
     */
    long countByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad);

    /**
     * Cuenta tickets de un cliente excluyendo un estado específico (ej: CERRADO).
     *
     * @param clienteId identificador del cliente
     * @param estado estado excluido
     * @return cantidad de tickets
     */
    long countByClienteIdAndEstadoNot(Long clienteId, EstadoTicket estado);

    /**
     * Cuenta tickets de un cliente con prioridad dada excluyendo un estado.
     *
     * @param clienteId identificador del cliente
     * @param prioridad prioridad del ticket
     * @param estado estado excluido
     * @return cantidad de tickets
     */
    long countByClienteIdAndPrioridadAndEstadoNot(Long clienteId, PrioridadTicket prioridad, EstadoTicket estado);

    /**
     * Cuenta los tickets no cerrados de la empresa cuya fecha límite de resolución fue superada.
     *
     * @param empresaId identificador de la empresa
     * @param ahora marca de tiempo actual de comparación
     * @return número de tickets fuera de SLA
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.empresa.id = :empresaId AND t.estado <> 'CERRADO' AND t.slaResolucionLimite < :ahora")
    long countTicketsFueraDeSla(@Param("empresaId") Long empresaId, @Param("ahora") LocalDateTime ahora);

    /**
     * Realiza una búsqueda paginada aplicando filtros combinados de texto, estado, prioridad y categoría.
     *
     * @param empresaId identificador de la empresa
     * @param busqueda texto opcional de búsqueda en código o asunto
     * @param estado filtro opcional por estado
     * @param prioridad filtro opcional por prioridad
     * @param categoria filtro opcional por categoría
     * @param pageable paginación y ordenamiento
     * @return página {@link Page} de tickets coincidentes
     */
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

    /**
     * Método de conveniencia para totalizar tickets de la empresa.
     *
     * @param empresaId identificador de la empresa
     * @return total de tickets
     */
    default Long contarTicketsTotalPorEmpresa(Long empresaId) {
        return countByEmpresaId(empresaId);
    }
}
