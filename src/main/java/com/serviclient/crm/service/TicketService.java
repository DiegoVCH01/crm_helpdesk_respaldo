package com.serviclient.crm.service;

import com.serviclient.crm.dao.*;
import com.serviclient.crm.dto.TicketDto;
import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.EstadoEncuestaCsat;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.entity.enums.TipoMensajeTicket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio de lógica de negocio para la Mesa de Ayuda (Helpdesk).
 *
 * <p>Centraliza todas las operaciones del ciclo de vida de un ticket: creación con cálculo de SLA,
 * mensajería, asignación de agentes, cambios de estado/prioridad, cierre con CSAT y auditoría
 * automática.
 *
 * <p><b>Invariantes de negocio aplicadas:</b>
 *
 * <ul>
 *   <li>El número de ticket se genera automáticamente ({@code TK-XXXX}).
 *   <li>Los límites de SLA se calculan al crear el ticket según la {@link
 *       com.serviclient.crm.entity.ConfiguracionSla} de la empresa.
 *   <li>La primera respuesta solo se registra una vez.
 *   <li>Al cerrar un ticket se puede generar automáticamente una {@link
 *       com.serviclient.crm.entity.EncuestaCsat} pendiente.
 *   <li>Cada acción relevante se persiste en {@link com.serviclient.crm.entity.TicketHistorial}.
 * </ul>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.controller.TicketController
 * @see com.serviclient.crm.entity.Ticket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

  private final TicketDAO ticketDAO;
  private final TicketMensajeDAO ticketMensajeDAO;
  private final TicketHistorialDAO ticketHistorialDAO;
  private final EncuestaSatisfaccionDAO encuestaSatisfaccionDAO;
  private final EncuestaCsatDAO encuestaCsatDAO;
  private final RespuestaCsatDAO respuestaCsatDAO;
  private final ClienteDAO clienteDAO;
  private final ContactoDAO contactoDAO;
  private final UsuarioDAO usuarioDAO;
  private final CategoriaTicketDAO categoriaTicketDAO;
  private final ConfiguracionSlaDAO configuracionSlaDAO;
  private final EmpresaService empresaService;
  private final HealthScoreService healthScoreService;

  /**
   * DTO inmutable con los KPIs principales de la mesa de ayuda. Se utiliza en la vista de listado
   * para mostrar el panel de métricas.
   */
  @Getter
  @Builder
  public static class MetricasTickets {
    /** Total de tickets en estado {@code ABIERTO}. */
    private long ticketsAbiertos;

    /** Total de tickets en estado {@code EN_PROCESO}. */
    private long enProceso;

    /** Total de tickets con prioridad {@code CRITICA} sin cerrar. */
    private long criticos;

    /** Total de tickets en estado {@code PENDIENTE}. */
    private long pendientes;

    /** Total de tickets cuyo límite de resolución SLA ya venció. */
    private long fueraDeSla;
  }

  /**
   * Calcula y retorna las métricas actuales de la mesa de ayuda para la empresa indicada.
   *
   * @param empresaId identificador de la empresa
   * @return {@link MetricasTickets} con conteos en tiempo real
   */
  @Transactional(readOnly = true)
  public MetricasTickets obtenerMetricas(Long empresaId) {
    long abiertos = ticketDAO.countByEmpresaIdAndEstado(empresaId, EstadoTicket.ABIERTO);
    long enProceso = ticketDAO.countByEmpresaIdAndEstado(empresaId, EstadoTicket.EN_PROCESO);
    long criticos = ticketDAO.countByEmpresaIdAndPrioridad(empresaId, PrioridadTicket.CRITICA);
    long pendientes = ticketDAO.countByEmpresaIdAndEstado(empresaId, EstadoTicket.PENDIENTE);
    long fueraDeSla = ticketDAO.countTicketsFueraDeSla(empresaId, LocalDateTime.now());

    return MetricasTickets.builder()
        .ticketsAbiertos(abiertos)
        .enProceso(enProceso)
        .criticos(criticos)
        .pendientes(pendientes)
        .fueraDeSla(fueraDeSla)
        .build();
  }

  /**
   * Lista tickets de una empresa aplicando filtros opcionales de búsqueda, estado, prioridad y
   * categoría. La páginación es manejada por el repositorio.
   *
   * @param empresaId identificador de la empresa
   * @param busqueda término libre de búsqueda (número de ticket o asunto); puede ser {@code null}
   * @param estado filtro de estado; puede ser {@code null} para todos los estados
   * @param prioridad filtro de prioridad; puede ser {@code null}
   * @param categoria filtro de categoría aplicado en memoria; puede ser {@code null}
   * @param pageable configuración de página y ordenamiento
   * @return página de tickets que cumplen los criterios
   */
  @Transactional(readOnly = true)
  public Page<Ticket> listarTickets(
      Long empresaId,
      String busqueda,
      EstadoTicket estado,
      PrioridadTicket prioridad,
      CategoriaTicket categoria,
      Pageable pageable) {
    Page<Ticket> page =
        ticketDAO.buscarConFiltros(empresaId, busqueda, estado, prioridad, pageable);
    if (categoria != null) {
      List<Ticket> filtered =
          page.getContent().stream().filter(t -> t.getCategoria() == categoria).toList();
      return new PageImpl<>(filtered, pageable, filtered.size());
    }
    return page;
  }

  /**
   * Obtiene un ticket por su identificador primario.
   *
   * @param id identificador del ticket
   * @return entidad {@link com.serviclient.crm.entity.Ticket} encontrada
   * @throws IllegalArgumentException si no existe un ticket con el {@code id} indicado
   */
  @Transactional(readOnly = true)
  public Ticket obtenerPorId(Long id) {
    return ticketDAO
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado con id: " + id));
  }

  @Transactional(readOnly = true)
  public Ticket obtenerPorIdYEmpresa(Long id, Long empresaId) {
    return ticketDAO
        .findByIdAndEmpresaId(id, empresaId)
        .orElseThrow(
            () -> {
              log.warn("Acceso rechazado a ticket. ticketId={}, empresaId={}", id, empresaId);

              return new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado.");
            });
  }

  /**
   * Obtiene un ticket por su código alfanumérico (p.ej. {@code "TK-4029"}).
   *
   * @param codigo código único del ticket
   * @return entidad {@link com.serviclient.crm.entity.Ticket} encontrada
   * @throws IllegalArgumentException si no existe un ticket con el código indicado
   */
  @Transactional(readOnly = true)
  public Ticket obtenerPorCodigo(String codigo) {
    return ticketDAO
        .findByCodigo(codigo)
        .orElseThrow(
            () -> new IllegalArgumentException("Ticket no encontrado con código: " + codigo));
  }

  /**
   * Crea un nuevo ticket con cálculo automático de límites de SLA. Persiste el mensaje inicial, el
   * historial de creación y notifica al agente asignado.
   *
   * @param empresaId identificador de la empresa que crea el ticket
   * @param dto datos del nuevo ticket (cliente, asunto, prioridad, agente, etc.)
   * @param autor usuario autenticado que crea el ticket
   * @return entidad {@link com.serviclient.crm.entity.Ticket} persistida con número auto-generado
   * @throws IllegalArgumentException si el cliente o la empresa no existen
   */
  @Transactional
  public Ticket crearTicket(Long empresaId, TicketDto dto, Usuario autor) {
    Empresa empresa = empresaService.obtenerPorId(empresaId);
    Cliente cliente =
        clienteDAO
            .findById(dto.getClienteId())
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    if (cliente.getEmpresa() == null || !empresaId.equals(cliente.getEmpresa().getId())) {
      throw new IllegalArgumentException("El cliente no pertenece a la empresa actual.");
    }

    Contacto contacto = null;
    if (dto.getContactoId() != null) {
      contacto =
          contactoDAO
              .findById(dto.getContactoId())
              .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado"));

      if (contacto.getCliente() == null || !contacto.getCliente().getId().equals(cliente.getId())) {
        throw new IllegalArgumentException("El contacto no pertenece al cliente seleccionado.");
      }
    } else {
      contacto = cliente.getContactoPrincipal();
    }

    Usuario agente = null;
    if (dto.getAgenteId() != null) {
      agente =
          usuarioDAO
              .findById(dto.getAgenteId())
              .orElseThrow(() -> new IllegalArgumentException("Agente no encontrado"));

      if (agente.getEmpresa() == null || !empresaId.equals(agente.getEmpresa().getId())) {
        throw new IllegalArgumentException("El agente no pertenece a la empresa actual.");
      }
    }

    // Generar número de ticket TK-xxxx
    String numeroTicket = "TK-" + (4000 + new Random().nextInt(5000));

    // Obtener o crear Categoría
    String catNombre =
        dto.getCategoria() != null ? dto.getCategoria().getEtiqueta() : "Soporte Técnico";
    CategoriaTicketEntity categoriaEntity =
        categoriaTicketDAO
            .findByEmpresaIdAndNombre(empresaId, catNombre)
            .orElseGet(
                () ->
                    categoriaTicketDAO.save(
                        CategoriaTicketEntity.builder()
                            .empresa(empresa)
                            .nombre(catNombre)
                            .descripcion("Categoría " + catNombre)
                            .estado("ACTIVO")
                            .build()));

    LocalDateTime ahora = LocalDateTime.now();
    PrioridadTicket prioridad =
        dto.getPrioridad() != null ? dto.getPrioridad() : PrioridadTicket.MEDIA;

    // SLA según configuración
    int minutosResp = 120;
    int minutosResol = 1440;
    var slaConfig = configuracionSlaDAO.findByEmpresaIdAndPrioridad(empresaId, prioridad);
    if (slaConfig.isPresent()) {
      minutosResp = slaConfig.get().getTiempoRespuestaMinutos();
      minutosResol = slaConfig.get().getTiempoResolucionMinutos();
    }

    Ticket ticket =
        Ticket.builder()
            .numeroTicket(numeroTicket)
            .empresa(empresa)
            .cliente(cliente)
            .contacto(contacto)
            .categoriaEntity(categoriaEntity)
            .agente(agente)
            .asunto(dto.getAsunto())
            .descripcion(dto.getDescripcion())
            .prioridad(prioridad)
            .estado(EstadoTicket.ABIERTO)
            .createdAt(ahora)
            .slaRespuestaLimite(ahora.plusMinutes(minutosResp))
            .slaResolucionLimite(ahora.plusMinutes(minutosResol))
            .build();

    Ticket guardado = ticketDAO.save(ticket);
    log.info(
        "Ticket creado correctamente. ticketId={}, empresaId={}, prioridad={}",
        guardado.getId(),
        empresaId,
        prioridad);

    if (prioridad == PrioridadTicket.CRITICA) {
      log.warn("Ticket crítico registrado. ticketId={}, empresaId={}", guardado.getId(), empresaId);
    }

    // Mensaje inicial en la conversación
    String remitenteNombre =
        (contacto != null)
            ? contacto.getNombreCompleto()
            : (autor != null ? autor.getNombreCompleto() : "Cliente");
    TicketMensaje mensajeInicial =
        TicketMensaje.builder()
            .ticket(guardado)
            .usuario(autor)
            .contacto(contacto)
            .tipo(autor != null ? TipoMensajeTicket.AGENTE : TipoMensajeTicket.CLIENTE)
            .esInterno(false)
            .mensaje(dto.getDescripcion())
            .createdAt(ahora)
            .build();
    ticketMensajeDAO.save(mensajeInicial);

    // Registro de auditoría
    TicketHistorial hist =
        TicketHistorial.builder()
            .ticket(guardado)
            .usuario(autor)
            .tipoEvento("CREACION")
            .descripcion("Ticket creado: " + dto.getAsunto())
            .createdAt(ahora)
            .build();
    ticketHistorialDAO.save(hist);

    healthScoreService.recalcularYActualizarHealthScore(cliente);
    return guardado;
  }

  @Transactional
  public TicketMensaje enviarMensaje(
      Long ticketId, Usuario autor, String contenido, boolean esNotaInterna) {
    Ticket ticket = obtenerPorId(ticketId);
    LocalDateTime ahora = LocalDateTime.now();

    // Si es la primera respuesta del agente, marcar SLA de 1ra respuesta
    if (!esNotaInterna && ticket.getFechaPrimeraRespuesta() == null) {
      ticket.setFechaPrimeraRespuesta(ahora);
      ticket.setEstado(EstadoTicket.EN_PROCESO);
      ticketDAO.save(ticket);
    }

    TicketMensaje mensaje =
        TicketMensaje.builder()
            .ticket(ticket)
            .usuario(autor)
            .tipo(TipoMensajeTicket.AGENTE)
            .esInterno(esNotaInterna)
            .mensaje(contenido)
            .createdAt(ahora)
            .build();

    TicketMensaje guardado = ticketMensajeDAO.save(mensaje);

    // Registro de historial si es mensaje público
    if (!esNotaInterna) {
      TicketHistorial hist =
          TicketHistorial.builder()
              .ticket(ticket)
              .usuario(autor)
              .tipoEvento("RESPUESTA")
              .descripcion(contenido.length() > 60 ? contenido.substring(0, 57) + "..." : contenido)
              .createdAt(ahora)
              .build();
      ticketHistorialDAO.save(hist);
    }

    return guardado;
  }

  @Transactional
  public void asignarAgente(Long ticketId, Long nuevoAgenteId, Usuario autor) {
    Ticket ticket = obtenerPorId(ticketId);
    Usuario nuevoAgente =
        usuarioDAO
            .findById(nuevoAgenteId)
            .orElseThrow(() -> new IllegalArgumentException("Agente no encontrado"));

    ticket.setAgente(nuevoAgente);
    ticketDAO.save(ticket);

    TicketHistorial hist =
        TicketHistorial.builder()
            .ticket(ticket)
            .usuario(autor)
            .tipoEvento("ASIGNACION")
            .valorNuevo(nuevoAgente.getNombreCompleto())
            .descripcion("Ticket asignado a " + nuevoAgente.getNombreCompleto())
            .createdAt(LocalDateTime.now())
            .build();
    ticketHistorialDAO.save(hist);
  }

  @Transactional
  public void cambiarEstadoYPrioridad(
      Long ticketId,
      EstadoTicket nuevoEstado,
      PrioridadTicket nuevaPrioridad,
      String comentario,
      Usuario autor) {
    Ticket ticket = obtenerPorId(ticketId);
    LocalDateTime ahora = LocalDateTime.now();

    if (nuevoEstado != null && nuevoEstado != ticket.getEstado()) {
      String anterior = ticket.getEstado().getEtiqueta();
      ticket.setEstado(nuevoEstado);
      if (nuevoEstado == EstadoTicket.RESUELTO && ticket.getFechaResolucion() == null) {
        ticket.setFechaResolucion(ahora);
      }
      TicketHistorial hist =
          TicketHistorial.builder()
              .ticket(ticket)
              .usuario(autor)
              .tipoEvento("ESTADO")
              .valorAnterior(anterior)
              .valorNuevo(nuevoEstado.getEtiqueta())
              .descripcion(
                  comentario != null && !comentario.isBlank()
                      ? comentario
                      : "Actualización manual de estado")
              .createdAt(ahora)
              .build();
      ticketHistorialDAO.save(hist);
    }

    if (nuevaPrioridad != null && nuevaPrioridad != ticket.getPrioridad()) {
      String anterior = ticket.getPrioridad().getEtiqueta();
      ticket.setPrioridad(nuevaPrioridad);
      TicketHistorial hist =
          TicketHistorial.builder()
              .ticket(ticket)
              .usuario(autor)
              .tipoEvento("PRIORIDAD")
              .valorAnterior(anterior)
              .valorNuevo(nuevaPrioridad.getEtiqueta())
              .descripcion("Ajuste de prioridad")
              .createdAt(ahora)
              .build();
      ticketHistorialDAO.save(hist);
    }

    ticketDAO.save(ticket);
    healthScoreService.recalcularYActualizarHealthScore(ticket.getCliente());
  }

  @Transactional
    public void cerrarTicket(Long ticketId, Boolean enviarCsat, String comentarioCierre, Usuario autor) {
        Ticket ticket = obtenerPorId(ticketId);
        LocalDateTime ahora = LocalDateTime.now();

        ticket.setEstado(EstadoTicket.CERRADO);
        ticket.setFechaCierre(ahora);
        if (ticket.getFechaResolucion() == null) {
            ticket.setFechaResolucion(ahora);
        }
        ticketDAO.save(ticket);

        TicketHistorial hist = TicketHistorial.builder()
                .ticket(ticket)
                .usuario(autor)
                .tipoEvento("CIERRE")
                .descripcion(Boolean.TRUE.equals(enviarCsat) ? "Cierre de ticket con encuesta CSAT" : "Cierre de ticket")
                .createdAt(ahora)
                .build();
        ticketHistorialDAO.save(hist);

        if (Boolean.TRUE.equals(enviarCsat)) {

            if (!encuestaCsatDAO.existsByTicketId(ticketId)) {

                EncuestaCsat encuestaCsat = EncuestaCsat.builder()
                        .empresa(ticket.getEmpresa())
                        .cliente(ticket.getCliente())
                        .contacto(ticket.getContacto())
                        .ticket(ticket)
                        .estado(EstadoEncuestaCsat.PENDIENTE)
                        .fechaEnvio(ahora)
                        .fechaRespuesta(null)
                        .build();

                encuestaCsatDAO.save(encuestaCsat);
            }
        }

        healthScoreService.recalcularYActualizarHealthScore(ticket.getCliente());
    }
}
