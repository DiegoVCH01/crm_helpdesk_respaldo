package com.serviclient.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa a una persona de contacto dentro de una empresa cliente.
 * <p>
 * Puede actuar como contacto principal o secundario. Si es contacto principal o se le habilita acceso,
 * se vincula a una {@link CuentaPortal} para interactuar en el portal de autoservicio de soporte.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see CuentaPortal
 */
@Entity
@Table(name = "contactos", uniqueConstraints = {
    @UniqueConstraint(name = "uq_contacto_email_cliente", columnNames = {"cliente_id", "email"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(length = 120)
    private String cargo;

    @Column(length = 180)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Builder.Default
    @Column(name = "es_contacto_principal", nullable = false)
    private Boolean esContactoPrincipal = false;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Boolean getEsPrincipal() {
        return esContactoPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esContactoPrincipal = esPrincipal;
    }

    public String getNombreCompleto() {
        if (apellido != null && !apellido.isBlank()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }

    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) return "C";
        String n = String.valueOf(nombre.charAt(0)).toUpperCase();
        String a = (apellido != null && !apellido.isBlank()) ? String.valueOf(apellido.charAt(0)).toUpperCase() : "";
        if (!a.isBlank()) return n + a;
        String[] parts = nombre.trim().split("\\s+");
        if (parts.length > 1) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return n;
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
