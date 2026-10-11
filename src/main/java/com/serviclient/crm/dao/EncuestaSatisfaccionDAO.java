package com.serviclient.crm.dao;

import com.serviclient.crm.entity.EncuestaSatisfaccion;
import java.util.List;

public interface EncuestaSatisfaccionDAO {
    EncuestaSatisfaccion save(EncuestaSatisfaccion encuesta);
    List<EncuestaSatisfaccion> findByClienteId(Long clienteId);
    List<EncuestaSatisfaccion> findByTicketId(Long ticketId);
    Double obtenerPromedioCsatPorCliente(Long clienteId);
    Double obtenerPromedioCsatPorEmpresa(Long empresaId);
}