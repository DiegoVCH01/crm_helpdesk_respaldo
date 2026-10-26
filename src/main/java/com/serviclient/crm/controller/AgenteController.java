package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dto.AgenteDto;
import com.serviclient.crm.dto.CredencialTemporalDto;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import com.serviclient.crm.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC para la gestión del equipo de agentes y supervisores.
 *
 * <p>Permite al administrador crear, editar, activar/desactivar agentes
 * y restablecer sus contraseñas. Todas las operaciones están acotadas
 * a la empresa del usuario autenticado.
 *
 * <p><b>Rutas expuestas:</b>
 *
 * <pre>
 * GET  /agentes                         → listado de agentes
 * POST /agentes                         → crear agente
 * POST /agentes/{id}/editar             → actualizar agente
 * POST /agentes/{id}/estado             → activar/desactivar
 * POST /agentes/{id}/restablecer-password → nueva contraseña temporal
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.IUsuarioService
 */
@Controller
@RequestMapping("/agentes")
@RequiredArgsConstructor
@Tag(
    name = "Agentes",
    description = "Gestión del equipo de agentes y supervisores: alta, edición, activación/desactivación y restablecimiento de contraseña")
public class AgenteController {

    private final IUsuarioService usuarioService;

    /**
     * Lista todos los agentes y supervisores de la empresa del usuario autenticado.
     *
     * @param userDetails datos del usuario autenticado
     * @param model modelo Thymeleaf con la lista de agentes y roles disponibles
     * @return vista {@code agentes/index}
     */
    @Operation(summary = "Listar agentes", description = "Muestra el listado de agentes y supervisores activos e inactivos de la empresa")
    @ApiResponse(responseCode = "200", description = "Vista de agentes renderizada correctamente")
    @GetMapping
    public String listar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        model.addAttribute(
                "agentes",
                usuarioService.listarAgentesPorEmpresa(userDetails.getEmpresaId()));
        model.addAttribute("rolesDisponibles", List.of(Rol.AGENTE, Rol.SUPERVISOR));
        return "agentes/index";
    }

    /**
     * Redirige al listado de agentes. Los enlaces antiguos ya no abren una pantalla independiente.
     *
     * @return redirección a {@code /agentes}
     */
    // Los enlaces antiguos ya no deben abrir una pantalla independiente.
    @GetMapping("/nuevo")
    public String nuevo() {
        return "redirect:/agentes";
    }

    /**
     * Valida la existencia del agente y redirige al listado.
     *
     * @param userDetails datos del usuario autenticado
     * @param id identificador del agente a editar
     * @return redirección a {@code /agentes}
     */
    @GetMapping("/{id}/editar")
    public String editar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        usuarioService.obtenerAgentePorEmpresa(id, userDetails.getEmpresaId());
        return "redirect:/agentes";
    }

    /**
     * Crea un nuevo agente o supervisor con contraseña temporal generada automáticamente.
     *
     * @param userDetails datos del usuario autenticado
     * @param dto datos del agente a crear (nombre, email, rol)
     * @param bindingResult resultado de las validaciones del formulario
     * @param redirectAttributes atributos flash para mensajes de éxito o error
     * @return redirección a {@code /agentes}
     */
    @Operation(summary = "Crear agente", description = "Crea un nuevo agente o supervisor con contraseña temporal generada automáticamente")
    @ApiResponse(responseCode = "302", description = "Redirige a /agentes con credenciales temporales en flash")
    @PostMapping
    public String crear(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("agenteDto") AgenteDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            informarErrorFormulario(
                    redirectAttributes, "modalNuevoAgente", dto,
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/agentes";
        }

        try {
            CredencialTemporalDto credencial =
                    usuarioService.crearAgente(userDetails.getEmpresaId(), dto);
            Usuario creado = credencial.getUsuario();

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Agente \"" + creado.getNombreCompleto() + "\" creado correctamente.");
            redirectAttributes.addFlashAttribute("agenteEmail", creado.getEmail());
            redirectAttributes.addFlashAttribute(
                    "agentePasswordTemporal", credencial.getPasswordTemporal());

        } catch (Exception ex) {
            informarErrorFormulario(
                    redirectAttributes, "modalNuevoAgente", dto, ex.getMessage());
        }

        return "redirect:/agentes";
    }

    /**
     * Edita nombre, email y rol de un agente existente.
     *
     * @param userDetails datos del usuario autenticado
     * @param id identificador del agente a actualizar
     * @param dto datos actualizados del agente
     * @param bindingResult resultado de las validaciones
     * @param redirectAttributes atributos flash para mensajes
     * @return redirección a {@code /agentes}
     */
    @Operation(summary = "Actualizar agente", description = "Edita nombre, email y rol de un agente existente")
    @ApiResponse(responseCode = "302", description = "Redirige a /agentes con mensaje de éxito o error")
    @PostMapping("/{id}/editar")
    public String actualizar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @ModelAttribute("agenteDto") AgenteDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        String modalId = "modalEditarAgente-" + id;

        if (bindingResult.hasErrors()) {
            informarErrorFormulario(
                    redirectAttributes, modalId, dto,
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/agentes";
        }

        try {
            usuarioService.actualizarAgente(id, userDetails.getEmpresaId(), dto);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Agente actualizado correctamente.");

        } catch (Exception ex) {
            informarErrorFormulario(redirectAttributes, modalId, dto, ex.getMessage());
        }

        return "redirect:/agentes";
    }

    /**
     * Alterna el estado activo/inactivo de un agente.
     *
     * @param userDetails datos del usuario autenticado
     * @param id identificador del agente
     * @param redirectAttributes atributos flash para el resultado
     * @return redirección a {@code /agentes}
     */
    @Operation(summary = "Activar / Desactivar agente", description = "Alterna el estado activo/inactivo del agente")
    @ApiResponse(responseCode = "302", description = "Redirige a /agentes con mensaje de confirmación")
    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario agente =
                    usuarioService.obtenerAgentePorEmpresa(id, userDetails.getEmpresaId());
            boolean estabaActivo = Boolean.TRUE.equals(agente.getActivo());

            usuarioService.cambiarEstadoAgente(id, userDetails.getEmpresaId());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    estabaActivo ? "Agente desactivado correctamente."
                                : "Agente activado correctamente.");

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/agentes";
    }

    /**
     * Genera una nueva contraseña temporal para el agente y la muestra en pantalla.
     *
     * @param userDetails datos del usuario autenticado
     * @param id identificador del agente
     * @param redirectAttributes atributos flash con la contraseña temporal
     * @return redirección a {@code /agentes}
     */
    @Operation(summary = "Restablecer contraseña de agente", description = "Genera una nueva contraseña temporal y la muestra en pantalla para entregar al agente")
    @ApiResponse(responseCode = "302", description = "Redirige a /agentes con contraseña temporal en flash")
    @PostMapping("/{id}/restablecer-password")
    public String restablecerPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Usuario agente =
                    usuarioService.obtenerAgentePorEmpresa(id, userDetails.getEmpresaId());
            String passwordTemporal = usuarioService.restablecerPasswordAgente(
                    id, userDetails.getEmpresaId());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Contraseña restablecida correctamente.");
            redirectAttributes.addFlashAttribute("agenteEmail", agente.getEmail());
            redirectAttributes.addFlashAttribute(
                    "agentePasswordTemporal", passwordTemporal);

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/agentes";
    }

    /**
     * Prepara los atributos flash para reabrir el modal con el error y los datos del formulario.
     *
     * @param redirectAttributes atributos flash de la redirección
     * @param modalId identificador del modal a reabrir
     * @param dto datos del formulario para preservar
     * @param mensaje mensaje de error a mostrar
     */
    private void informarErrorFormulario(
            RedirectAttributes redirectAttributes,
            String modalId,
            AgenteDto dto,
            String mensaje) {

        redirectAttributes.addFlashAttribute(
                "errorMessage", mensaje != null ? mensaje : "No se pudo guardar el agente.");
        redirectAttributes.addFlashAttribute("modalReabrir", modalId);
        redirectAttributes.addFlashAttribute("agenteFormularioError", dto);
    }
}
