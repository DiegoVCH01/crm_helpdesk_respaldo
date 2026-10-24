package com.serviclient.crm.controller;

import com.serviclient.crm.dto.PortalTicketDto;
import com.serviclient.crm.entity.Ticket;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.service.ITicketService;
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

  @GetMapping("/nuevo")
  public String nuevaIncidencia(Model model) {

    model.addAttribute("ticketForm", new PortalTicketDto());
    model.addAttribute("categorias", CategoriaTicket.values());
    model.addAttribute("prioridades", PrioridadTicket.values());

    return "portal/nueva-incidencia";
  }

  @GetMapping("/{id}")
  public String detalleTicket(@PathVariable Long id, Model model) {

    Ticket ticket = ticketService.obtenerPorId(id);

    model.addAttribute("ticket", ticket);

    return "portal/detalle-ticket";
  }
}