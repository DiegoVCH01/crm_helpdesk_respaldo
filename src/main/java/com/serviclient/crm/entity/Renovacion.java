package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.EstadoRenovacion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que gestiona el seguimiento comercial y retención de suscripciones y contratos B2B.
 * <p>
 * Registra fechas de vencimiento, estado en el pipeline de renovación (PENDIENTE, EN_NEGOCIACION, RENOVADO, NO_RENOVADO),
 * alertas de anticipación y el histórico cronológico de interacciones de Customer Success ({@link RenovacionSeguimiento}).
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see RenovacionSeguimiento
 */
@Entity
@Table(name = "renovaciones", indexes = {
    @Index(name = "idx_renovacion_empresa_estado", columnList = "empresa_id, estado"),
    @Index(name = "idx_renovacion_vencimiento", columnList = "fecha_vencimiento"),
    @Index(name = "idx_renovacion_cliente", columnList = "cliente_id"),
    @Index(name = "idx_renovacion_empresa_fecha_estado", columnList = "empresa_id, fecha_vencimiento, estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renovacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_servicio_id", nullable = false)
    private ClienteServicio clienteServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Builder.Default
    @Column(name = "dias_alerta", nullable = false)
    private Integer diasAlerta = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoRenovacion estado = EstadoRenovacion.PENDIENTE;

    @Column(name = "fecha_renovacion")
    private LocalDate fechaRenovacion;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "renovacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha DESC")
    @Builder.Default
    private List<RenovacionSeguimiento> seguimientos = new ArrayList<>();

    // Compatibility helpers
    public String getServicio() {
        if (clienteServicio != null && clienteServicio.getServicio() != null) {
            return clienteServicio.getServicio().getNombre();
        }
        return "SaaS Subscription";
    }

    public BigDecimal getMontoEstimado() {
        return new BigDecimal("12000.00");
    }

    public long getDiasRestantes() {
        if (fechaVencimiento == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
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
