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


/**
 * Controlador MVC para la autenticación del Portal de Clientes.
 *
 * <p>Gestiona el inicio de sesión y el cambio de contraseña obligatorio
 * para los usuarios con rol {@code ROLE_CLIENTE} que acceden al portal
 * externo de la plataforma.
 *
 * <p><b>Rutas expuestas:</b>
 *
 * <pre>
 * GET  /portalusuario/login            → formulario de login del portal
 * POST /portalusuario/cambiar-password → cambio de contraseña temporal
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Controller
@RequestMapping("/portalusuario")
@RequiredArgsConstructor
public class PortalUsuarioController {

    private final CuentaPortalRepository cuentaPortalRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Muestra la página de login del portal de clientes con mensajes contextuales.
     * Si el usuario ya está autenticado como cliente, verifica si requiere cambio
     * de contraseña; si no, redirige al formulario de nuevo ticket.
     *
     * @param error presente cuando las credenciales son inválidas
     * @param logout presente tras un cierre de sesión exitoso
     * @param authentication información de autenticación actual
     * @param model modelo Thymeleaf para pasar mensajes a la vista
     * @return vista {@code portalusuario/login} o redirección a {@code /portal/tickets/nuevo}
     */
    @GetMapping("/login")
public String loginPage(
        @RequestParam(value = "error", required = false) String error,
        @RequestParam(value = "logout", required = false) String logout,
        Authentication authentication,
        Model model) {

    if (error != null) {
        model.addAttribute("errorMessage",
                "Usuario o contraseña incorrectos.");
    }

    if (logout != null) {
        model.addAttribute("successMessage",
                "Has cerrado sesión correctamente.");
    }

    boolean esCliente = authentication != null
            && authentication.isAuthenticated()
            && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

    if (esCliente) {
        CuentaPortal cuenta = cuentaPortalRepository
                .findByEmail(authentication.getName())
                .filter(CuentaPortal::estaActiva)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN));

        if (Boolean.TRUE.equals(cuenta.getRequiereCambioPassword())) {
            model.addAttribute("debeCambiarPassword", true);
        } else {
            return "redirect:/portal/tickets/nuevo";
        }
    }

    return "portalusuario/login";
}

    /**
     * Procesa el cambio de contraseña temporal del cliente del portal.
     * Valida longitud, coincidencia y que no sea igual a la contraseña temporal actual.
     *
     * @param nuevaPassword nueva contraseña a establecer
     * @param confirmarPassword confirmación de la nueva contraseña
     * @param authentication información de autenticación actual
     * @param redirectAttributes atributos flash para mensajes de error
     * @return redirección a {@code /portal/tickets/nuevo} si es exitoso,
     *         o a {@code /portalusuario/login} si hay error
     * @throws ResponseStatusException si el usuario no tiene rol {@code ROLE_CLIENTE}
     */
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