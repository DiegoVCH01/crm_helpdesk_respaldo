package com.serviclient.crm.entity;

import com.serviclient.crm.entity.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA que modela a un usuario interno de la empresa tenant (Administrador, Agente de Soporte o Supervisor).
 * <p>
 * Gestiona los datos de acceso, credenciales cifradas con BCrypt, bandera de cambio obligatorio
 * de contraseña temporal y relación con su rol funcional y empresa.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Entity
@Table(name = "usuarios", uniqueConstraints = {
    @UniqueConstraint(name = "uq_usuario_email_empresa", columnNames = {"empresa_id", "email"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private RolEntity rolEntity;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Column(length = 30)
    private String telefono;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Builder.Default
    @Column(name = "debe_cambiar_password", nullable = false)
    private Boolean debeCambiarPassword = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Mapea la entidad de rol {@link RolEntity} al enum de seguridad tipado {@link Rol}.
     *
     * @return rol tipado (ADMIN, AGENTE o SUPERVISOR)
     */
    public Rol getRol() {
        if (rolEntity != null && rolEntity.getNombre() != null) {
            String roleName = rolEntity.getNombre().toUpperCase().trim();
            if (roleName.contains("ADMIN")) return Rol.ADMIN;
            if (roleName.contains("AGENTE") || roleName.contains("SOPORTE")) return Rol.AGENTE;
            if (roleName.contains("SUPERVISOR") || roleName.contains("SUCCESS")) return Rol.SUPERVISOR;
        }
        return Rol.AGENTE;
    }

    /**
     * Indica si el usuario posee estado operativo ACTIVO.
     *
     * @return true si el estado es ACTIVO, false en caso contrario
     */
    public Boolean getActivo() {
        return "ACTIVO".equalsIgnoreCase(this.estado);
    }

    public void setActivo(Boolean activo) {
        this.estado = (Boolean.TRUE.equals(activo)) ? "ACTIVO" : "INACTIVO";
    }

    /**
     * Retorna el nombre y apellido concatenados del usuario.
     *
     * @return nombre completo
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Genera las iniciales a partir del nombre y apellido para avatares visuales.
     *
     * @return iniciales en mayúsculas o "U"
     */
    public String getIniciales() {
        String n = (nombre != null && !nombre.isBlank()) ? String.valueOf(nombre.charAt(0)).toUpperCase() : "";
        String a = (apellido != null && !apellido.isBlank()) ? String.valueOf(apellido.charAt(0)).toUpperCase() : "";
        return (n + a).isBlank() ? "U" : n + a;
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
