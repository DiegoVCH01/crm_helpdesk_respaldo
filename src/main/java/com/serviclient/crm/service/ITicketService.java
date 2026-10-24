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

public interface ITicketService {

    @Getter
    @Builder
    public static class MetricasTickets {
        private long ticketsAbiertos;
        private long enProceso;
        private long criticos;
        private long pendientes;
        private long fueraDeSla;
    }

    MetricasTickets obtenerMetricas(Long empresaId);

    Page<Ticket> listarTickets(Long empresaId, String busqueda, EstadoTicket estado,
                               PrioridadTicket prioridad, CategoriaTicket categoria, Pageable pageable);

    Ticket obtenerPorId(Long id);

    Ticket obtenerPorIdYEmpresa(Long id, Long empresaId);

    Ticket obtenerPorCodigo(String codigo);

    Ticket crearTicket(Long empresaId, TicketDto dto, Usuario autor);

    TicketMensaje enviarMensaje(Long ticketId, Usuario autor, String contenido, boolean esNotaInterna);

    void asignarAgente(Long ticketId, Long nuevoAgenteId, Usuario autor);

    void cambiarEstadoYPrioridad(Long ticketId, EstadoTicket nuevoEstado,
                                  PrioridadTicket nuevaPrioridad, String comentario, Usuario autor);

    void cerrarTicket(Long ticketId, Boolean enviarCsat, String comentarioCierre, Usuario autor);
}
