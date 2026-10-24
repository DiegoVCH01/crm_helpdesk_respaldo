package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dao.EncuestaSatisfaccionDAO;
import com.serviclient.crm.dao.RenovacionDAO;
import com.serviclient.crm.dto.ClienteDto;
import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.EncuestaSatisfaccion;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.entity.enums.Rol;
import com.serviclient.crm.service.ClienteService;
import com.serviclient.crm.service.CuentaPortalService;
import com.serviclient.crm.service.HealthScoreService;
import com.serviclient.crm.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC para la gestión de clientes B2B.
 *
 * <p>Proporciona operaciones CRUD completas sobre la entidad {@link
 * com.serviclient.crm.entity.Cliente} y expone la vista 360° del cliente con sus KPIs, historial de
 * tickets, encuestas CSAT, renovaciones e interacciones.
 *
 * <p><b>Rutas expuestas:</b>
 *
 * <pre>
 * GET  /clientes                    → listado paginado con filtros y métricas
 * GET  /clientes/nuevo              → formulario de alta
 * POST /clientes                    → persistir nuevo cliente
 * GET  /clientes/{id}               → vista 360° del cliente
 * GET  /clientes/{id}/editar        → formulario de edición
 * POST /clientes/{id}/editar        → actualizar cliente existente
 * POST /clientes/{id}/eliminar      → eliminar cliente
 * POST /clientes/{id}/contactos     → agregar contacto al cliente
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.1.0
 * @see com.serviclient.crm.service.ClienteService
 * @see com.serviclient.crm.service.HealthScoreService
 */
