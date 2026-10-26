package com.serviclient.crm.dto;

import com.serviclient.crm.entity.enums.CanalOrigen;
import com.serviclient.crm.entity.enums.CategoriaTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Objeto de transferencia de datos (DTO) para la creación de tickets desde la consola interna de agentes.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {

    private Long id;

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    private Long contactoId;

    private Long agenteId;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    @NotBlank(message = "La descripción inicial es obligatoria")
    private String descripcion;

    @Builder.Default
    private CategoriaTicket categoria = CategoriaTicket.SOPORTE_TECNICO;

    @Builder.Default
    private PrioridadTicket prioridad = PrioridadTicket.MEDIA;

    @Builder.Default
    private CanalOrigen canalOrigen = CanalOrigen.PORTAL_WEB;
}
