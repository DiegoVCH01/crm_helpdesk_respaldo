package com.serviclient.crm.dto;

import com.serviclient.crm.entity.enums.EstadoRenovacion;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Contenedor de objetos de transferencia de datos (DTOs) estáticos para acciones operativas sobre tickets y renovaciones.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
public class TicketMensajeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnviarMensaje {
        @NotBlank(message = "El contenido no puede estar vacío")
        private String contenido;

        private Boolean esNotaInterna = false;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CambiarEstado {
        private EstadoTicket estado;
        private PrioridadTicket prioridad;
        private Long agenteId;
        private String comentario;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CerrarTicket {
        private Boolean enviarCsat = true;
        private String comentarioCierre;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistrarSeguimientoRenovacion {
        @NotNull(message = "El ID de la renovación es obligatorio")
        private Long renovacionId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @NotNull(message = "La fecha es obligatoria")
        private LocalDate fecha;

        private Long responsableId;

        @NotNull(message = "El estado es obligatorio")
        private EstadoRenovacion estado;

        @NotBlank(message = "El comentario es obligatorio")
        private String comentario;
    }
}
