package com.serviclient.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Entidad JPA que representa las credenciales de acceso al Portal de Autoservicio para un contacto de cliente.
 * <p>
 * Mantiene la asociación uno a uno con {@link Contacto}, almacena el hash BCrypt de la contraseña,
 * el estado de actividad y la bandera de obligatoriedad de cambio de contraseña en el primer inicio de sesión.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see Contacto
 */
@Entity
@Table(
    name = "cuentas_portal",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_cuenta_portal_contacto", columnNames = "contacto_id"),
      @UniqueConstraint(name = "uq_cuenta_portal_usuario", columnNames = "usuario_portal")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaPortal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contacto_id", nullable = false, unique = true)
  private Contacto contacto;

  /*
   * En la BD la columna se llama usuario_portal,
   * pero funcionalmente se utilizará como correo electrónico.
   */
  @Column(name = "usuario_portal", nullable = false, unique = true, length = 180)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Builder.Default
  @Column(name = "requiere_cambio_password", nullable = false)
  private Boolean requiereCambioPassword = true;

  @Builder.Default
  @Column(nullable = false, length = 20)
  private String estado = "ACTIVO";

  @Column(name = "ultimo_acceso")
  private LocalDateTime ultimoAcceso;

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public boolean estaActiva() {
    return "ACTIVO".equalsIgnoreCase(estado);
  }

  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }

    if (updatedAt == null) {
      updatedAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
