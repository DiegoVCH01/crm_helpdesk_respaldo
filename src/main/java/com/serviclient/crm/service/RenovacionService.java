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

/**
 * Implementación del servicio de gestión de renovaciones de contratos y retención de clientes.
 * <p>
 * Proporciona funcionalidades para el seguimiento de vencimientos de contratos (ARR),
 * cálculo de ventanas de riesgo (&lt;30 días, 31-90 días, &gt;90 días), registro de bitácoras
 * de interacción comercial y actualización del ciclo de vida de la renovación.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see IRenovacionService
 */
@Service
@RequiredArgsConstructor
public class RenovacionService implements IRenovacionService {

  private final RenovacionRepository renovacionRepository;
  private final RenovacionSeguimientoRepository seguimientoRepository;
  private final HealthScoreService healthScoreService;

  /**
   * Obtiene la totalidad de contratos y renovaciones registrados en el sistema.
   *
   * @return lista completa de renovaciones
   */
  public List<Renovacion> obtenerProximasRenovaciones() {
    return renovacionRepository.findAll();
  }

  /**
   * Lista las renovaciones de una empresa aplicando filtros opcionales de búsqueda y estado.
   *
   * @param empresaId identificador de la empresa
   * @param busqueda término opcional de búsqueda por nombre comercial del cliente
   * @param estado estado del proceso de renovación para filtrar
   * @return lista de renovaciones coincidentes
   */
  @Transactional(readOnly = true)
  public List<Renovacion> listarProximasRenovaciones(Long empresaId, String busqueda, EstadoRenovacion estado) {
    if (empresaId == null) {
      return List.of();
    }
    // Usamos el nuevo método del DAO dinámico
    return renovacionRepository.buscarConFiltros(empresaId, busqueda, estado);
  }

  /**
   * Calcula indicadores cuantitativos de contratos distribuidos por ventanas temporales de vencimiento.
   *
   * @param empresaId identificador de la empresa
   * @return objeto {@link MetricasRenovaciones} con conteos por períodos y estados
   */
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

  /**
   * Obtiene una renovación específica por su identificador primario.
   *
   * @param id identificador de la renovación
   * @return entidad {@link Renovacion} encontrada
   * @throws RuntimeException si la renovación no existe
   */
  @Override
  public Renovacion obtenerPorId(Long id) {
    return renovacionRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Renovación no encontrada con ID: " + id));
  }

  /**
   * Actualiza el estado del ciclo de vida de un contrato o renovación.
   *
   * @param id identificador de la renovación
   * @param nuevoEstado nuevo estado a establecer
   * @param responsable usuario que efectúa el cambio
   * @return entidad {@link Renovacion} actualizada
   */
  @Transactional
  public Renovacion actualizarEstado(Long id, EstadoRenovacion nuevoEstado, Usuario responsable) {
    Renovacion renovacion = obtenerPorId(id);
    renovacion.setEstado(nuevoEstado);
    return renovacionRepository.save(renovacion);
  }

  /**
   * Añade una nota de seguimiento a la bitácora de la renovación.
   *
   * @param renovacionId identificador de la renovación
   * @param nota contenido del comentario o apunte comercial
   * @param responsable usuario autor de la nota
   * @return entidad {@link RenovacionSeguimiento} creada y persistida
   */
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

  /**
   * Registra una acción de seguimiento validando pertenencia a la empresa y actualizando opcionalmente el estado.
   *
   * @param dto datos del formulario de seguimiento
   * @param usuario usuario que registra el evento
   * @param empresaId identificador de la empresa activa para validación de pertenencia
   * @return entidad {@link RenovacionSeguimiento} persistida
   * @throws IllegalArgumentException si la renovación no pertenece a la empresa actual
   */
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

  /**
   * Obtiene el historial completo de seguimientos registrados para una renovación en orden cronológico inverso.
   *
   * @param renovacionId identificador de la renovación
   * @return lista de seguimientos asociados
   */
  public List<RenovacionSeguimiento> obtenerSeguimientos(Long renovacionId) {
    return seguimientoRepository.findByRenovacionIdOrderByFechaDesc(renovacionId);
  }
}
