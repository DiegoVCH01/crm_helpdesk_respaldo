package com.serviclient.crm.config.security;

import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.repository.CuentaPortalRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class CambioPasswordPortalFilter extends OncePerRequestFilter {

    private final CuentaPortalRepository repository;

    public CambioPasswordPortalFilter(CuentaPortalRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        boolean esCliente = auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

        if (esCliente) {
            CuentaPortal cuenta = repository
                    .findByEmail(auth.getName())
                    .orElse(null);

            if (cuenta == null || !cuenta.estaActiva()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (Boolean.TRUE.equals(cuenta.getRequiereCambioPassword())) {
                String ruta = request.getServletPath();
                String metodo = request.getMethod();

                boolean permitida =
                        (ruta.equals("/portalusuario/login")
                                && metodo.equalsIgnoreCase("GET"))
                        || (ruta.equals("/portalusuario/cambiar-password")
                                && metodo.equalsIgnoreCase("POST"))
                        || (ruta.equals("/portalusuario/logout")
                                && metodo.equalsIgnoreCase("POST"));

                if (!permitida) {
                    if (metodo.equalsIgnoreCase("GET")) {
                        response.sendRedirect(
                                request.getContextPath()
                                + "/portalusuario/login");
                    } else {
                        response.sendError(
                                HttpServletResponse.SC_FORBIDDEN);
                    }
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}