package com.serviclient.crm.config.security;

import com.serviclient.crm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.serviclient.crm.repository.CuentaPortalRepository;
import java.util.Collections;

/**
 * Configuración central de seguridad web y control de acceso basada en Spring
 * Security.
 * <p>
 * Define una arquitectura de doble cadena de filtros de seguridad
 * ({@link SecurityFilterChain}):
 * <ol>
 * <li><b>Cadena de Portal de Clientes (Order 1):</b> Protege las rutas
 * {@code /portal/**} y {@code /portalusuario/**},
 * utilizando {@code ClienteUserDetailsService},
 * {@link DaoAuthenticationProvider} específico y
 * el filtro {@link CambioPasswordPortalFilter}.</li>
 * <li><b>Cadena Principal Administrativa (Order 2):</b> Protege la consola de
 * gestión para usuarios internos
 * (ADMIN, AGENTE, SUPERVISOR), control de acceso granular basado en
 * roles/permisos y
 * el filtro {@link CambioPasswordObligatorioFilter}.</li>
 * </ol>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see org.springframework.security.web.SecurityFilterChain
 * @see CustomUserDetailsService
 * @see ClienteUserDetailsService
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Bean del codificador de contraseñas utilizando el algoritmo seguro BCrypt.
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Proveedor de autenticación DAO para usuarios internos y administradores.
     *
     * @return proveedor configurado con {@code CustomUserDetailsService} y BCrypt
     */
    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Proveedor de autenticación DAO específico para usuarios y contactos del
     * portal de clientes.
     *
     * @param clienteUserDetailsService servicio de detalles de clientes
     * @return proveedor configurado para el portal
     */
    @Bean
    public DaoAuthenticationProvider clienteAuthenticationProvider(
            @Qualifier("clienteUserDetailsService") UserDetailsService clienteUserDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(clienteUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ==========================================
    // FILTRO 1: PORTAL DE CLIENTES
    // ==========================================
    /**
     * Cadena de filtros de seguridad prioritaria para el Portal de Clientes
     * (autoservicio).
     *
     * @param http                          constructor de seguridad HTTP
     * @param clienteAuthenticationProvider proveedor de autenticación de clientes
     * @param cuentaPortalRepository        repositorio de cuentas del portal
     * @return {@link SecurityFilterChain} configurado para el portal
     * @throws Exception ante fallos de configuración
     */
    @Bean
    @Order(1)
    public SecurityFilterChain portalClienteSecurityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider clienteAuthenticationProvider, CuentaPortalRepository cuentaPortalRepository)
            throws Exception {

        ProviderManager authManager = new ProviderManager(Collections.singletonList(clienteAuthenticationProvider));

        http
                .securityMatcher("/portalusuario/**", "/portal/**")
                .authenticationManager(authManager)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/portalusuario/login", "/css/**", "/js/**", "/img/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/portal/**").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/portalusuario/cambiar-password")
                        .hasAuthority("ROLE_CLIENTE")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/portalusuario/login")
                        .loginProcessingUrl("/portalusuario/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {

                            boolean debeCambiar = cuentaPortalRepository
                                    .findByEmail(authentication.getName())
                                    .map(cuenta -> Boolean.TRUE.equals(
                                            cuenta.getRequiereCambioPassword()))
                                    .orElse(true);

                            String destino = debeCambiar
                                    ? "/portalusuario/login"
                                    : "/portal/tickets/nuevo";

                            response.sendRedirect(request.getContextPath() + destino);
                        })
                        .failureUrl("/portalusuario/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/portalusuario/logout")
                        .logoutSuccessUrl("/portalusuario/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        http.addFilterAfter(
                new CambioPasswordPortalFilter(cuentaPortalRepository),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ==========================================
    // FILTRO 2: PORTAL ADMINISTRADOR (ORIGINAL)
    // ==========================================
    /**
     * Cadena de filtros de seguridad principal para el CRM y consola
     * administrativa.
     *
     * @param http              constructor de seguridad HTTP
     * @param usuarioRepository repositorio de usuarios para el filtro de cambio
     *                          obligatorio de clave
     * @return {@link SecurityFilterChain} configurado para el CRM administrativo
     * @throws Exception ante fallos de configuración
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http, UsuarioRepository usuarioRepository) throws Exception {
        http.authenticationManager(
                new ProviderManager(
                        Collections.singletonList(adminAuthenticationProvider())));
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                                .permitAll()
                                .requestMatchers(
                                        "/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**", "/favicon.ico")
                                .permitAll()
                                .requestMatchers("/login", "/registro/**", "/h2-console/**")
                                .permitAll()
                                .requestMatchers("/portalusuario/**")
                                .permitAll()
                                // Swagger / OpenAPI
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs.yaml")
                                .permitAll()

                                // CONTROL DE ACCESO POR ROLES
                                .requestMatchers("/clientes/*/contactos/**")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/clientes/exportar")
                                .hasAnyRole("ADMIN", "SUPERVISOR")

                                .requestMatchers("/clientes/nuevo")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.POST, "/clientes")
                                .hasRole("ADMIN")

                                .requestMatchers("/clientes/*/editar")
                                .hasRole("ADMIN")

                                .requestMatchers("/clientes/*/eliminar")
                                .hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/clientes", "/clientes/*")
                                .hasAnyRole("ADMIN", "AGENTE", "SUPERVISOR")

                                .requestMatchers("/clientes/**")
                                .hasRole("ADMIN")

                                .requestMatchers("/tickets/**")
                                .hasAnyRole("ADMIN", "AGENTE")

                                .requestMatchers("/agentes/**")
                                .hasRole("ADMIN")
                                .anyRequest()
                                .authenticated())
                .formLogin(
                        form -> form.loginPage("/login")
                                .loginProcessingUrl("/login")
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .defaultSuccessUrl("/dashboard", true)
                                .failureUrl("/login?error=true")
                                .permitAll())
                .logout(
                        logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout=true")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll());

        http.addFilterAfter(
                new CambioPasswordObligatorioFilter(usuarioRepository),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}