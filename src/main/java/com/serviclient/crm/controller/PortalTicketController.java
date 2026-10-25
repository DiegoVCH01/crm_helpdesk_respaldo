
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/portal/tickets")
@RequiredArgsConstructor
@Tag(name = "Portal de Clientes",
     description = "Gestión de incidencias del portal")
public class PortalTicketController {

    private final ITicketService ticketService;
    private final CuentaPortalRepository cuentaPortalRepository;

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
        Long empresaId =
                cuenta.getContacto().getCliente().getEmpresa().getId();

        Ticket ticket = ticketService.obtenerPorIdYEmpresa(id, empresaId);

        if (!clienteId.equals(ticket.getCliente().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ticket no encontrado.");
        }

        cargarDatosUsuarioAutenticado(model, cuenta);
        model.addAttribute("ticket", ticket);

        return "portal/detalle-ticket";
    }

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

    private void cargarDatosUsuarioAutenticado(
            Model model, CuentaPortal cuenta) {

        Contacto contacto = cuenta.getContacto();

        String nombre = contacto.getNombre() != null
                ? contacto.getNombre() : "";

        String apellido = contacto.getApellido() != null
                ? contacto.getApellido() : "";

        String nombreCompleto = (nombre + " " + apellido).trim();

        String inicialNombre = !nombre.isEmpty()
                ? nombre.substring(0, 1).toUpperCase() : "";

        String inicialApellido = !apellido.isEmpty()
                ? apellido.substring(0, 1).toUpperCase() : "";

        String iniciales = inicialNombre + inicialApellido;

        model.addAttribute("usuarioNombre",
                nombreCompleto.isEmpty() ? cuenta.getEmail() : nombreCompleto);

        model.addAttribute("usuarioIniciales",
                iniciales.isEmpty() ? "U" : iniciales);

        model.addAttribute("empresaNombre",
                contacto.getCliente().getRazonSocial());
    }
}
