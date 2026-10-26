package com.serviclient.crm.dto;

import com.serviclient.crm.entity.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Objeto de transferencia de datos (DTO) para la creación y edición de agentes o supervisores.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Getter
@Setter
public class AgenteDto {

  @NotBlank(message = "El nombre es obligatorio")
  private String nombre;

  @NotBlank(message = "El apellido es obligatorio")
  private String apellido;

  @NotBlank(message = "El correo es obligatorio")
  @Email(message = "El correo no tiene un formato válido")
  private String email;

  private String telefono;

  @NotNull(message = "Debe seleccionar un rol")
  private Rol rol;
}
