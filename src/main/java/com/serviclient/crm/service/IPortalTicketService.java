package com.serviclient.crm.service;

import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.Ticket;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato de servicio para la gestión de incidencias reportadas desde el portal de autoservicio de clientes.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.PortalTicketService
 */
public interface IPortalTicketService {

    /**
     * Registra un nuevo ticket de soporte iniciado por un contacto autenticado en el portal, adjuntando archivos opcionales.
     *
     * @param cuentaPortal cuenta del cliente que crea la incidencia
     * @param dto datos del formulario del ticket (asunto, descripción, categoría, prioridad)
     * @param archivos archivos adjuntos opcionales cargados en la solicitud
     * @return entidad {@link Ticket} creada y asociada al cliente
     * @throws IOException si ocurre un fallo al procesar los archivos binarios adjuntos
     */
    Ticket crearTicketDesdePortal(CuentaPortal cuentaPortal, PortalTicketDto dto, MultipartFile[] archivos) throws IOException;
}
