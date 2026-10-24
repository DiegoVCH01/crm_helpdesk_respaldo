package com.serviclient.crm.controller;

import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.service.ITicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/portal/tickets")
@RequiredArgsConstructor
@Tag(
    name = "Portal de Clientes",
    description = "Acceso externo para que los clientes finales creen incidencias y consulten el estado de sus tickets")
public class PortalTicketController {

  private final ITicketService ticketService;

  @Operation(summary = "Formulario de nueva incidencia", description = "Muestra el formulario público para que un cliente externo reporte una incidencia")
  @ApiResponse(responseCode = "200", description = "Vista del formulario renderizada correctamente")
  @GetMapping("/nuevo")
  public String nuevaIncidencia(Model model) {

    model.addAttribute("ticketForm", new PortalTicketDto());
    model.addAttribute("categorias", CategoriaTicket.values());
    model.addAttribute("prioridades", PrioridadTicket.values());

    return "portal/nueva-incidencia";
  }

  @Operation(summary = "Ver detalle de ticket (portal)", description = "Muestra el estado y conversación de un ticket accesible desde el portal de clientes")
  @ApiResponse(responseCode = "200", description = "Vista de detalle del ticket")
  @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
  @GetMapping("/{id}")
  public String detalleTicket(@Parameter(description = "ID del ticket") @PathVariable Long id, Model model) {

    Ticket ticket = ticketService.obtenerPorId(id);

    model.addAttribute("ticket", ticket);

    return "portal/detalle-ticket";
  }
}