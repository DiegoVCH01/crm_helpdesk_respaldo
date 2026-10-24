package com.serviclient.crm.service;

import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.Ticket;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface IPortalTicketService {

    Ticket crearTicketDesdePortal(CuentaPortal cuentaPortal, PortalTicketDto dto, MultipartFile[] archivos) throws IOException;
}
