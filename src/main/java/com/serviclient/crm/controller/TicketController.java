package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dto.TicketDto;
import com.serviclient.crm.dto.TicketMensajeDto;
import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.service.ClienteService;
import com.serviclient.crm.service.TicketService;
import com.serviclient.crm.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC para la Mesa de Ayuda (Helpdesk).
 *
 * <p>Gestiona el ciclo de vida completo de los tickets de soporte: creación, visualización,
 * mensajería, asignación de agentes, cambios de estado/prioridad y cierre con envío opcional de
 * encuesta CSAT.
 *
 * <p><b>Rutas expuestas:</b>
 *
 * <pre>
 * GET  /tickets                    → listado paginado con KPIs y filtros
 * GET  /tickets/nuevo               → formulario de nuevo ticket
 * POST /tickets                    → crear ticket
 * GET  /tickets/{id}               → detalle con conversación y SLA
 * POST /tickets/{id}/mensajes       → enviar mensaje o nota interna
 * POST /tickets/{id}/asignar        → reasignar agente responsable
 * POST /tickets/{id}/cambiar-estado → cambiar estado y/o prioridad
 * POST /tickets/{id}/cerrar         → cerrar ticket (dispara CSAT si aplica)
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.TicketService
 */
@Controller
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Mesa de Ayuda (Tickets)",
    description =
        "Ciclo de vida de tickets: creación, asignación, mensajería, gestión de SLA y resolución")
public class TicketController {

  private final TicketService ticketService;
  private final ClienteService clienteService;
  private final UsuarioService usuarioService;

