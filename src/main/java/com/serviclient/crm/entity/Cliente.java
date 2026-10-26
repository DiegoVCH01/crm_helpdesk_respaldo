package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.EstadoCliente;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Entidad JPA que representa una empresa cliente (cuenta B2B) dentro del modelo multi-tenant.
 * <p>
 * Almacena datos comerciales, fiscales (RUC), ejecutivos asignados, métricas algorítmicas de retención
 * (Health Score, promedio CSAT) y relaciones con contactos, servicios suscritos y renovaciones.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Entity
@Table(
    name = "clientes",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_cliente_ruc_empresa",
          columnNames = {"empresa_id", "ruc"})
    },
    indexes = {@Index(name = "idx_clientes_empresa_estado", columnList = "empresa_id, estado")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "empresa_id", nullable = false)
  private Empresa empresa;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "responsable_id")
  private Usuario responsable;

  @Column(name = "nombre_comercial", nullable = false, length = 150)
  private String nombreComercial;

  @Column(name = "razon_social", length = 200)
  private String razonSocial;

  @Column(length = 20)
  private String ruc;

  @Column(length = 100)
  private String industria;

  @Column(name = "tamano_empresa", length = 50)
  private String tamanoEmpresa;

  @Column(length = 100)
  private String pais;

  @Column(length = 100)
  private String ciudad;

  @Column(length = 255)
  private String direccion;

  @Column(name = "sitio_web", length = 255)
  private String sitioWeb;

  @Builder.Default
  @Column(nullable = false, length = 20)
  private String estado = "ACTIVO";

  @Column(name = "logo_url", length = 500)
  private String logoUrl;

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(
      mappedBy = "cliente",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @Builder.Default
  private List<Contacto> contactos = new ArrayList<>();

  @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ClienteServicio> servicios = new ArrayList<>();

  @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Ticket> tickets = new ArrayList<>();

  @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Renovacion> renovaciones = new ArrayList<>();

  @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Interaccion> interacciones = new ArrayList<>();

  @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("fechaCalculo DESC")
  @Builder.Default
  private List<HealthScoreHistorial> healthScoreHistorial = new ArrayList<>();

  // Transient attributes / helper methods for view compatibility
  @Builder.Default @Transient private Integer healthScoreTransient = 80;

  @Builder.Default @Transient private Double csatPromedioTransient = 4.5;

  @Transient private String servicioContratadoTransient;

  @Transient private LocalDate fechaInicioTransient;

  @Transient private LocalDate fechaRenovacionTransient;

  @Transient private EstadoCliente estadoClienteTransient;

  public Integer getHealthScore() {
    if (healthScoreHistorial != null && !healthScoreHistorial.isEmpty()) {
      return healthScoreHistorial.get(0).getPuntaje().intValue();
    }
    return healthScoreTransient != null ? healthScoreTransient : 80;
  }

  public void setHealthScore(Integer healthScore) {
    this.healthScoreTransient = healthScore;
  }

  public EstadoCliente getEstadoSalud() {
    if (estadoClienteTransient != null) return estadoClienteTransient;
    int hs = getHealthScore();
    if (hs >= 80) return EstadoCliente.SALUDABLE;
    if (hs >= 50) return EstadoCliente.OBSERVACION;
    return EstadoCliente.EN_RIESGO;
  }

  // Overload for compatibility with views accessing cliente.estado
  public EstadoCliente getEstadoEnum() {
    return getEstadoSalud();
  }

  public void setEstado(EstadoCliente estadoCliente) {
    this.estadoClienteTransient = estadoCliente;
    if (estadoCliente != null) {
      this.estado = "ACTIVO";
    }
  }

  public void setEstado(String estadoStr) {
    this.estado = estadoStr;
  }

  public Double getCsatPromedio() {
    return csatPromedioTransient != null ? csatPromedioTransient : 4.5;
  }

  public void setCsatPromedio(Double csat) {
    this.csatPromedioTransient = csat;
  }

  public String getServicioContratado() {
    if (servicioContratadoTransient != null) return servicioContratadoTransient;
    if (servicios != null && !servicios.isEmpty()) {
      return servicios.get(0).getServicio() != null
          ? servicios.get(0).getServicio().getNombre()
          : "SaaS Solution";
    }
    return "SaaS Enterprise";
  }

  public void setServicioContratado(String s) {
    this.servicioContratadoTransient = s;
  }

  public LocalDate getFechaInicio() {
    if (fechaInicioTransient != null) return fechaInicioTransient;
    if (servicios != null && !servicios.isEmpty()) {
      return servicios.get(0).getFechaInicio();
    }
    return LocalDate.now().minusMonths(6);
  }

  public void setFechaInicio(LocalDate fi) {
    this.fechaInicioTransient = fi;
  }

  public LocalDate getFechaRenovacion() {
    if (fechaRenovacionTransient != null) return fechaRenovacionTransient;
    if (renovaciones != null && !renovaciones.isEmpty()) {
      return renovaciones.get(0).getFechaVencimiento();
    }
    if (servicios != null && !servicios.isEmpty() && servicios.get(0).getFechaFin() != null) {
      return servicios.get(0).getFechaFin();
    }
    return LocalDate.now().plusMonths(6);
  }

  public void setFechaRenovacion(LocalDate fr) {
    this.fechaRenovacionTransient = fr;
  }

  public long getDiasParaRenovacion() {
    LocalDate fr = getFechaRenovacion();
    if (fr == null) return 0;
    return ChronoUnit.DAYS.between(LocalDate.now(), fr);
  }

  public int getCsatPorcentaje() {
    Double csat = getCsatPromedio();
    if (csat == null || csat <= 0) return 0;
    return (int) Math.round((csat / 5.0) * 100);
  }

  public String getHealthScoreColorClass() {
    int hs = getHealthScore();
    if (hs >= 70) return "bg-emerald-500";
    if (hs >= 50) return "bg-amber-500";
    return "bg-rose-500";
  }

  public String getHealthScoreTextColorClass() {
    int hs = getHealthScore();
    if (hs >= 70) return "text-emerald-600";
    if (hs >= 50) return "text-amber-600";
    return "text-rose-600";
  }

  public Contacto getContactoPrincipal() {
    if (contactos == null || contactos.isEmpty()) {
      return null;
    }

    return contactos.stream()
        .filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado()))
        .filter(c -> Boolean.TRUE.equals(c.getEsPrincipal()))
        .findFirst()
        .orElse(null);
  }

  public LocalDateTime getFechaCreacion() {
    return createdAt;
  }

  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.createdAt = fechaCreacion;
  }

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
    if (this.updatedAt == null) {
      this.updatedAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
