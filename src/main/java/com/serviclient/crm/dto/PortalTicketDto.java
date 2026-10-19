package com.serviclient.crm.dto;

import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalTicketDto {

  @NotBlank(message = "El asunto es obligatorio")
  @Size(max = 200, message = "El asunto no puede superar los 200 caracteres")
  private String asunto;

  @NotNull(message = "La categoría es obligatoria")
  @Builder.Default
  private CategoriaTicket categoria = CategoriaTicket.SOPORTE_TECNICO;

  @NotNull(message = "El nivel de urgencia es obligatorio")
  @Builder.Default
  private PrioridadTicket prioridad = PrioridadTicket.MEDIA;

  @NotBlank(message = "La descripción es obligatoria")
  private String descripcion;
}
