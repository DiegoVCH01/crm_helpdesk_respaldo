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

@Controller
@RequestMapping("/agentes")
@RequiredArgsConstructor
@Tag(
    name = "Agentes",
    description = "Gestión del equipo de agentes y supervisores: alta, edición, activación/desactivación y restablecimiento de contraseña")
public class AgenteController {

    private final IUsuarioService usuarioService;

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

    // Los enlaces antiguos ya no deben abrir una pantalla independiente.
    @GetMapping("/nuevo")
    public String nuevo() {
        return "redirect:/agentes";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        usuarioService.obtenerAgentePorEmpresa(id, userDetails.getEmpresaId());
        return "redirect:/agentes";
    }

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
