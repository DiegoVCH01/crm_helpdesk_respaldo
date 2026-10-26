package com.serviclient.crm.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

/**
 * Objeto de transferencia de datos (DTO) para el asistente de aprovisionamiento de una nueva empresa tenant.
 * <p>
 * Agrupa la información corporativa (paso 1), las credenciales del usuario administrador raíz (paso 2)
 * y las políticas predeterminadas de SLA, encuestas CSAT y Health Score (paso 3).
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroEmpresaDto implements Serializable {

  // --- Paso 1: Información de la Empresa ---
  @NotBlank(message = "El nombre comercial es obligatorio")
  private String nombreComercial;

  @NotBlank(message = "La razón social es obligatoria")
  private String razonSocial;

  @NotBlank(message = "El RUC es obligatorio")
  private String ruc;

  private String industria;
  private String tamanoEmpresa;
  private String pais;
  private String ciudad;
  private String direccion;
  private String sitioWeb;
  private String logoUrl;

  // --- Paso 2: Usuario Administrador ---
  @NotBlank(message = "El nombre es obligatorio")
  private String adminNombre;

  @NotBlank(message = "El apellido es obligatorio")
  private String adminApellido;

  @NotBlank(message = "El correo electrónico es obligatorio")
  @Email(message = "Formato de correo no válido")
  private String adminEmail;

  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
  private String adminPassword;

  private String adminPasswordConfirm;
  private String adminTelefono;

  // --- Paso 3: Configuración Inicial ---
  @Builder.Default private Boolean usarConfiguracionRecomendada = true;

  @Builder.Default
  @NotNull(message = "El tiempo de primera respuesta es obligatorio")
  @Min(value = 1, message = "Debe ser al menos 1 hora")
  private Integer slaPrimerRespuestaHoras = 2;

  @Builder.Default
  @NotNull(message = "El tiempo de resolución es obligatorio")
  @Min(value = 1, message = "Debe ser al menos 1 hora")
  private Integer slaResolucionHoras = 24;

  @Builder.Default private Boolean activarCsat = true;

  @Builder.Default private String escalaCsat = "1 a 5";

  @Builder.Default private Boolean factorTicketsCriticos = true;

  @Builder.Default private Boolean factorCsat = true;

  @Builder.Default private Boolean factorRenovacionProxima = true;
}