@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {

  private final ClienteService clienteService;
  private final HealthScoreService healthScoreService;
  private final UsuarioService usuarioService;
  private final EncuestaSatisfaccionDAO encuestaSatisfaccionDAO;
  private final RenovacionDAO renovacionDAO;

  // ─────────────────────────────────────────────────────────────────────────
  //  LISTADO
  // ─────────────────────────────────────────────────────────────────────────

  @Operation(
      summary = "Listar clientes",
      description =
          "Obtiene el listado paginado de clientes de la empresa activa con métricas de salud y"
              + " filtros.")
  @GetMapping
  public String listarClientes(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(value = "busqueda", required = false) String busqueda,
      @RequestParam(value = "estado", required = false) EstadoCliente estado,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      Model model) {
    Long empresaId = resolverEmpresaId(userDetails);

    Pageable pageable = PageRequest.of(page, size, Sort.by("nombreComercial").ascending());
    Page<Cliente> clientesPage =
        clienteService.listarClientes(empresaId, busqueda, estado, pageable);
    ClienteService.MetricasClientes metricas = clienteService.obtenerMetricas(empresaId);

    model.addAttribute("clientesPage", clientesPage);
    model.addAttribute("metricas", metricas);
    model.addAttribute("busqueda", busqueda);
    model.addAttribute("estadoSeleccionado", estado);
    model.addAttribute("estados", EstadoCliente.values());
    model.addAttribute("activeMenu", "clientes");

    return "clientes/index";
  }

  @Operation(
      summary = "Exportar clientes a Excel",
      description =
          "Descarga la lista de clientes en formato .xlsx aplicando los filtros actuales.")
  @GetMapping("/exportar")
  public ResponseEntity<byte[]> exportarClientesExcel(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(value = "busqueda", required = false) String busqueda,
      @RequestParam(value = "estado", required = false) EstadoCliente estado) {

    Long empresaId = resolverEmpresaId(userDetails);

    try {
      // Obtiene los bytes del archivo Excel desde el servicio
      byte[] excelContent = clienteService.exportarClientesExcel(empresaId, busqueda, estado);

      // Prepara las cabeceras HTTP para forzar la descarga del archivo
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(
          MediaType.parseMediaType(
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
      headers.setContentDispositionFormData("attachment", "clientes_serviclient.xlsx");
      headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

      return ResponseEntity.ok().headers(headers).body(excelContent);
    } catch (Exception e) {
      log.error("Error al exportar la lista de clientes a Excel", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  CREAR
  // ─────────────────────────────────────────────────────────────────────────

  @Operation(summary = "Formulario de nuevo cliente")
  @GetMapping("/nuevo")
  public String formularioNuevoCliente(
      @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    Long empresaId = resolverEmpresaId(userDetails);
    List<Usuario> usuarios = usuarioService.obtenerTodosPorEmpresa(empresaId);

    model.addAttribute("clienteDto", new ClienteDto());
    model.addAttribute("usuarios", usuarios);
    model.addAttribute("modoEdicion", false);
    model.addAttribute("activeMenu", "clientes");
    return "clientes/form";
  }

  @Operation(summary = "Crear nuevo cliente")
  @PostMapping
  public String guardarCliente(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @ModelAttribute("clienteDto") ClienteDto dto,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {
    Long empresaId = resolverEmpresaId(userDetails);

    if (bindingResult.hasErrors()) {
      model.addAttribute("usuarios", usuarioService.obtenerTodosPorEmpresa(empresaId));
      model.addAttribute("modoEdicion", false);
      model.addAttribute("activeMenu", "clientes");
      return "clientes/form";
    }

    try {
      ClienteService.ResultadoCreacionCliente resultado =
          clienteService.crearCliente(empresaId, dto);
      Cliente guardado = resultado.cliente();
      redirectAttributes.addFlashAttribute(
          "successMessage",
          "Cliente \"" + guardado.getNombreComercial() + "\" creado exitosamente.");

      if (resultado.credencialesPortal() != null) {
        redirectAttributes.addFlashAttribute("portalEmail", resultado.credencialesPortal().email());
        redirectAttributes.addFlashAttribute(
            "portalPasswordTemporal", resultado.credencialesPortal().passwordTemporal());
      }
      return "redirect:/clientes/" + guardado.getId();
    } catch (IllegalArgumentException ex) {
      log.warn("No se pudo registrar el cliente: {}", ex.getMessage());

      model.addAttribute("errorMessage", ex.getMessage());

      model.addAttribute("usuarios", usuarioService.obtenerTodosPorEmpresa(empresaId));

      model.addAttribute("modoEdicion", false);
      model.addAttribute("activeMenu", "clientes");

      return "clientes/form";

    } catch (Exception ex) {
      log.error("Error inesperado al crear cliente", ex);

      model.addAttribute("errorMessage", "Ocurrió un error inesperado al registrar el cliente.");

      model.addAttribute("usuarios", usuarioService.obtenerTodosPorEmpresa(empresaId));

      model.addAttribute("modoEdicion", false);
      model.addAttribute("activeMenu", "clientes");

      return "clientes/form";
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  EDITAR
  // ─────────────────────────────────────────────────────────────────────────

  @Operation(
      summary = "Formulario de edición de cliente",
      description =
          "Carga el formulario pre-relleno con los datos actuales del cliente para su"
              + " modificación.")
  @GetMapping("/{id}/editar")
  public String formularioEditarCliente(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("id") Long id,
      Model model) {
    Long empresaId = resolverEmpresaId(userDetails);
    Cliente cliente = clienteService.obtenerPorIdYEmpresa(id, empresaId);
    ClienteDto dto = clienteService.toDto(cliente);
    List<Usuario> usuarios = usuarioService.obtenerTodosPorEmpresa(empresaId);

    model.addAttribute("clienteDto", dto);
    model.addAttribute("usuarios", usuarios);
    model.addAttribute("modoEdicion", true);
    model.addAttribute("clienteId", id);
    model.addAttribute("activeMenu", "clientes");
    return "clientes/form";
  }

  @Operation(
      summary = "Actualizar cliente existente",
      description = "Valida y persiste los cambios realizados sobre un cliente existente.")
  @PostMapping("/{id}/editar")
  public String actualizarCliente(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("id") Long id,
      @Valid @ModelAttribute("clienteDto") ClienteDto dto,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {
    Long empresaId = resolverEmpresaId(userDetails);

    if (bindingResult.hasErrors()) {
      model.addAttribute("usuarios", usuarioService.obtenerTodosPorEmpresa(empresaId));
      model.addAttribute("modoEdicion", true);
      model.addAttribute("clienteId", id);
      model.addAttribute("activeMenu", "clientes");
      return "clientes/form";
    }

    try {
      Cliente actualizado = clienteService.actualizarCliente(id, empresaId, dto);
      redirectAttributes.addFlashAttribute(
          "successMessage",
          "Cliente \"" + actualizado.getNombreComercial() + "\" actualizado exitosamente.");
      return "redirect:/clientes/" + id;
    } catch (Exception ex) {
      log.error("Error al actualizar cliente id={}", id, ex);
      model.addAttribute("errorMessage", "Error al actualizar: " + ex.getMessage());
      model.addAttribute("usuarios", usuarioService.obtenerTodosPorEmpresa(empresaId));
      model.addAttribute("modoEdicion", true);
      model.addAttribute("clienteId", id);
      model.addAttribute("activeMenu", "clientes");
      return "clientes/form";
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  ELIMINAR
  // ─────────────────────────────────────────────────────────────────────────

  @Operation(
      summary = "Eliminar cliente",
      description = "Elimina permanentemente el cliente y todos sus datos asociados.")
  @PostMapping("/{id}/eliminar")
  public String eliminarCliente(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("id") Long id,
      RedirectAttributes redirectAttributes) {

    Long empresaId = resolverEmpresaId(userDetails);

    try {
      Cliente cliente = clienteService.obtenerPorIdYEmpresa(id, empresaId);
      String nombre = cliente.getNombreComercial();
      clienteService.eliminarCliente(id, empresaId);
      redirectAttributes.addFlashAttribute(
          "successMessage", "Cliente \"" + nombre + "\" eliminado correctamente.");
    } catch (Exception ex) {
      log.error("Error al eliminar cliente id={}", id, ex);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "No se pudo eliminar el cliente: " + ex.getMessage());
    }
    return "redirect:/clientes";
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  VISTA 360°
  // ─────────────────────────────────────────────────────────────────────────

  @Operation(
      summary = "Vista 360° del cliente",
      description =
          "Carga la ficha integral del cliente: KPIs, factores de riesgo, tickets, encuestas y"
              + " contratos.")
  @GetMapping("/{id}")
  public String verCliente360(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("id") Long id,
      @RequestParam(value = "tab", defaultValue = "resumen") String tab,
      Model model) {

    Long empresaId = resolverEmpresaId(userDetails);
    validarAccesoTabCliente360(userDetails, tab);
    Cliente cliente = clienteService.obtenerPorIdYEmpresa(id, empresaId);
    ClienteService.KpisCliente360 kpis = clienteService.obtenerKpisCliente360(cliente);
    List<HealthScoreService.FactorSalud> factores =
        healthScoreService.obtenerFactoresRiesgo(cliente);
    List<EncuestaSatisfaccion> encuestas = encuestaSatisfaccionDAO.findByClienteId(id);
    List<Renovacion> renovaciones = renovacionDAO.findByClienteId(id);
    List<Contacto> contactosActivos = clienteService.obtenerContactosActivos(id);
    List<Contacto> contactosInactivos = clienteService.obtenerContactosInactivos(id);

    model.addAttribute("cliente", cliente);
    model.addAttribute("kpis", kpis);
    model.addAttribute("factores", factores);
    model.addAttribute("encuestas", encuestas);
    model.addAttribute("renovaciones", renovaciones);
    model.addAttribute("activeTab", tab);
    model.addAttribute("activeMenu", "clientes");
    model.addAttribute("contactosActivos", contactosActivos);
    model.addAttribute("contactosInactivos", contactosInactivos);

    return "clientes/cliente360";
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  CONTACTOS
  // ─────────────────────────────────────────────────────────────────────────

  @Operation(summary = "Agregar contacto al cliente")
  @PostMapping("/{clienteId}/contactos")
  public String agregarContacto(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clienteId,
      @ModelAttribute Contacto contacto,
      RedirectAttributes redirectAttributes) {
    Long empresaId = resolverEmpresaId(userDetails);

    try {
      clienteService.obtenerPorIdYEmpresa(clienteId, empresaId);
      ClienteService.ResultadoContacto resultado =
          clienteService.agregarContacto(clienteId, contacto);
      if (resultado.credencialesPortal() != null) {
        redirectAttributes.addFlashAttribute("portalEmail", resultado.credencialesPortal().email());
        redirectAttributes.addFlashAttribute(
            "portalPasswordTemporal", resultado.credencialesPortal().passwordTemporal());
      }
      redirectAttributes.addFlashAttribute("successMessage", "Contacto agregado correctamente.");
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Error al agregar contacto: " + ex.getMessage());
    }
    return "redirect:/clientes/" + clienteId + "?tab=contactos";
  }

  @PostMapping("/{clienteId}/contactos/{contactoId}/editar")
  public String editarContacto(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clienteId,
      @PathVariable Long contactoId,
      @ModelAttribute Contacto contacto,
      RedirectAttributes redirectAttributes) {

    Long empresaId = resolverEmpresaId(userDetails);

    try {
      clienteService.obtenerPorIdYEmpresa(clienteId, empresaId);
      clienteService.actualizarContacto(clienteId, contactoId, contacto);
      redirectAttributes.addFlashAttribute("successMessage", "Contacto actualizado correctamente.");

    } catch (Exception ex) {

      redirectAttributes.addFlashAttribute(
          "errorMessage", "No se pudo actualizar el contacto: " + ex.getMessage());
    }

    return "redirect:/clientes/" + clienteId + "?tab=contactos";
  }

  @PostMapping("/{clienteId}/contactos/{contactoId}/principal")
  public String establecerPrincipal(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clienteId,
      @PathVariable Long contactoId,
      RedirectAttributes redirectAttributes) {

    Long empresaId = resolverEmpresaId(userDetails);

    try {
      clienteService.obtenerPorIdYEmpresa(clienteId, empresaId);
      CuentaPortalService.CredencialesTemporales credenciales =
          clienteService.establecerContactoPrincipal(clienteId, contactoId);

      if (credenciales != null) {
        redirectAttributes.addFlashAttribute("portalEmail", credenciales.email());
        redirectAttributes.addFlashAttribute(
            "portalPasswordTemporal", credenciales.passwordTemporal());
      }
      redirectAttributes.addFlashAttribute(
          "successMessage", "Contacto principal actualizado correctamente.");

    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    }

    return "redirect:/clientes/" + clienteId + "?tab=contactos";
  }

  @PostMapping("/{clienteId}/contactos/{contactoId}/desactivar")
  public String desactivarContacto(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clienteId,
      @PathVariable Long contactoId,
      RedirectAttributes redirectAttributes) {

    Long empresaId = resolverEmpresaId(userDetails);

    try {
      clienteService.obtenerPorIdYEmpresa(clienteId, empresaId);
      clienteService.desactivarContacto(clienteId, contactoId);
      redirectAttributes.addFlashAttribute("successMessage", "Contacto desactivado correctamente.");

    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    }

    return "redirect:/clientes/" + clienteId + "?tab=contactos";
  }

  @PostMapping("/{clienteId}/contactos/{contactoId}/reactivar")
  public String reactivarContacto(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clienteId,
      @PathVariable Long contactoId,
      RedirectAttributes redirectAttributes) {

    Long empresaId = resolverEmpresaId(userDetails);

    try {
      clienteService.obtenerPorIdYEmpresa(clienteId, empresaId);
      CuentaPortalService.CredencialesTemporales credenciales =
          clienteService.reactivarContacto(clienteId, contactoId);

      if (credenciales != null) {
        redirectAttributes.addFlashAttribute("portalEmail", credenciales.email());
        redirectAttributes.addFlashAttribute(
            "portalPasswordTemporal", credenciales.passwordTemporal());
      }
      redirectAttributes.addFlashAttribute("successMessage", "Contacto reactivado correctamente.");

    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    }

    return "redirect:/clientes/" + clienteId + "?tab=contactos";
  }

  @PostMapping("/{clienteId}/contactos/{contactoId}/portal/restablecer")
  public String restablecerCredencialesPortal(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long clienteId,
      @PathVariable Long contactoId,
      RedirectAttributes redirectAttributes) {

    Long empresaId = resolverEmpresaId(userDetails);

    try {
      clienteService.obtenerPorIdYEmpresa(clienteId, empresaId);

      CuentaPortalService.CredencialesTemporales credenciales =
          clienteService.restablecerCredencialesPortal(clienteId, contactoId);
      redirectAttributes.addFlashAttribute(
          "successMessage", "Se generó una nueva contraseña temporal para el Portal Cliente.");
      redirectAttributes.addFlashAttribute("portalEmail", credenciales.email());
      redirectAttributes.addFlashAttribute(
          "portalPasswordTemporal", credenciales.passwordTemporal());
    } catch (Exception ex) {

      redirectAttributes.addFlashAttribute(
          "errorMessage", "No se pudo restablecer el acceso: " + ex.getMessage());
    }

    return "redirect:/clientes/" + clienteId + "?tab=contactos";
  }

  private void validarAccesoTabCliente360(CustomUserDetails userDetails, String tab) {

    Rol rol = userDetails.getRol();

    boolean permitido =
        switch (tab) {
          case "resumen" -> true;
          case "contactos" -> rol == Rol.ADMIN;
          case "tickets" -> rol == Rol.ADMIN || rol == Rol.AGENTE;
          case "actividad" -> rol == Rol.ADMIN || rol == Rol.AGENTE || rol == Rol.SUPERVISOR;
          case "renovacion", "csat" -> rol == Rol.ADMIN || rol == Rol.SUPERVISOR;
          default -> false;
        };
    if (!permitido) {
      throw new AccessDeniedException("No tienes permisos para acceder a esta sección.");
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  //  HELPERS
  // ─────────────────────────────────────────────────────────────────────────

  private Long resolverEmpresaId(CustomUserDetails userDetails) {

    if (userDetails == null || userDetails.getEmpresaId() == null) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "No se pudo identificar la empresa del usuario.");
    }

    return userDetails.getEmpresaId();
  }
}
