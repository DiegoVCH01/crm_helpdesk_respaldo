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

/**
 * Filtro de seguridad servlet que intercepta accesos al portal de clientes para forzar el cambio de clave inicial.
 * <p>
 * Verifica si el contacto cliente autenticado con {@code ROLE_CLIENTE} tiene activa la bandera
 * {@code requiereCambioPassword}, restringiendo sus solicitudes y forzando la redirección
 * hacia el formulario de actualización de contraseña del portal.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see org.springframework.web.filter.OncePerRequestFilter
 */
public class CambioPasswordPortalFilter extends OncePerRequestFilter {

    private final CuentaPortalRepository repository;

    /**
     * Construye el filtro inyectando el repositorio de cuentas del portal.
     *
     * @param repository repositorio para verificar el estado de la cuenta portal
     */
    public CambioPasswordPortalFilter(CuentaPortalRepository repository) {
        this.repository = repository;
    }

    /**
     * Evalúa si el cliente autenticado en el portal requiere cambiar su contraseña antes de navegar libremente.
     *
     * @param request petición HTTP entrante
     * @param response respuesta HTTP saliente
     * @param chain cadena de filtros servlet
     * @throws ServletException si ocurre una excepción de servlet
     * @throws IOException si ocurre un fallo de lectura o escritura
     */
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