package com.serviclient.crm.controller.portalusuario;

import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.repository.CuentaPortalRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils; // <-- Import de Apache Commons
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;


@Controller
@RequestMapping("/portalusuario")
@RequiredArgsConstructor
public class PortalUsuarioController {

    private final CuentaPortalRepository cuentaPortalRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Has cerrado sesión correctamente.");
        }
        return "portalusuario/login";
    }

    @PostMapping("/cambiar-password")
public String procesarCambioPassword(
        @RequestParam("nuevaPassword") String nuevaPassword,
        @RequestParam("confirmarPassword") String confirmarPassword,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    boolean esCliente = authentication != null
            && authentication.isAuthenticated()
            && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

    if (!esCliente) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    if (StringUtils.isAnyBlank(nuevaPassword, confirmarPassword)) {
        redirectAttributes.addFlashAttribute(
                "errorCambioPassword", "Debes completar ambos campos.");
        return "redirect:/portalusuario/login";
    }

    if (!StringUtils.equals(nuevaPassword, confirmarPassword)) {
        redirectAttributes.addFlashAttribute(
                "errorCambioPassword", "Las contraseñas no coinciden.");
        return "redirect:/portalusuario/login";
    }

    if (StringUtils.length(nuevaPassword) < 8
            || StringUtils.length(nuevaPassword) > 72) {
        redirectAttributes.addFlashAttribute(
                "errorCambioPassword",
                "La contraseña debe tener entre 8 y 72 caracteres.");
        return "redirect:/portalusuario/login";
    }

    CuentaPortal cuenta = cuentaPortalRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Cuenta no encontrada."));

    if (!cuenta.estaActiva()
            || !Boolean.TRUE.equals(cuenta.getRequiereCambioPassword())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    if (passwordEncoder.matches(nuevaPassword, cuenta.getPasswordHash())) {
        redirectAttributes.addFlashAttribute(
                "errorCambioPassword",
                "La nueva contraseña debe ser diferente a la temporal.");
        return "redirect:/portalusuario/login";
    }

    cuenta.setPasswordHash(passwordEncoder.encode(nuevaPassword));
    cuenta.setRequiereCambioPassword(false);
    cuentaPortalRepository.save(cuenta);

    return "redirect:/portal/tickets/nuevo";
}
}