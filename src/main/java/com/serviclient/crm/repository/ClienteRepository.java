package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Cliente}.
 * <p>
 * Incluye consultas de acceso restringidas por empresa tenant y búsqueda dinámica paginada por criterios múltiples.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por su id validando que pertenezca a la empresa tenant indicada.
     *
     * @param id identificador del cliente
     * @param empresaId identificador de la empresa
     * @return {@link Optional} con el cliente encontrado
     */
    Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Busca un cliente por su número de RUC dentro del contexto de una empresa tenant.
     *
     * @param empresaId identificador de la empresa
     * @param ruc número de identificación fiscal
     * @return {@link Optional} con el cliente correspondiente
     */
    Optional<Cliente> findByEmpresaIdAndRuc(Long empresaId, String ruc);

    /**
     * Lista todos los clientes registrados para una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return lista de clientes
     */
    List<Cliente> findByEmpresaId(Long empresaId);

    /**
     * Cuenta el total de clientes activos y registrados pertenecientes a una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return cantidad total de clientes
     */
    long countByEmpresaId(Long empresaId);

    /**
     * Realiza una búsqueda paginada filtrando por texto sobre nombre comercial, razón social, RUC o ejecutivo asignado.
     *
     * @param empresaId identificador de la empresa
     * @param busqueda término opcional de búsqueda
     * @param pageable parámetros de paginación y ordenamiento
     * @return página de resultados {@link Page}
     */
    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND " +
           "(:busqueda IS NULL OR :busqueda = '' OR " +
           "LOWER(c.nombreComercial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.ruc) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.responsable.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.responsable.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    Page<Cliente> buscarConFiltros(@Param("empresaId") Long empresaId,
                                   @Param("busqueda") String busqueda,
                                   Pageable pageable);
}
