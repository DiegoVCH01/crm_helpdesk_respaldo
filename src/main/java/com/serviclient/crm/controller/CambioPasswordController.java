package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC para el cambio de contraseña temporal.
 *
 * <p>Permite a los usuarios que tienen una contraseña temporal asignada
 * establecer una nueva contraseña personal. Tras el cambio exitoso,
 * redirige al dashboard principal.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.IUsuarioService#cambiarPasswordTemporal(Long, String, String)
 */
@Controller
@RequiredArgsConstructor
@Tag(
    name = "Perfil de Usuario",
    description = "Operaciones de perfil: cambio de contraseña temporal tras el primer inicio de sesión")
public class CambioPasswordController {

  private final IUsuarioService usuarioService;

  /**
   * Redirige las peticiones GET de cambio de contraseña al formulario de login,
   * donde se muestra el modal de cambio de contraseña si aplica.
   *
   * @return redirección a {@code /login}
   */
  @GetMapping("/cambiar-password")
  public String redirigirLogin() {
    return "redirect:/login";
  }

  @Operation(summary = "Cambiar contraseña temporal", description = "Permite al usuario establecer una nueva contraseña cuando se le ha asignado una temporal. Redirige al dashboard si el cambio es exitoso")
  @ApiResponse(responseCode = "302", description = "Redirige a /dashboard si el cambio es exitoso, o a /login si hay error")
  /**
   * Procesa el cambio de contraseña temporal del usuario autenticado.
   *
   * @param userDetails datos del usuario autenticado
   * @param nuevaPassword nueva contraseña a establecer
   * @param confirmarPassword confirmación de la nueva contraseña
   * @param redirectAttributes atributos flash para mensajes de éxito o error
   * @return redirección a {@code /dashboard} si el cambio es exitoso, o a {@code /login} si hay error
   */
  @PostMapping("/cambiar-password")
  public String cambiarPassword(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam String nuevaPassword,
      @RequestParam String confirmarPassword,
      RedirectAttributes redirectAttributes) {

    try {
      usuarioService.cambiarPasswordTemporal(userDetails.getId(), nuevaPassword, confirmarPassword);

      redirectAttributes.addFlashAttribute(
          "successMessage", "Tu contraseña se cambió correctamente.");

    } catch (IllegalArgumentException ex) {
      redirectAttributes.addFlashAttribute("errorCambioPassword", ex.getMessage());

      return "redirect:/login";
    }

    return "redirect:/dashboard";
  }
}
