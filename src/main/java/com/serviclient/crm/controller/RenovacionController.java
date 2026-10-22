package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dto.TicketMensajeDto;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import com.serviclient.crm.service.RenovacionService;
import com.serviclient.crm.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC para el panel de Renovaciones de contratos.
 *
 * <p>Permite visualizar las renovaciones próximas a vencer, agrupadas por rangos de urgencia (0-15
 * días, 16-30 días, 31-60 días), y registrar seguimientos con cambios de estado.
 *
 * <p><b>Rutas expuestas:</b>
 *
 * <pre>
 * GET  /renovaciones             → panel de renovaciones con métricas y alertas
 * POST /renovaciones/seguimiento  → registrar seguimiento de una renovación
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.RenovacionService
 */
@Controller
@RequestMapping("/renovaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Renovaciones",
    description =
        "Control de contratos próximos a vencer, retención de ARR y registro de seguimientos"
            + " comerciales")
public class RenovacionController {

  private final RenovacionService renovacionService;
  private final UsuarioService usuarioService;

  /**
   * Muestra el panel de renovaciones con métricas, listado de renovaciones próximas a vencer y el
   * formulario para registrar seguimientos.
   *
   * @param userDetails principal autenticado para obtener el {@code empresaId}
   * @param model modelo Thymeleaf con renovaciones, agentes y estados
   * @return vista {@code renovaciones/index}
   */
  @Operation(
      summary = "Panel de renovaciones",
      description =
          "Muestra los contratos próximos a vencer con KPIs de valor total en riesgo (ARR),"
              + " contratos críticos (< 15 días) y formulario modal para registrar seguimientos.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Panel de renovaciones cargado exitosamente")
  })
  @GetMapping
  public String listarRenovaciones(
      @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    Long empresaId =
        (userDetails != null && userDetails.getEmpresaId() != null)
            ? userDetails.getEmpresaId()
            : 1L;

    RenovacionService.MetricasRenovaciones metricas = renovacionService.obtenerMetricas(empresaId);
    List<Renovacion> renovaciones = renovacionService.listarProximasRenovaciones(empresaId);
    List<Usuario> agentes = usuarioService.obtenerAgentesYAdminsPorEmpresa(empresaId);

    model.addAttribute("metricas", metricas);
    model.addAttribute("renovaciones", renovaciones);
    model.addAttribute("agentes", agentes);
    model.addAttribute("estadosRenovacion", EstadoRenovacion.values());
    model.addAttribute("nuevoSeguimiento", new TicketMensajeDto.RegistrarSeguimientoRenovacion());
    model.addAttribute("fechaHoy", LocalDate.now());
    model.addAttribute("activeMenu", "renovaciones");

    return "renovaciones/index";
  }

  /**
   * Registra un nuevo seguimiento para una renovación específica. Actualiza el estado de la
   * renovación y persiste el comentario del responsable.
   *
   * @param userDetails principal autenticado
   * @param dto datos del seguimiento a registrar (renovacionId, estado, comentario)
   * @param bindingResult resultado de validaciones
   * @param redirectAttributes atributos flash para el resultado
   * @return redirección al panel de renovaciones
   */
  @Operation(
      summary = "Registrar seguimiento de renovación",
      description =
          "Registra una acción de contacto o negociación sobre un contrato próximo a vencer,"
              + " actualizando su estado (EN_NEGOCIACION, RENOVADO, NO_RENOVADO, CANCELADO).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "302",
        description = "Seguimiento guardado, redirige al panel de renovaciones")
  })
  @PostMapping("/seguimiento")
  public String registrarSeguimiento(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Parameter(description = "Datos del seguimiento de renovación")
          @Valid
          @ModelAttribute("nuevoSeguimiento")
          TicketMensajeDto.RegistrarSeguimientoRenovacion dto,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    Long empresaId =
        (userDetails != null && userDetails.getEmpresaId() != null)
            ? userDetails.getEmpresaId()
            : 1L;
    Usuario autor = usuarioService.obtenerUsuarioAutenticado().orElse(null);

    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Datos incompletos para registrar el seguimiento.");
      return "redirect:/renovaciones";
    }

    try {
      renovacionService.registrarSeguimiento(dto, autor, empresaId);
      redirectAttributes.addFlashAttribute(
          "successMessage", "Seguimiento de renovación guardado exitosamente.");
    } catch (Exception ex) {
      log.error("Error al registrar seguimiento", ex);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Error al guardar seguimiento: " + ex.getMessage());
    }

    return "redirect:/renovaciones";
  }
}
