package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link Renovacion}.
 * <p>
 * Ofrece consultas agregadas para ventanas de vencimiento (&lt;30 días, 31-90 días, &gt;90 días),
 * identificación de clientes en riesgo por proximidad de renovación y búsqueda filtrada.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Repository
public interface RenovacionRepository extends JpaRepository<Renovacion, Long> {

    /**
     * Lista todas las renovaciones asociadas a una empresa tenant.
     *
     * @param empresaId identificador de la empresa
     * @return lista de renovaciones
     */
    List<Renovacion> findByEmpresaId(Long empresaId);

    /**
     * Lista las renovaciones o contratos correspondientes a un cliente.
     *
     * @param clienteId identificador del cliente
     * @return lista de renovaciones del cliente
     */
    List<Renovacion> findByClienteId(Long clienteId);

    /**
     * Lista las renovaciones de una empresa ordenadas ascendentemente por fecha de vencimiento.
     *
     * @param empresaId identificador de la empresa
     * @return lista de contratos ordenados por proximidad de término
     */
    List<Renovacion> findByEmpresaIdOrderByFechaVencimientoAsc(Long empresaId);

    /**
     * Cuenta renovaciones según su estado operativo para una empresa.
     *
     * @param empresaId identificador de la empresa
     * @param estado estado de renovación
     * @return cantidad de renovaciones
     */
    long countByEmpresaIdAndEstado(Long empresaId, EstadoRenovacion estado);

    /**
     * Cuenta contratos pendientes en una ventana temporal dada (por ejemplo, entre hoy y +30 días).
     *
     * @param empresaId identificador de la empresa
     * @param desde fecha inicial
     * @param hasta fecha final
     * @return total de renovaciones en el rango
     */
    @Query("SELECT COUNT(r) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.estado NOT IN " +
           "(com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO) " +
           "AND r.fechaVencimiento BETWEEN :desde AND :hasta")
    long countRenovacionesEnRango(@Param("empresaId") Long empresaId,
                                  @Param("desde") LocalDate desde,
                                  @Param("hasta") LocalDate hasta);

    /**
     * Cuenta contratos pendientes que vencen a más de una fecha determinada (ej: &gt;90 días).
     *
     * @param empresaId identificador de la empresa
     * @param fecha fecha umbral
     * @return cantidad de renovaciones más allá del umbral
     */
    @Query("SELECT COUNT(r) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.estado NOT IN " +
           "(com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO) " +
           "AND r.fechaVencimiento > :fecha")
    long countRenovacionesMasDeDias(@Param("empresaId") Long empresaId,
                                    @Param("fecha") LocalDate fecha);

    /**
     * Cuenta clientes únicos que poseen al menos un contrato no renovado por vencer antes de la fecha límite.
     *
     * @param empresaId identificador de la empresa
     * @param hasta fecha límite superior
     * @return cantidad de clientes en riesgo próximo
     */
    @Query("SELECT COUNT(DISTINCT r.cliente.id) FROM Renovacion r WHERE r.empresa.id = :empresaId AND r.fechaVencimiento <= :hasta " +
           "AND r.estado NOT IN (com.serviclient.crm.entity.enums.EstadoRenovacion.RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_RENOVADO, com.serviclient.crm.entity.enums.EstadoRenovacion.NO_REALIZADO)")
    long countClientesEnRiesgoRenovacionProxima(@Param("empresaId") Long empresaId,
                                                @Param("hasta") LocalDate hasta);

    /**
     * Realiza una búsqueda de renovaciones filtrando opcionalmente por cliente y estado, ordenando por vencimiento.
     *
     * @param empresaId identificador de la empresa
     * @param busqueda término opcional de búsqueda sobre el nombre comercial del cliente
     * @param estado estado opcional de renovación
     * @return lista de renovaciones filtradas
     */
    @Query("SELECT r FROM Renovacion r WHERE r.empresa.id = :empresaId " +
           "AND (:busqueda IS NULL OR :busqueda = '' OR LOWER(r.cliente.nombreComercial) LIKE LOWER(CONCAT('%', :busqueda, '%'))) " +
           "AND (:estado IS NULL OR r.estado = :estado) " +
           "ORDER BY r.fechaVencimiento ASC")
    List<Renovacion> buscarConFiltros(@Param("empresaId") Long empresaId,
                                      @Param("busqueda") String busqueda,
                                      @Param("estado") EstadoRenovacion estado);
}
