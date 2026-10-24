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

public class CambioPasswordObligatorioFilter extends OncePerRequestFilter {

  private final UsuarioRepository usuarioRepository;

  public CambioPasswordObligatorioFilter(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

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
