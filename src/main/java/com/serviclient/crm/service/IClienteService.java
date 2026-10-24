package com.serviclient.crm.service;

import com.serviclient.crm.dto.ClienteDto;
import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.ClienteServicio;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.EncuestaSatisfaccion;
import com.serviclient.crm.entity.Renovacion;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.service.CuentaPortalService.CredencialesTemporales;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IClienteService {

    @Getter
    @Builder
    public static class MetricasClientes {
        private long totalClientes;
        private long saludables;
        private long enObservacion;
        private long enRiesgo;
    }

    @Getter
    @Builder
    public static class KpisCliente360 {
        private double csat;
        private long ticketsAbiertos;
        private long ticketsCriticos;
        private long diasParaRenovacion;
    }

    public record ResultadoCreacionCliente(
        Cliente cliente, CredencialesTemporales credencialesPortal) {}

    public record ResultadoContacto(
        Contacto contacto, CredencialesTemporales credencialesPortal) {}

    MetricasClientes obtenerMetricas(Long empresaId);

    Page<Cliente> listarClientes(Long empresaId, String busqueda, EstadoCliente estado, Pageable pageable);

    byte[] exportarClientesExcel(Long empresaId, String busqueda, EstadoCliente estado);

    List<Cliente> listarTodosPorEmpresa(Long empresaId);

    Cliente obtenerPorId(Long id);

    Cliente obtenerPorIdYEmpresa(Long id, Long empresaId);

    KpisCliente360 obtenerKpisCliente360(Cliente cliente);

    ResultadoCreacionCliente crearCliente(Long empresaId, ClienteDto dto);

    Cliente actualizarCliente(Long id, Long empresaId, ClienteDto dto);

    void eliminarCliente(Long id, Long empresaId);

    ClienteDto toDto(Cliente cliente);

    List<ClienteServicio> obtenerServiciosPorCliente(Long clienteId);

    ResultadoContacto agregarContacto(Long clienteId, Contacto contacto);

    Contacto actualizarContacto(Long clienteId, Long contactoId, Contacto datosContacto);

    List<Contacto> obtenerContactosActivos(Long clienteId);

    List<Contacto> obtenerContactosInactivos(Long clienteId);

    CredencialesTemporales establecerContactoPrincipal(Long clienteId, Long contactoId);

    void desactivarContacto(Long clienteId, Long contactoId);

    CredencialesTemporales reactivarContacto(Long clienteId, Long contactoId);

    CredencialesTemporales restablecerCredencialesPortal(Long clienteId, Long contactoId);

    List<EncuestaSatisfaccion> obtenerEncuestasPorCliente(Long clienteId);

    List<Renovacion> obtenerRenovacionesPorCliente(Long clienteId);
}
