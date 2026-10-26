package com.serviclient.crm.service;

import com.serviclient.crm.dto.TicketDto;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.TicketMensaje;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contrato de servicio para el ciclo de vida completo de tickets de soporte en la Mesa de Ayuda.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.TicketService
 */
public interface ITicketService {

    /**
     * DTO inmutable con indicadores cuantitativos del estado de la mesa de ayuda.
     */
    @Getter
    @Builder
    public static class MetricasTickets {
        private long ticketsAbiertos;
        private long enProceso;
        private long criticos;
        private long pendientes;
        private long fueraDeSla;
    }

    /**
     * Obtiene el resumen de métricas e indicadores de tickets para una empresa.
     *
     * @param empresaId identificador de la empresa multitenant
     * @return objeto {@link MetricasTickets} con los conteos agregados
     */
    MetricasTickets obtenerMetricas(Long empresaId);

    /**
     * Lista de manera paginada los tickets de la empresa aplicando filtros combinados opcionales.
     *
     * @param empresaId identificador de la empresa
     * @param busqueda término opcional de búsqueda por código o asunto
     * @param estado filtro opcional por {@link EstadoTicket}
     * @param prioridad filtro opcional por {@link PrioridadTicket}
     * @param categoria filtro opcional por {@link CategoriaTicket}
     * @param pageable configuración de paginación y ordenamiento
     * @return página con los tickets coincidentes
     */
    Page<Ticket> listarTickets(Long empresaId, String busqueda, EstadoTicket estado,
                               PrioridadTicket prioridad, CategoriaTicket categoria, Pageable pageable);

    /**
     * Obtiene un ticket por su identificador único.
     *
     * @param id identificador del ticket
     * @return instancia de {@link Ticket}
     * @throws org.springframework.web.server.ResponseStatusException si el ticket no existe
     */
    Ticket obtenerPorId(Long id);

    /**
     * Obtiene un ticket validando la pertenencia a la empresa activa.
     *
     * @param id identificador del ticket
     * @param empresaId identificador de la empresa
     * @return instancia de {@link Ticket}
     * @throws org.springframework.web.server.ResponseStatusException si no existe o pertenece a otra empresa
     */
    Ticket obtenerPorIdYEmpresa(Long id, Long empresaId);

    /**
     * Busca un ticket por su código correlativo institucional (ej. {@code TK-2024-001}).
     *
     * @param codigo código único del ticket
     * @return instancia de {@link Ticket}
     * @throws org.springframework.web.server.ResponseStatusException si el código no coincide
     */
    Ticket obtenerPorCodigo(String codigo);

    /**
     * Registra un nuevo ticket de soporte calculando automáticamente los plazos de SLA.
     *
     * @param empresaId identificador de la empresa
     * @param dto datos de entrada del ticket (asunto, descripción, prioridad, categoría, clienteId)
     * @param autor usuario que registra la incidencia
     * @return ticket persistido con plazos de primera respuesta y resolución
     * @throws IllegalArgumentException si los datos requeridos no son válidos
     */
    Ticket crearTicket(Long empresaId, TicketDto dto, Usuario autor);

    /**
     * Agrega un nuevo mensaje o nota interna a la conversación de un ticket.
     *
     * @param ticketId identificador del ticket
     * @param autor usuario que redacta el mensaje
     * @param contenido texto del mensaje
     * @param esNotaInterna {@code true} si es nota privada solo visible para agentes, {@code false} si es respuesta al cliente
     * @return entidad {@link TicketMensaje} registrada
     * @throws org.springframework.web.server.ResponseStatusException si el ticket no existe
     */
    TicketMensaje enviarMensaje(Long ticketId, Usuario autor, String contenido, boolean esNotaInterna);

    /**
     * Reasigna el agente responsable de la atención del ticket registrando auditoría en el historial.
     *
     * @param ticketId identificador del ticket
     * @param nuevoAgenteId identificador del nuevo agente responsable
     * @param autor usuario que efectúa la reasignación
     * @throws org.springframework.web.server.ResponseStatusException si el ticket o agente no existen
     */
    void asignarAgente(Long ticketId, Long nuevoAgenteId, Usuario autor);

    /**
     * Modifica el estado y/o severidad del ticket, actualizando la trazabilidad histórica.
     *
     * @param ticketId identificador del ticket
     * @param nuevoEstado nuevo {@link EstadoTicket}
     * @param nuevaPrioridad nueva {@link PrioridadTicket}
     * @param comentario justificación del cambio de estado
     * @param autor usuario responsable del cambio
     * @throws org.springframework.web.server.ResponseStatusException si el ticket no existe
     */
    void cambiarEstadoYPrioridad(Long ticketId, EstadoTicket nuevoEstado,
                                  PrioridadTicket nuevaPrioridad, String comentario, Usuario autor);

    /**
     * Concluye la atención del ticket, registrando resolución y disparando encuesta CSAT si corresponde.
     *
     * @param ticketId identificador del ticket
     * @param enviarCsat indicador de envío automático de encuesta de satisfacción
     * @param comentarioCierre motivo o solución final aplicada
     * @param autor usuario que formaliza el cierre
     * @throws org.springframework.web.server.ResponseStatusException si el ticket no existe
     */
    void cerrarTicket(Long ticketId, Boolean enviarCsat, String comentarioCierre, Usuario autor);
}
