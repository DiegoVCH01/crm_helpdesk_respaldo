package com.serviclient.crm.dto;

import com.serviclient.crm.entity.enums.EstadoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDto {

  private Long id;

  @NotBlank(message = "El nombre comercial es obligatorio")
  private String nombreComercial;

  private String razonSocial;
  private String ruc;

  private String industria;
  private String tamanoEmpresa;
  private String pais;
  private String ciudad;
  private String direccion;
  private String sitioWeb;

  private Long responsableId;

  @NotBlank(message = "El servicio contratado es obligatorio")
  private String servicioContratado;

  @Builder.Default private EstadoCliente estado = EstadoCliente.SALUDABLE;

  @Builder.Default private Integer healthScore = 80;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  @NotNull(message = "La fecha de inicio es obligatoria")
  private LocalDate fechaInicio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  @NotNull(message = "La fecha de renovación es obligatoria")
  private LocalDate fechaRenovacion;

  private String logoUrl;

  // Contacto inicial opcional
  private String contactoNombre;
  private String contactoApellido;
  private String contactoCargo;
  private String contactoEmail;
  private String contactoTelefono;
}