  /**
   * Lista los tickets de la empresa con soporte de búsqueda y filtros múltiples. Calcula KPIs de la
   * mesa de ayuda (abiertos, en proceso, críticos, fuera de SLA).
   *
   * @param userDetails principal autenticado
   * @param busqueda búsqueda por número de ticket o asunto (opcional)
   * @param estado filtro por {@link com.serviclient.crm.entity.enums.EstadoTicket} (opcional)
   * @param prioridad filtro por {@link com.serviclient.crm.entity.enums.PrioridadTicket} (opcional)
   * @param categoria filtro por {@link com.serviclient.crm.entity.enums.CategoriaTicket} (opcional)
   * @param page número de página (0-indexed)
   * @param size tamaño de página
   * @param model modelo Thymeleaf
   * @return vista {@code helpdesk/index}
   */
  @Operation(
      summary = "Listar tickets de soporte",
      description =
          "Obtiene el listado paginado de tickets de la empresa con filtros combinados por"
              + " código/asunto, estado, prioridad y categoría, junto con métricas agregadas del"
              + " Helpdesk (abiertos, SLA vencido, etc.).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Bandeja de tickets renderizada correctamente")
  })
  @GetMapping
  public String listarTickets(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "Búsqueda por código de ticket (ej. TCK-2024-001) o asunto")
          @RequestParam(value = "busqueda", required = false)
          String busqueda,
      @Parameter(
              description =
                  "Filtro por estado (NUEVO, ASIGNADO, EN_PROCESO, ESPERANDO_CLIENTE, RESUELTO,"
                      + " CERRADO)")
          @RequestParam(value = "estado", required = false)
          EstadoTicket estado,
      @Parameter(description = "Filtro por nivel de prioridad (BAJA, MEDIA, ALTA, CRITICA)")
          @RequestParam(value = "prioridad", required = false)
          PrioridadTicket prioridad,
      @Parameter(
              description =
                  "Filtro por categoría (SOPORTE_TECNICO, FACTURACION, CONSULTA, RECLAMO, etc.)")
          @RequestParam(value = "categoria", required = false)
          CategoriaTicket categoria,
      @Parameter(description = "Número de página (0-indexed)", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "Cantidad de tickets por página", example = "10")
          @RequestParam(value = "size", defaultValue = "10")
          int size,
      Model model) {
    Long empresaId = resolverEmpresaId(userDetails);

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Ticket> ticketsPage =
        ticketService.listarTickets(empresaId, busqueda, estado, prioridad, categoria, pageable);
    TicketService.MetricasTickets metricas = ticketService.obtenerMetricas(empresaId);

    model.addAttribute("ticketsPage", ticketsPage);
    model.addAttribute("metricas", metricas);
    model.addAttribute("busqueda", busqueda);
    model.addAttribute("estadoSeleccionado", estado);
    model.addAttribute("prioridadSeleccionada", prioridad);
    model.addAttribute("categoriaSeleccionada", categoria);

    model.addAttribute("estados", EstadoTicket.values());
    model.addAttribute("prioridades", PrioridadTicket.values());
    model.addAttribute("categorias", CategoriaTicket.values());
    model.addAttribute("activeMenu", "tickets");

    return "helpdesk/index";
  }

  /**
   * Muestra el formulario de creación de un nuevo ticket. Carga listas de clientes, agentes,
   * categorías y prioridades.
   *
   * @param userDetails principal autenticado
   * @param model modelo Thymeleaf
   * @return vista {@code helpdesk/form}
   */
  @Operation(
      summary = "Formulario de nuevo ticket",
      description =
          "Renderiza el formulario para registrar un nuevo ticket de soporte, cargando la lista de"
              + " clientes corporativos y agentes de soporte disponibles.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Formulario de creación renderizado correctamente")
  })
  @GetMapping("/nuevo")
  public String formularioNuevoTicket(
      @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    Long empresaId = resolverEmpresaId(userDetails);
    List<Cliente> clientes = clienteService.listarTodosPorEmpresa(empresaId);
    List<Usuario> agentes = usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId);

    model.addAttribute("ticketDto", new TicketDto());
    model.addAttribute("clientes", clientes);
    model.addAttribute("agentes", agentes);
    model.addAttribute("categorias", CategoriaTicket.values());
    model.addAttribute("prioridades", PrioridadTicket.values());
    model.addAttribute("activeMenu", "tickets");

    return "helpdesk/form";
  }

  /**
   * Persiste un nuevo ticket. Calcula automáticamente los límites de SLA según la prioridad y crea
   * el historial inicial del ticket.
   *
   * @param userDetails principal autenticado
   * @param dto datos del ticket
   * @param bindingResult resultado de las validaciones
   * @param model modelo Thymeleaf
   * @param redirectAttributes atributos flash
   * @return redirección a {@code /tickets/{id}} o al formulario si hay errores
   */
  @Operation(
      summary = "Crear nuevo ticket",
      description =
          "Crea un ticket de soporte calculando automáticamente los tiempos máximos de primera"
              + " respuesta y resolución basados en el SLA configurado.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "302",
        description = "Ticket creado exitosamente, redirige a la vista de detalle"),
    @ApiResponse(responseCode = "200", description = "Errores de validación en el formulario")
  })
  @PostMapping
  public String guardarTicket(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @ModelAttribute("ticketDto") TicketDto dto,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {
    Long empresaId = resolverEmpresaId(userDetails);
    Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);

    if (bindingResult.hasErrors()) {
      model.addAttribute("clientes", clienteService.listarTodosPorEmpresa(empresaId));
      model.addAttribute("agentes", usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId));
      model.addAttribute("categorias", CategoriaTicket.values());
      model.addAttribute("prioridades", PrioridadTicket.values());
      model.addAttribute("activeMenu", "tickets");
      return "helpdesk/form";
    }

    try {
      Ticket guardado = ticketService.crearTicket(empresaId, dto, autor);
      redirectAttributes.addFlashAttribute(
          "successMessage", "Ticket " + guardado.getCodigo() + " creado exitosamente.");
      return "redirect:/tickets/" + guardado.getId();
    } catch (Exception ex) {
      log.error("Error al crear ticket", ex);
      model.addAttribute("errorMessage", "Error al crear ticket: " + ex.getMessage());
      model.addAttribute("clientes", clienteService.listarTodosPorEmpresa(empresaId));
      model.addAttribute("agentes", usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId));
      model.addAttribute("categorias", CategoriaTicket.values());
      model.addAttribute("prioridades", PrioridadTicket.values());
      model.addAttribute("activeMenu", "tickets");
      return "helpdesk/form";
    }
  }

  /**
   * Muestra el detalle completo de un ticket: conversación ordenada cronológicamente, indicadores
   * de SLA, historial de cambios y controles de acción.
   *
   * @param userDetails principal autenticado
   * @param id identificador del ticket
   * @param model modelo Thymeleaf
   * @return vista {@code helpdesk/detalle}
   */
  @Operation(
      summary = "Detalle del ticket",
      description =
          "Muestra la vista completa del ticket: hilo de conversación, notas internas, estado de"
              + " SLA con cuenta regresiva, historial de auditoría y opciones de reasignación.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Detalle del ticket renderizado"),
    @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
  })
  @GetMapping("/{id}")
  public String verDetalleTicket(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "ID único del ticket", example = "1") @PathVariable("id") Long id,
      Model model) {
    Long empresaId = resolverEmpresaId(userDetails);
    Ticket ticket = ticketService.obtenerPorIdYEmpresa(id, empresaId);
    List<Usuario> agentes = usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId);

    model.addAttribute("ticket", ticket);
    model.addAttribute("agentes", agentes);
    model.addAttribute("estados", EstadoTicket.values());
    model.addAttribute("prioridades", PrioridadTicket.values());
    model.addAttribute("nuevoMensaje", new TicketMensajeDto.EnviarMensaje());
    model.addAttribute("cambioEstado", new TicketMensajeDto.CambiarEstado());
    model.addAttribute("cierreTicket", new TicketMensajeDto.CerrarTicket());
    model.addAttribute("activeMenu", "tickets");

    return "helpdesk/detalle";
  }

  /**
   * Envía un mensaje o nota interna en el ticket. Los mensajes de agente son visibles para el
   * cliente; las notas internas no. Registra la primera respuesta si aún no se había dado.
   *
   * @param ticketId identificador del ticket
   * @param contenido texto del mensaje
   * @param esNotaInterna {@code true} para nota interna, {@code false} para mensaje al cliente
   * @param redirectAttributes atributos flash para el resultado
   * @return redirección al detalle del ticket
   */
  @Operation(
      summary = "Enviar mensaje o nota interna",
      description =
          "Agrega una respuesta pública o un comentario interno confidencial en el hilo de"
              + " conversación del ticket, actualizando la métrica de tiempo de primera respuesta"
              + " si aplica.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "302",
        description = "Mensaje publicado, redirige al detalle del ticket")
  })
  @PostMapping("/{id}/mensajes")
  public String enviarMensaje(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "ID del ticket", example = "1") @PathVariable("id") Long ticketId,
      @Parameter(description = "Texto del mensaje o comentario") @RequestParam("contenido")
          String contenido,
      @Parameter(
              description =
                  "Indica si el mensaje es una nota interna privada solo para el equipo de soporte")
          @RequestParam(value = "esNotaInterna", defaultValue = "false")
          boolean esNotaInterna,
      RedirectAttributes redirectAttributes) {
    Long empresaId = resolverEmpresaId(userDetails);
    Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);
    try {
      ticketService.obtenerPorIdYEmpresa(ticketId, empresaId);
      if (contenido != null && !contenido.trim().isBlank()) {
        ticketService.enviarMensaje(ticketId, autor, contenido.trim(), esNotaInterna);
        redirectAttributes.addFlashAttribute(
            "successMessage",
            esNotaInterna ? "Comentario interno guardado." : "Respuesta enviada al cliente.");
      }
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Error al enviar mensaje: " + ex.getMessage());
    }
    return "redirect:/tickets/" + ticketId;
  }

  /**
   * Reasigna el agente responsable del ticket y registra el evento en el historial.
   *
   * @param ticketId identificador del ticket
   * @param agenteId identificador del nuevo agente responsable
   * @param redirectAttributes atributos flash
   * @return redirección al detalle del ticket
   */
  @Operation(
      summary = "Reasignar agente responsable",
      description =
          "Cambia el especialista o agente asignado a la resolución del ticket y genera una entrada"
              + " en la bitácora de auditoría.")
  @ApiResponses({
    @ApiResponse(responseCode = "302", description = "Agente reasignado, redirige al detalle")
  })
  @PostMapping("/{id}/asignar")
  public String asignarAgente(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "ID del ticket", example = "1") @PathVariable("id") Long ticketId,
      @Parameter(description = "ID del nuevo usuario/agente responsable", example = "2")
          @RequestParam("agenteId")
          Long agenteId,
      RedirectAttributes redirectAttributes) {
    Long empresaId = resolverEmpresaId(userDetails);
    Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);
    try {
      ticketService.obtenerPorIdYEmpresa(ticketId, empresaId);
      ticketService.asignarAgente(ticketId, agenteId, autor);
      redirectAttributes.addFlashAttribute("successMessage", "Agente reasignado exitosamente.");
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Error al asignar agente: " + ex.getMessage());
    }
    return "redirect:/tickets/" + ticketId;
  }

  /**
   * Cambia el estado y/o la prioridad del ticket, con un comentario opcional que se registra en el
   * historial.
   *
   * @param ticketId identificador del ticket
   * @param estado nuevo estado (opcional si solo se cambia la prioridad)
   * @param prioridad nueva prioridad (opcional)
   * @param comentario motivo del cambio (opcional)
   * @param redirectAttributes atributos flash
   * @return redirección al detalle del ticket
   */
  @Operation(
      summary = "Cambiar estado y/o prioridad",
      description =
          "Actualiza el estado del flujo de trabajo y/o la urgencia del ticket, registrando el"
              + " motivo del cambio en el historial.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "302",
        description = "Estado/prioridad actualizado, redirige al detalle")
  })
  @PostMapping("/{id}/cambiar-estado")
  public String cambiarEstado(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "ID del ticket", example = "1") @PathVariable("id") Long ticketId,
      @Parameter(description = "Nuevo estado del ticket")
          @RequestParam(value = "estado", required = false)
          EstadoTicket estado,
      @Parameter(description = "Nueva prioridad del ticket")
          @RequestParam(value = "prioridad", required = false)
          PrioridadTicket prioridad,
      @Parameter(description = "Comentario justificativo del cambio")
          @RequestParam(value = "comentario", required = false)
          String comentario,
      RedirectAttributes redirectAttributes) {
    Long empresaId = resolverEmpresaId(userDetails);
    Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);
    try {
      ticketService.obtenerPorIdYEmpresa(ticketId, empresaId);
      ticketService.cambiarEstadoYPrioridad(ticketId, estado, prioridad, comentario, autor);
      redirectAttributes.addFlashAttribute(
          "successMessage", "Estado/Prioridad actualizado correctamente.");
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Error al actualizar estado: " + ex.getMessage());
    }
    return "redirect:/tickets/" + ticketId;
  }

  /**
   * Cierra el ticket, opcionalmente enviando una encuesta CSAT al contacto. Registra la fecha de
   * cierre y el historial de la acción.
   *
   * @param ticketId identificador del ticket a cerrar
   * @param enviarCsat si {@code true}, crea el registro de encuesta CSAT pendiente
   * @param comentarioCierre comentario de cierre registrado en el historial
   * @param redirectAttributes atributos flash
   * @return redirección al detalle del ticket
   */
  @Operation(
      summary = "Cerrar ticket de soporte",
      description =
          "Finaliza el ticket de soporte, calcula el tiempo total de resolución frente a los SLA"
              + " comprometidos y opcionalmente dispara el envío de la encuesta de satisfacción"
              + " CSAT al cliente.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "302",
        description = "Ticket cerrado exitosamente, redirige al detalle")
  })
  @PostMapping("/{id}/cerrar")
  public String cerrarTicket(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "ID del ticket a cerrar", example = "1") @PathVariable("id")
          Long ticketId,
      @Parameter(
              description = "Indica si se debe generar una encuesta de satisfacción CSAT",
              example = "true")
          @RequestParam(value = "enviarCsat", defaultValue = "true")
          Boolean enviarCsat,
      @Parameter(description = "Nota de resolución o cierre del ticket")
          @RequestParam(value = "comentarioCierre", required = false)
          String comentarioCierre,
      RedirectAttributes redirectAttributes) {
    Long empresaId = resolverEmpresaId(userDetails);
    Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);
    try {
      ticketService.obtenerPorIdYEmpresa(ticketId, empresaId);
      ticketService.cerrarTicket(ticketId, enviarCsat, comentarioCierre, autor);
      redirectAttributes.addFlashAttribute("successMessage", "Ticket cerrado satisfactoriamente.");
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Error al cerrar ticket: " + ex.getMessage());
    }
    return "redirect:/tickets/" + ticketId;
  }

  private Long resolverEmpresaId(CustomUserDetails userDetails) {

    if (userDetails == null || userDetails.getEmpresaId() == null) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "No se pudo identificar la empresa del usuario.");
    }

    return userDetails.getEmpresaId();
  }
}
