package com.serviclient.crm.service;

import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.RenovacionSeguimiento;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Contrato de servicio para el monitoreo de vencimientos de contratos, ARR en riesgo y bitácora de seguimientos.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.RenovacionService
 */
public interface IRenovacionService {

    /**
     * DTO con indicadores cuantitativos de contratos clasificados por ventanas de vencimiento y estado.
     */
    @Getter
    @Builder
    public static class MetricasRenovaciones {
        private long menosDe30Dias;
        private long de31a90Dias;
        private long masDe90Dias;
        private long completadas;
        private long noRealizadas;
        private long clientesEnRiesgoProximos;
    }

    /**
     * Obtiene las renovaciones de contratos próximas a vencer en el sistema.
     *
     * @return lista de contratos ordenados por proximidad de vencimiento
     */
    List<Renovacion> obtenerProximasRenovaciones();

    /**
     * Lista renovaciones de contratos para una empresa con filtros opcionales de búsqueda y estado.
     *
     * @param empresaId identificador de la empresa
     * @param busqueda texto opcional de búsqueda por nombre de cliente
     * @param estado filtro opcional por {@link EstadoRenovacion}
     * @return lista de renovaciones filtradas
     */
    List<Renovacion> listarProximasRenovaciones(Long empresaId, String busqueda, EstadoRenovacion estado);

    /**
     * Calcula métricas analíticas sobre el volumen de contratos próximos a vencer para una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return objeto {@link MetricasRenovaciones} consolidado
     */
    MetricasRenovaciones calcularMetricas(Long empresaId);

    /**
     * Acceso simplificado al cálculo de métricas de renovación.
     *
     * @param empresaId identificador de la empresa
     * @return objeto {@link MetricasRenovaciones}
     */
    default MetricasRenovaciones obtenerMetricas(Long empresaId) {
        return calcularMetricas(empresaId);
    }

    /**
     * Busca una renovación por su identificador único.
     *
     * @param id identificador de la renovación
     * @return entidad {@link Renovacion}
     * @throws org.springframework.web.server.ResponseStatusException si no existe
     */
    Renovacion obtenerPorId(Long id);

    /**
     * Actualiza el estado de negociación del contrato registrando al usuario responsable.
     *
     * @param id identificador de la renovación
     * @param nuevoEstado nuevo {@link EstadoRenovacion}
     * @param responsable usuario que realiza el cambio
     * @return entidad {@link Renovacion} actualizada
     */
    Renovacion actualizarEstado(Long id, EstadoRenovacion nuevoEstado, Usuario responsable);

    /**
     * Agrega una nota de seguimiento a la bitácora comercial de la renovación.
     *
     * @param renovacionId identificador de la renovación
     * @param nota contenido descriptivo del avance comercial
     * @param responsable usuario autor de la nota
     * @return registro {@link RenovacionSeguimiento} persistido
     */
    RenovacionSeguimiento agregarSeguimiento(Long renovacionId, String nota, Usuario responsable);

    /**
     * Procesa y valida el registro de un seguimiento comercial desde el formulario web.
     *
     * @param dto datos de entrada del seguimiento
     * @param usuario usuario responsable
     * @param empresaId identificador de la empresa para control de multitenancy
     * @return registro {@link RenovacionSeguimiento} persistido
     */
    RenovacionSeguimiento registrarSeguimiento(
        com.serviclient.crm.dto.TicketMensajeDto.RegistrarSeguimientoRenovacion dto, Usuario usuario, Long empresaId);

    /**
     * Obtiene el historial completo de seguimientos registrados para un contrato.
     *
     * @param renovacionId identificador de la renovación
     * @return lista de notas de seguimiento ordenadas cronológicamente
     */
    List<RenovacionSeguimiento> obtenerSeguimientos(Long renovacionId);
}
