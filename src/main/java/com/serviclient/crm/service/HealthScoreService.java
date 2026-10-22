package com.serviclient.crm.service;

import com.serviclient.crm.dao.ClienteDAO;
import com.serviclient.crm.dao.EncuestaSatisfaccionDAO;
import com.serviclient.crm.dao.HealthScoreHistorialDAO;
import com.serviclient.crm.dao.TicketDAO;
import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.HealthScoreHistorial;
import com.serviclient.crm.entity.enums.EstadoSaludCliente;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthScoreService {

  private final ClienteDAO clienteDAO;
  private final TicketDAO ticketDAO;
  private final EncuestaSatisfaccionDAO encuestaSatisfaccionDAO;
  private final HealthScoreHistorialDAO healthScoreHistorialDAO;

  @Getter
  @Builder
  public static class FactorSalud {
    private String titulo;
    private String concepto;
    private String impacto; // "NEGATIVO", "POSITIVO", "NEUTRO"
    private String penalizacionTexto;
    private String recomendacion;
  }

  @Transactional
  public int recalcularYActualizarHealthScore(Cliente cliente) {
    int puntaje = 100;

    // 1. Descuento por tickets abiertos
    long ticketsAbiertos =
        ticketDAO.countByClienteIdAndEstadoNot(cliente.getId(), EstadoTicket.CERRADO);
    if (ticketsAbiertos > 0) {
      puntaje -= (int) (ticketsAbiertos * 5);
    }

    // 2. Descuento por tickets críticos
    long ticketsCriticos =
        ticketDAO.countByClienteIdAndPrioridadAndEstadoNot(
            cliente.getId(), PrioridadTicket.CRITICA, EstadoTicket.CERRADO);
    if (ticketsCriticos > 0) {
      puntaje -= (int) (ticketsCriticos * 15);
    }

    // 3. Ajuste por CSAT
    Double csatPromedio = encuestaSatisfaccionDAO.obtenerPromedioCsatPorCliente(cliente.getId());
    if (csatPromedio != null) {
      if (csatPromedio < 3.0) {
        puntaje -= 20;
      } else if (csatPromedio >= 4.5) {
        puntaje += 5;
      }
    }

    // Normalizar entre 0 y 100
    puntaje = Math.max(0, Math.min(100, puntaje));

    // Guardar historial
    EstadoSaludCliente estadoSalud =
        puntaje >= 80
            ? EstadoSaludCliente.SALUDABLE
            : (puntaje >= 50 ? EstadoSaludCliente.EN_OBSERVACION : EstadoSaludCliente.EN_RIESGO);

    HealthScoreHistorial historial =
        HealthScoreHistorial.builder()
            .cliente(cliente)
            .puntaje(new BigDecimal(puntaje))
            .estado(estadoSalud)
            .fechaCalculo(LocalDateTime.now())
            .build();

    healthScoreHistorialDAO.save(historial);
    cliente.setHealthScore(puntaje);
    clienteDAO.save(cliente);

    return puntaje;
  }

  @Transactional(readOnly = true)
  public List<FactorSalud> obtenerFactoresRiesgo(Cliente cliente) {
    List<FactorSalud> factores = new ArrayList<>();

    long ticketsCriticos =
        ticketDAO.countByClienteIdAndPrioridadAndEstadoNot(
            cliente.getId(), PrioridadTicket.CRITICA, EstadoTicket.CERRADO);

    if (ticketsCriticos > 0) {
      factores.add(
          FactorSalud.builder()
              .titulo("Tickets Críticos Abiertos")
              .concepto("Tickets Críticos Abiertos")
              .impacto("NEGATIVO")
              .penalizacionTexto("-" + (ticketsCriticos * 15) + " pts")
              .recomendacion(
                  "Asignar recursos de nivel 2 para resolver incidencias críticas de inmediato.")
              .build());
    }

    return factores;
  }
}
