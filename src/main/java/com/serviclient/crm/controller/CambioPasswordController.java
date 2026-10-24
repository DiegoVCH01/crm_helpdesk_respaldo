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

@Controller
@RequiredArgsConstructor
@Tag(
    name = "Perfil de Usuario",
    description = "Operaciones de perfil: cambio de contraseña temporal tras el primer inicio de sesión")
public class CambioPasswordController {

  private final IUsuarioService usuarioService;

  @GetMapping("/cambiar-password")
  public String redirigirLogin() {
    return "redirect:/login";
  }

  @Operation(summary = "Cambiar contraseña temporal", description = "Permite al usuario establecer una nueva contraseña cuando se le ha asignado una temporal. Redirige al dashboard si el cambio es exitoso")
  @ApiResponse(responseCode = "302", description = "Redirige a /dashboard si el cambio es exitoso, o a /login si hay error")
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
