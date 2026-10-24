package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.service.IEmpresaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final IEmpresaService empresaService;

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
            return empresaService.obtenerPorId(userDetails.getEmpresaId());
        }
        return empresaService.obtenerPrimera();
    }

    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
