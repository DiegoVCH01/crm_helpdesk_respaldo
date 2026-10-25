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

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Provider para administradores
  @Bean
  public DaoAuthenticationProvider adminAuthenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  // Provider específico para el Portal de Clientes
  @Bean
  public DaoAuthenticationProvider clienteAuthenticationProvider(
      @Qualifier("clienteUserDetailsService") UserDetailsService clienteUserDetailsService) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(clienteUserDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  // ==========================================
  // FILTRO 1: PORTAL DE CLIENTES
  // ==========================================
  @Bean
  @Order(1)
  public SecurityFilterChain portalClienteSecurityFilterChain(
      HttpSecurity http,
      DaoAuthenticationProvider clienteAuthenticationProvider) throws Exception {

    ProviderManager authManager = new ProviderManager(Collections.singletonList(clienteAuthenticationProvider));

    http
        .securityMatcher("/portalusuario/**", "/portal/**")
        .authenticationManager(authManager)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/portalusuario/login", "/css/**", "/js/**", "/img/**", "/images/**").permitAll()
            .requestMatchers("/portal/**").hasAuthority("ROLE_CLIENTE")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/portalusuario/login")
            .loginProcessingUrl("/portalusuario/login")
            .usernameParameter("username")
            .passwordParameter("password")
            .defaultSuccessUrl("/portal/tickets/nuevo", true)
            .failureUrl("/portalusuario/login?error=true")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/portalusuario/logout")
            .logoutSuccessUrl("/portalusuario/login?logout=true")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll()
        );

    return http.build();
  }

  // ==========================================
  // FILTRO 2: PORTAL ADMINISTRADOR (ORIGINAL)
  // ==========================================
  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http, UsuarioRepository usuarioRepository) throws Exception {
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
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
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/dashboard", true)
                    .failureUrl("/login?error=true")
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll());

    http.authenticationProvider(adminAuthenticationProvider());

    http.addFilterAfter(
        new CambioPasswordObligatorioFilter(usuarioRepository),
        UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}