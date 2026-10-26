package com.serviclient.crm.service;

import com.serviclient.crm.repository.TicketAdjuntoRepository;
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

/**
 * Implementación del servicio de gestión de tickets e incidencias reportadas desde el portal de autoservicio del cliente.
 * <p>
 * Se encarga de transformar las peticiones del portal en tickets formales del CRM asignados
 * con origen {@link CanalOrigen#PORTAL_WEB}, gestionando además el almacenamiento físico
 * y validación de archivos adjuntos (límite de 10 MB y formatos permitidos png, jpg, jpeg, pdf, txt).
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see IPortalTicketService
 */
@Service
@RequiredArgsConstructor
public class PortalTicketService implements IPortalTicketService {

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

  private static final Set<String> EXTENSIONES_PERMITIDAS =
      Set.of("png", "jpg", "jpeg", "pdf", "txt");

  private final TicketService ticketService;
  private final TicketAdjuntoRepository ticketAdjuntoRepository;

  /**
   * Registra una nueva incidencia emitida por un contacto cliente autenticado a través del portal de clientes.
   *
   * @param cuentaPortal credencial de la cuenta del portal en sesión
   * @param dto formulario con el asunto, categoría, prioridad y descripción del problema
   * @param archivos arreglo opcional de archivos adjuntos cargados por el usuario
   * @return entidad {@link Ticket} creada y asociada a la empresa y cliente correspondientes
   * @throws IOException si ocurre un error de E/S al guardar los archivos adjuntos en disco
   * @throws IllegalArgumentException si la cuenta está inactiva o los archivos superan límites de tamaño o formato
   * @throws IllegalStateException si la cuenta del portal carece de relación con un contacto o cliente
   */
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

  /**
   * Almacena físicamente los archivos adjuntos en el sistema de archivos del servidor
   * y registra las entidades {@link TicketAdjunto} en la base de datos.
   *
   * @param ticket ticket al que se asocian los adjuntos
   * @param archivos lista de ficheros subidos en la petición HTTP
   * @throws IOException si falla la creación del directorio o la copia del flujo binario
   */
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

      ticketAdjuntoRepository.save(adjunto);
    }
  }

  /**
   * Valida que el archivo individual no exceda el límite máximo de tamaño ni utilice extensiones no permitidas.
   *
   * @param archivo archivo multiparte a validar
   * @throws IllegalArgumentException si excede 10 MB, no tiene nombre o la extensión no es soportada
   */
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

  /**
   * Extrae la extensión del nombre del archivo en minúsculas.
   *
   * @param nombreArchivo nombre del fichero
   * @return extensión sin el punto, o cadena vacía si no posee extensión
   */
  private String obtenerExtension(String nombreArchivo) {

    int punto = nombreArchivo.lastIndexOf('.');

    if (punto < 0 || punto == nombreArchivo.length() - 1) {
      return "";
    }

    return nombreArchivo.substring(punto + 1).toLowerCase();
  }
}
