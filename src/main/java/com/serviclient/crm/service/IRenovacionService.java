package com.serviclient.crm.service;

import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.RenovacionSeguimiento;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.EstadoRenovacion;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public interface IRenovacionService {

    @Getter
    @Builder
    public static class MetricasRenovaciones {
        private long menosDe30Dias;
        private long de31a90Dias;
        private long masDe90Dias;
        private long completadas;
        private long noRealizadas;
        private long clientesEnRiesgoProximos;
    }

    List<Renovacion> obtenerProximasRenovaciones();

    List<Renovacion> listarProximasRenovaciones(Long empresaId, String busqueda, EstadoRenovacion estado);

    MetricasRenovaciones calcularMetricas(Long empresaId);

    default MetricasRenovaciones obtenerMetricas(Long empresaId) {
        return calcularMetricas(empresaId);
    }

    Renovacion obtenerPorId(Long id);

    Renovacion actualizarEstado(Long id, EstadoRenovacion nuevoEstado, Usuario responsable);

    RenovacionSeguimiento agregarSeguimiento(Long renovacionId, String nota, Usuario responsable);

    RenovacionSeguimiento registrarSeguimiento(
        com.serviclient.crm.dto.TicketMensajeDto.RegistrarSeguimientoRenovacion dto, Usuario usuario, Long empresaId);

    List<RenovacionSeguimiento> obtenerSeguimientos(Long renovacionId);
}
