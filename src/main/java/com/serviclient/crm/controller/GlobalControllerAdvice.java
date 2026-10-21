package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.dao.EmpresaDAO;
import com.serviclient.crm.dao.UsuarioDAO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UsuarioDAO usuarioDAO;
    private final EmpresaDAO empresaDAO;

    @ModelAttribute("currentUser")
    public CustomUserDetails getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    @ModelAttribute("currentEmpresa")
    public Empresa getCurrentEmpresa(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails && userDetails.getEmpresaId() != null) {
            return empresaDAO.findById(userDetails.getEmpresaId()).orElse(null);
        }
        return empresaDAO.findFirstByOrderByIdAsc().orElse(null);
    }

    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
