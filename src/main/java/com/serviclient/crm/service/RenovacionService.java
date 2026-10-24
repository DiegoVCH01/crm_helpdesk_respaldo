package com.serviclient.crm.service;

import com.serviclient.crm.repository.RenovacionRepository;
import com.serviclient.crm.repository.RenovacionSeguimientoRepository;
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
public class RenovacionService implements IRenovacionService {

  private final RenovacionRepository renovacionRepository;
  private final RenovacionSeguimientoRepository seguimientoRepository;
  private final HealthScoreService healthScoreService;


  public List<Renovacion> obtenerProximasRenovaciones() {
    return renovacionRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<Renovacion> listarProximasRenovaciones(Long empresaId, String busqueda, EstadoRenovacion estado) {
    if (empresaId == null) {
      return List.of();
    }
    // Usamos el nuevo método del DAO dinámico
    return renovacionRepository.buscarConFiltros(empresaId, busqueda, estado);
  }

  @Override
  @Transactional(readOnly = true)
  public MetricasRenovaciones calcularMetricas(Long empresaId) {
    LocalDate hoy = LocalDate.now();

    List<Renovacion> todas =
        empresaId != null ? renovacionRepository.findByEmpresaId(empresaId) : List.of();

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

  @Override
  public Renovacion obtenerPorId(Long id) {
    return renovacionRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Renovación no encontrada con ID: " + id));
  }

  @Transactional
  public Renovacion actualizarEstado(Long id, EstadoRenovacion nuevoEstado, Usuario responsable) {
    Renovacion renovacion = obtenerPorId(id);
    renovacion.setEstado(nuevoEstado);
    return renovacionRepository.save(renovacion);
  }

  @Override
  @Transactional
  public RenovacionSeguimiento agregarSeguimiento(Long renovacionId, String nota, Usuario responsable) {
    Renovacion renovacion = obtenerPorId(renovacionId);
    RenovacionSeguimiento seguimiento = RenovacionSeguimiento.builder()
        .renovacion(renovacion)
        .comentario(nota)
        .usuario(responsable)
        .estado(renovacion.getEstado())
        .fecha(java.time.LocalDateTime.now())
        .build();
    return seguimientoRepository.save(seguimiento);
  }

  @Transactional
  public RenovacionSeguimiento registrarSeguimiento(
      TicketMensajeDto.RegistrarSeguimientoRenovacion dto, Usuario usuario, Long empresaId) {

    Renovacion renovacion = obtenerPorId(dto.getRenovacionId());

    if (renovacion.getEmpresa() == null || !empresaId.equals(renovacion.getEmpresa().getId())) {
      throw new IllegalArgumentException("La renovación no pertenece a la empresa actual.");
    }

    if (dto.getEstado() != null) {
        renovacion.setEstado(dto.getEstado());
        renovacionRepository.save(renovacion);
    }

    RenovacionSeguimiento seguimiento =
        RenovacionSeguimiento.builder()
            .renovacion(renovacion)
            .comentario(dto.getComentario())
            .usuario(usuario)
            .estado(dto.getEstado())
            .fecha(LocalDateTime.now())
            .build();

    return seguimientoRepository.save(seguimiento);
  }

  public List<RenovacionSeguimiento> obtenerSeguimientos(Long renovacionId) {
    return seguimientoRepository.findByRenovacionIdOrderByFechaDesc(renovacionId);
  }
}
