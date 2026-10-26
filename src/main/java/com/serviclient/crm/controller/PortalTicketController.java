
package com.serviclient.crm.controller;

import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.CuentaPortalRepository;
import com.serviclient.crm.service.ITicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils; // <-- Utilidad de Apache Commons
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controlador MVC del Portal de Clientes para la gestión de tickets.
 *
 * <p>Permite a los clientes autenticados mediante {@link CuentaPortal} crear
 * nuevas incidencias y consultar el detalle de sus tickets existentes.
 *
 * <p><b>Rutas expuestas:</b>
 *
 * <pre>
 * GET /portal/tickets/nuevo  → formulario de nueva incidencia
 * GET /portal/tickets/{id}   → detalle del ticket del cliente
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.ITicketService
 */
@Controller
@RequestMapping("/portal/tickets")
@RequiredArgsConstructor
@Tag(name = "Portal de Clientes",
     description = "Gestión de incidencias del portal")
public class PortalTicketController {

    private final ITicketService ticketService;
    private final CuentaPortalRepository cuentaPortalRepository;

    /**
     * Muestra el formulario para que el cliente registre una nueva incidencia.
     *
     * @param model modelo Thymeleaf con datos del usuario y opciones del formulario
     * @param principal usuario autenticado en el portal
     * @return vista {@code portal/nueva-incidencia}
     */
    @Operation(summary = "Formulario de nueva incidencia")
    @ApiResponse(responseCode = "200", description = "Formulario renderizado")
    @GetMapping("/nuevo")
    public String nuevaIncidencia(Model model, Principal principal) {

        CuentaPortal cuenta = obtenerCuenta(principal);
        cargarDatosUsuarioAutenticado(model, cuenta);

        model.addAttribute("ticketForm", new PortalTicketDto());
        model.addAttribute("categorias", CategoriaTicket.values());
        model.addAttribute("prioridades", PrioridadTicket.values());

        return "portal/nueva-incidencia";
    }

    /**
     * Muestra el detalle de un ticket específico del cliente autenticado.
     * Valida que el ticket pertenezca al cliente.
     *
     * @param id identificador del ticket
     * @param model modelo Thymeleaf
     * @param principal usuario autenticado en el portal
     * @return vista {@code portal/detalle-ticket}
     * @throws ResponseStatusException si el ticket no pertenece al cliente
     */
    @Operation(summary = "Detalle del ticket del cliente")
    @ApiResponse(responseCode = "200", description = "Ticket encontrado")
    @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    @GetMapping("/{id}")
    public String detalleTicket(
            @Parameter(description = "ID del ticket")
            @PathVariable Long id,
            Model model,
            Principal principal) {

        CuentaPortal cuenta = obtenerCuenta(principal);

        Long clienteId = cuenta.getContacto().getCliente().getId();
        Long empresaId = cuenta.getContacto().getCliente().getEmpresa().getId();

        Ticket ticket = ticketService.obtenerPorIdYEmpresa(id, empresaId);

        if (!clienteId.equals(ticket.getCliente().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ticket no encontrado.");
        }

        cargarDatosUsuarioAutenticado(model, cuenta);
        model.addAttribute("ticket", ticket);

        return "portal/detalle-ticket";
    }

    /**
     * Obtiene la cuenta de portal activa a partir del principal autenticado.
     *
     * @param principal usuario autenticado
     * @return cuenta de portal activa con contacto y cliente asociados
     * @throws ResponseStatusException si el principal es nulo, la cuenta no existe
     *         o el contacto/cliente no está identificado
     */
    private CuentaPortal obtenerCuenta(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        CuentaPortal cuenta = cuentaPortalRepository
                .findByEmail(principal.getName())
                .filter(CuentaPortal::estaActiva)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Cuenta no disponible."));

        if (cuenta.getContacto() == null
                || cuenta.getContacto().getCliente() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Cliente no identificado.");
        }

        return cuenta;
    }

    /**
     * Carga en el modelo los datos del usuario autenticado en el portal:
     * nombre completo, iniciales y nombre de la empresa.
     *
     * @param model modelo Thymeleaf donde se añaden los atributos
     * @param cuenta cuenta de portal con el contacto asociado
     */
    private void cargarDatosUsuarioAutenticado(Model model, CuentaPortal cuenta) {
        Contacto contacto = cuenta.getContacto();

        // 1. Unir nombres y apellidos limpiando espacios (maneja nulos sin romper)
        String nombreCompleto = StringUtils.trimToEmpty(
                StringUtils.joinWith(" ", contacto.getNombre(), contacto.getApellido())
        );

        // 2. Extraer de forma segura el primer caracter de cada campo
        String inicialNombre = StringUtils.left(contacto.getNombre(), 1);
        String inicialApellido = StringUtils.left(contacto.getApellido(), 1);
        String iniciales = StringUtils.upperCase(inicialNombre + inicialApellido);

        // 3. Fallbacks con defaultIfBlank (asigna valor alternativo si es null o cadena en blanco)
        model.addAttribute("usuarioNombre",
                StringUtils.defaultIfBlank(nombreCompleto, cuenta.getEmail()));

        model.addAttribute("usuarioIniciales",
                StringUtils.defaultIfBlank(iniciales, "U"));

        model.addAttribute("empresaNombre",
                StringUtils.defaultIfBlank(contacto.getCliente().getRazonSocial(), "Mi Empresa"));
    
        boolean debeCambiar = Boolean.TRUE.equals(cuenta.getRequiereCambioPassword()); // o cuenta.isRequiereCambioPassword()
        model.addAttribute("debeCambiarPassword", debeCambiar);  
        }
}