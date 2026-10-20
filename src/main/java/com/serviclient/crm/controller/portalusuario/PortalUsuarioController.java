package com.serviclient.crm.controller.portalusuario;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/portalusuario")
public class PortalUsuarioController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Credenciales incorrectas para el portal de usuarios.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Sesión cerrada correctamente.");
        }
        return "portalusuario/login";
    }

    @GetMapping("/cambiar-password")
    public String cambiarPasswordPage() {
        return "portalusuario/cambiar-password";
    }
}