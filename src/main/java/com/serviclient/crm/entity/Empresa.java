package com.serviclient.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresas", uniqueConstraints = {
    @UniqueConstraint(name = "uq_empresa_ruc", columnNames = {"ruc"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_comercial", nullable = false, length = 150)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(nullable = false, length = 20)
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

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Builder.Default
    @Column(name = "zona_horaria", length = 100)
    private String zonaHoraria = "America/Lima";

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "ACTIVA";

    // Configuración Inicial SLA (helper / backwards compatibility)
    @Builder.Default
    @Transient
    private Integer slaPrimerRespuestaHoras = 2;

    @Builder.Default
    @Transient
    private Integer slaResolucionHoras = 24;

    // Configuración Inicial CSAT (helper / backwards compatibility)
    @Builder.Default
    @Transient
    private Boolean activarCsat = true;

    @Builder.Default
    @Transient
    private String escalaCsat = "1 a 5";

    // Configuración Factores Health Score (helper / backwards compatibility)
    @Builder.Default
    @Transient
    private Boolean factorTicketsCriticos = true;

    @Builder.Default
    @Transient
    private Boolean factorCsat = true;

    @Builder.Default
    @Transient
    private Boolean factorRenovacionProxima = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Usuario> usuarios = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Cliente> clientes = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Servicio> servicios = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CategoriaTicketEntity> categorias = new ArrayList<>();

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
