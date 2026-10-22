package com.serviclient.crm.service;

import com.serviclient.crm.dao.RenovacionDAO;
import com.serviclient.crm.dao.RenovacionSeguimientoDAO;
import com.serviclient.crm.dto.TicketMensajeDto;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.RenovacionSeguimiento;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RenovacionService {

  private final RenovacionDAO renovacionDAO;
  private final RenovacionSeguimientoDAO seguimientoDAO;
  private final HealthScoreService healthScoreService;

  @Getter
  @Builder
  public static class MetricasRenovaciones {
    private long menosDe30Dias;
    private long de31a90Dias;
    private long masDe90Dias;
    private long completadas;
    private long noRealizadas;
    private long clientesEnRiesgoProximos;
  }

  public List<Renovacion> obtenerProximasRenovaciones() {
    return renovacionDAO.findAll();
  }

  @Transactional(readOnly = true)
  public List<Renovacion> listarProximasRenovaciones(Long empresaId) {
    if (empresaId == null) {
      return List.of();
    }

    return renovacionDAO.findByEmpresaIdOrderByFechaVencimientoAsc(empresaId);
  }

  @Transactional(readOnly = true)
  public MetricasRenovaciones obtenerMetricas(Long empresaId) {
    return calcularMetricas(empresaId);
  }

  private MetricasRenovaciones calcularMetricas(Long empresaId) {
    LocalDate hoy = LocalDate.now();

    List<Renovacion> todas =
        empresaId != null ? renovacionDAO.findByEmpresaId(empresaId) : List.of();

    long menos30 =
        todas.stream()
            .filter(
                r ->
                    r.getFechaVencimiento() != null
                        && !r.getFechaVencimiento().isBefore(hoy)
                        && r.getFechaVencimiento().isBefore(hoy.plusDays(30)))
            .count();

    long entre31y90 =
        todas.stream()
            .filter(
                r ->
                    r.getFechaVencimiento() != null
                        && !r.getFechaVencimiento().isBefore(hoy.plusDays(31))
                        && r.getFechaVencimiento().isBefore(hoy.plusDays(91)))
            .count();

    long mas90 =
        todas.stream()
            .filter(
                r ->
                    r.getFechaVencimiento() != null
                        && r.getFechaVencimiento().isAfter(hoy.plusDays(90)))
            .count();

    long completadas =
        todas.stream().filter(r -> r.getEstado() == EstadoRenovacion.RENOVADO).count();

    long noRealizadas =
        todas.stream().filter(r -> r.getEstado() == EstadoRenovacion.NO_RENOVADO).count();

    return MetricasRenovaciones.builder()
        .menosDe30Dias(menos30)
        .de31a90Dias(entre31y90)
        .masDe90Dias(mas90)
        .completadas(completadas)
        .noRealizadas(noRealizadas)
        .clientesEnRiesgoProximos(menos30)
        .build();
  }

  public Renovacion obtenerPorId(Long id) {
    return renovacionDAO
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Renovación no encontrada con ID: " + id));
  }

  @Transactional
  public Renovacion guardar(Renovacion renovacion) {
    return renovacionDAO.save(renovacion);
  }

  @Transactional
  public RenovacionSeguimiento registrarSeguimiento(
      TicketMensajeDto.RegistrarSeguimientoRenovacion dto, Usuario usuario) {
    Renovacion renovacion = obtenerPorId(dto.getRenovacionId());

    RenovacionSeguimiento seguimiento =
        RenovacionSeguimiento.builder()
            .renovacion(renovacion)
            .comentario(dto.getComentario())
            .usuario(usuario)
            .fecha(LocalDateTime.now())
            .build();

    return seguimientoDAO.save(seguimiento);
  }

  public List<RenovacionSeguimiento> obtenerSeguimientos(Long renovacionId) {
    return seguimientoDAO.findByRenovacionIdOrderByFechaDesc(renovacionId);
  }
}
