package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CambioPasswordController {

  private final IUsuarioService usuarioService;

  @GetMapping("/cambiar-password")
  public String redirigirLogin() {
    return "redirect:/login";
  }

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
