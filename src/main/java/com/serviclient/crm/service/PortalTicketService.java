package com.serviclient.crm.service;

import com.serviclient.crm.dao.TicketAdjuntoDAO;
import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.dto.TicketDto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.TicketAdjunto;
import com.serviclient.crm.entity.enums.CanalOrigen;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PortalTicketService {

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

  private static final Set<String> EXTENSIONES_PERMITIDAS =
      Set.of("png", "jpg", "jpeg", "pdf", "txt");

  private final TicketService ticketService;
  private final TicketAdjuntoDAO ticketAdjuntoDAO;

  @Transactional
  public Ticket crearTicketDesdePortal(
      CuentaPortal cuentaPortal, PortalTicketDto dto, MultipartFile[] archivos) throws IOException {

    if (cuentaPortal == null || !cuentaPortal.estaActiva()) {
      throw new IllegalArgumentException(
          "La cuenta del portal no está disponible o se encuentra inactiva.");
    }

    if (cuentaPortal.getContacto() == null || cuentaPortal.getContacto().getCliente() == null) {
      throw new IllegalStateException(
          "La cuenta del portal no tiene un contacto o cliente asociado.");
    }

    var contacto = cuentaPortal.getContacto();
    var cliente = contacto.getCliente();
    var empresa = cliente.getEmpresa();

    TicketDto ticketDto =
        TicketDto.builder()
            .clienteId(cliente.getId())
            .contactoId(contacto.getId())
            .agenteId(null)
            .asunto(dto.getAsunto())
            .descripcion(dto.getDescripcion())
            .categoria(dto.getCategoria())
            .prioridad(dto.getPrioridad())
            .canalOrigen(CanalOrigen.PORTAL_WEB)
            .build();

    /*
     * autor = null porque el ticket fue creado por el cliente
     * desde el Portal y no por un usuario interno de ServiClient.
     */
    Ticket ticket = ticketService.crearTicket(empresa.getId(), ticketDto, null);

    guardarAdjuntos(ticket, archivos);

    return ticket;
  }

  private void guardarAdjuntos(Ticket ticket, MultipartFile[] archivos) throws IOException {

    if (archivos == null || archivos.length == 0) {
      return;
    }

    Path directorio = Paths.get("uploads", "tickets", ticket.getId().toString());

    Files.createDirectories(directorio);

    for (MultipartFile archivo : archivos) {

      if (archivo == null || archivo.isEmpty()) {
        continue;
      }

      validarArchivo(archivo);

      String nombreOriginal =
          archivo.getOriginalFilename() != null
              ? Paths.get(archivo.getOriginalFilename()).getFileName().toString()
              : "archivo";

      String extension = obtenerExtension(nombreOriginal);

      String nombreGuardado = UUID.randomUUID() + "." + extension;

      Path destino = directorio.resolve(nombreGuardado);

      Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

      TicketAdjunto adjunto =
          TicketAdjunto.builder()
              .ticket(ticket)
              .mensaje(null)
              .nombreArchivo(nombreOriginal)
              .urlArchivo(destino.toString().replace("\\", "/"))
              .tipoArchivo(archivo.getContentType())
              .tamano(archivo.getSize())
              .build();

      ticketAdjuntoDAO.save(adjunto);
    }
  }

  private void validarArchivo(MultipartFile archivo) {

    if (archivo.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException(
          "El archivo " + archivo.getOriginalFilename() + " supera el límite de 10 MB.");
    }

    String nombre = archivo.getOriginalFilename();

    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El archivo no tiene un nombre válido.");
    }

    String extension = obtenerExtension(nombre);

    if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
      throw new IllegalArgumentException("Formato no permitido: " + nombre);
    }
  }

  private String obtenerExtension(String nombreArchivo) {

    int punto = nombreArchivo.lastIndexOf('.');

    if (punto < 0 || punto == nombreArchivo.length() - 1) {
      return "";
    }

    return nombreArchivo.substring(punto + 1).toLowerCase();
  }
}
