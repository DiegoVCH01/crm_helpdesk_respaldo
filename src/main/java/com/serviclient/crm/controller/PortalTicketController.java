package com.serviclient.crm.controller;

import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.CuentaPortalRepository;
import com.serviclient.crm.service.ITicketService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/portal/tickets")
@RequiredArgsConstructor
public class PortalTicketController {

  private final ITicketService ticketService;
  private final CuentaPortalRepository cuentaPortalRepository;

  @GetMapping("/nuevo")
  public String nuevaIncidencia(Model model, Principal principal) {

    cargarDatosUsuarioAutenticado(model, principal);

    model.addAttribute("ticketForm", new PortalTicketDto());
    model.addAttribute("categorias", CategoriaTicket.values());
    model.addAttribute("prioridades", PrioridadTicket.values());

    return "portal/nueva-incidencia";
  }

  @GetMapping("/{id}")
  public String detalleTicket(@PathVariable Long id, Model model, Principal principal) {

    cargarDatosUsuarioAutenticado(model, principal);

    Ticket ticket = ticketService.obtenerPorId(id);
    model.addAttribute("ticket", ticket);

    return "portal/detalle-ticket";
  }

  private void cargarDatosUsuarioAutenticado(Model model, Principal principal) {
    if (principal != null) {
      cuentaPortalRepository.findByEmail(principal.getName()).ifPresent(cuenta -> {
        Contacto contacto = cuenta.getContacto();
        if (contacto != null) {
          String nombre = contacto.getNombre() != null ? contacto.getNombre() : "";
          String apellido = contacto.getApellido() != null ? contacto.getApellido() : "";
          String nombreCompleto = (nombre + " " + apellido).trim();

          String inicialNombre = !nombre.isEmpty() ? nombre.substring(0, 1).toUpperCase() : "";
          String inicialApellido = !apellido.isEmpty() ? apellido.substring(0, 1).toUpperCase() : "";
          String iniciales = inicialNombre + inicialApellido;

          model.addAttribute("usuarioNombre", nombreCompleto.isEmpty() ? cuenta.getEmail() : nombreCompleto);
          model.addAttribute("usuarioIniciales", iniciales.isEmpty() ? "U" : iniciales);

          if (contacto.getCliente() != null) {
            model.addAttribute("empresaNombre", contacto.getCliente().getRazonSocial());
          }
        }
      });
    }
  }
}