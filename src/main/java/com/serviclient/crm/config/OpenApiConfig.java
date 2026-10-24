package com.serviclient.crm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración central de OpenAPI 3.0 (Swagger) para ServiClient CRM Helpdesk.
 *
 * <p>Expone la documentación interactiva en las siguientes rutas:</p>
 * <ul>
 *   <li><b>Swagger UI</b>: {@code /swagger-ui.html}</li>
 *   <li><b>JSON spec</b>: {@code /v3/api-docs}</li>
 *   <li><b>YAML spec</b>: {@code /v3/api-docs.yaml}</li>
 * </ul>
 *
 * <p>El esquema de seguridad configurado es <em>Session Cookie</em> (autenticación
 * basada en formulario de Spring Security), compatible con la arquitectura
 * monolítica Thymeleaf + Spring MVC del proyecto.</p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @since 2026-09-28
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * Define el bean principal {@link OpenAPI} que configura metadatos de la API,
     * servidores disponibles, esquemas de seguridad y agrupación de tags.
     *
     * @return instancia de {@link OpenAPI} completamente configurada
     */
    @Bean
    public OpenAPI serviclientOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(externalDocs())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor Local de Desarrollo"),
                        new Server()
                                .url("https://crm.serviclient.com")
                                .description("Servidor de Producción (referencia)")
                ))
                .components(new Components()
                        .addSecuritySchemes("session-cookie", sessionCookieScheme())
                        .addSecuritySchemes("csrf-token", csrfScheme())
                )
                .addSecurityItem(new SecurityRequirement().addList("session-cookie"))
                .tags(List.of(
                        new Tag().name("Autenticación")
                                .description("Registro de empresa (wizard 3 pasos) e inicio/cierre de sesión"),
                        new Tag().name("Clientes")
                                .description("Gestión de clientes B2B: listado, vista 360°, contactos, Health Score y exportación Excel"),
                        new Tag().name("Mesa de Ayuda (Tickets)")
                                .description("Ciclo de vida de tickets: creación, asignación, mensajería, gestión de SLA y resolución"),
                        new Tag().name("Renovaciones")
                                .description("Control de contratos próximos a vencer, retención de ARR y registro de seguimientos comerciales"),
                        new Tag().name("Dashboard")
                                .description("Panel principal interactivo con KPIs de clientes, tickets, SLA, CSAT y renovaciones"),
                        new Tag().name("Agentes")
                                .description("Gestión del equipo de agentes y supervisores: alta, edición, activación/desactivación y restablecimiento de contraseña"),
                        new Tag().name("Portal de Clientes")
                                .description("Acceso externo para que los clientes finales creen incidencias y consulten el estado de sus tickets"),
                        new Tag().name("Perfil de Usuario")
                                .description("Operaciones de perfil: cambio de contraseña temporal tras el primer inicio de sesión"),
                        new Tag().name("Actuator")
                                .description("Endpoints de monitoreo y salud de la aplicación (Spring Actuator)")
                ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Construye el bloque {@link Info} con título, descripción, versión,
     * datos de contacto y licencia de la API.
     *
     * @return objeto {@link Info} para el spec OpenAPI
     */
    private Info apiInfo() {
        return new Info()
                .title("ServiClient CRM Helpdesk API")
                .description("""
                        **ServiClient** es un CRM académico orientado a empresas de servicios B2B.
                        
                        Permite gestionar el ciclo de vida del cliente: desde el registro y onboarding,
                        hasta el seguimiento de tickets de soporte, control de SLA, encuestas CSAT
                        y gestión de renovaciones de contratos.
                        
                        ---
                        
                        ### Módulos disponibles
                        | Módulo               | Ruta base                  |
                        |----------------------|----------------------------|
                        | Autenticación        | `/login`, `/registro`      |
                        | Clientes             | `/clientes`                |
                        | Helpdesk / Tickets   | `/tickets`                 |
                        | Renovaciones         | `/renovaciones`            |
                        | Dashboard            | `/dashboard`               |
                        | Agentes              | `/agentes`                 |
                        | Portal de Clientes   | `/portal/tickets`          |
                        | Perfil de Usuario    | `/cambiar-password`        |
                        
                        ### Autenticación
                        Esta API utiliza **sesiones HTTP** gestionadas por Spring Security.
                        Inicia sesión en `/login` con `POST` (form-data: `username` y `password`)
                        antes de llamar a cualquier endpoint protegido.
                        
                        **Credenciales demo:** `admin@serviclient.com` / `admin123`
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("ServiClient Dev Team")
                        .email("dev@serviclient.com")
                        .url("https://serviclient.com"))
                .license(new License()
                        .name("Uso Académico / MIT")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Enlace a la documentación externa del proyecto (GitHub / Wiki).
     *
     * @return {@link ExternalDocumentation} con URL del repositorio
     */
    private ExternalDocumentation externalDocs() {
        return new ExternalDocumentation()
                .description("Repositorio y Wiki del proyecto en GitHub")
                .url("https://github.com/serviclient/crm-helpdesk");
    }

    /**
     * Esquema de seguridad basado en cookies de sesión (JSESSIONID).
     * Compatible con la autenticación form-login de Spring Security.
     *
     * @return {@link SecurityScheme} de tipo API Key (cookie)
     */
    private SecurityScheme sessionCookieScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID")
                .description(
                        "Cookie de sesión HTTP generada por Spring Security tras el login exitoso. "
                        + "El navegador la gestiona automáticamente al usar Swagger UI.");
    }

    /**
     * Esquema para el token CSRF enviado en el header {@code X-CSRF-TOKEN}.
     * Requerido por Spring Security en operaciones POST/PUT/DELETE.
     *
     * @return {@link SecurityScheme} de tipo API Key (header)
     */
    private SecurityScheme csrfScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-CSRF-TOKEN")
                .description(
                        "Token CSRF requerido por Spring Security para métodos HTTP mutantes "
                        + "(POST, PUT, DELETE). Obtenible desde el atributo oculto `_csrf` en cualquier "
                        + "formulario Thymeleaf renderizado por la aplicación.");
    }
}
