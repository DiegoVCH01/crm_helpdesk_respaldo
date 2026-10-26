package com.serviclient.crm.config.security;

import com.serviclient.crm.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de seguridad servlet que intercepta solicitudes de usuarios internos con credenciales temporales.
 * <p>
 * Si el usuario autenticado tiene marcado el flag {@code debeCambiarPassword}, el filtro restringe
 * su navegación redirigiéndolo obligatoriamente hacia el flujo de cambio de clave antes de concederle
 * acceso al resto de la aplicación.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see org.springframework.web.filter.OncePerRequestFilter
 */
public class CambioPasswordObligatorioFilter extends OncePerRequestFilter {

  private final UsuarioRepository usuarioRepository;

  /**
   * Crea una nueva instancia del filtro inyectando el repositorio de usuarios.
   *
   * @param usuarioRepository repositorio para verificar el estado de la contraseña
   */
  public CambioPasswordObligatorioFilter(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  /**
   * Evalúa cada petición HTTP verificando si el usuario autenticado debe cambiar su clave temporal.
   *
   * @param request petición HTTP entrante
   * @param response respuesta HTTP saliente
   * @param filterChain cadena de filtros de Spring Security
   * @throws ServletException si ocurre un fallo en el procesamiento del servlet
   * @throws IOException si ocurre un error de entrada/salida
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null
        && auth.isAuthenticated()
        && auth.getPrincipal() instanceof CustomUserDetails user) {

      boolean debeCambiar =
          usuarioRepository
              .findById(user.getId())
              .map(u -> Boolean.TRUE.equals(u.getDebeCambiarPassword()))
              .orElse(false);

      if (debeCambiar) {

        String ruta = request.getServletPath();
        String metodo = request.getMethod();

        boolean recursoEstatico = ruta.startsWith("/css/")
            || ruta.startsWith("/js/")
            || ruta.startsWith("/img/")
            || ruta.startsWith("/images/")
            || ruta.startsWith("/webjars/")
            || ruta.startsWith("/assets/")
            || ruta.equals("/favicon.ico");

        boolean permitida = ("GET".equalsIgnoreCase(metodo)
            && (ruta.equals("/login") || recursoEstatico))
            || ("POST".equalsIgnoreCase(metodo)
                && (ruta.equals("/cambiar-password")
                    || ruta.equals("/logout")))
            || ruta.equals("/error");

        if (!permitida) {

          if ("GET".equalsIgnoreCase(metodo)) {
            response.sendRedirect(
                request.getContextPath() + "/login");
          } else {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Debes cambiar tu contraseña primero.");
          }

          return;
        }
      }
    }

    filterChain.doFilter(request, response);
  }
}
