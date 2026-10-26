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

    /**
     * Obtiene el conteo agregado de clientes saludables, en observación y en riesgo.
     *
     * @param empresaId identificador de la empresa
     * @return objeto {@link MetricasClientes} con los indicadores
     */
    MetricasClientes obtenerMetricas(Long empresaId);

    /**
     * Lista de manera paginada los clientes aplicando filtros de búsqueda textual y estado de salud.
     *
     * @param empresaId identificador de la empresa
     * @param busqueda término de búsqueda por nombre comercial o razón social
     * @param estado filtro opcional por {@link EstadoCliente}
     * @param pageable configuración de paginación y ordenamiento
     * @return página con los clientes encontrados
     */
    Page<Cliente> listarClientes(Long empresaId, String busqueda, EstadoCliente estado, Pageable pageable);

    /**
     * Genera en memoria un archivo Excel (.xlsx) con el padrón de clientes filtrado.
     *
     * @param empresaId identificador de la empresa
     * @param busqueda término opcional de búsqueda
     * @param estado estado del cliente para filtrar
     * @return arreglo de bytes del documento binario Excel
     */
    byte[] exportarClientesExcel(Long empresaId, String busqueda, EstadoCliente estado);

    /**
     * Retorna todos los clientes registrados para la empresa sin paginación.
     *
     * @param empresaId identificador de la empresa
     * @return listado de clientes
     */
    List<Cliente> listarTodosPorEmpresa(Long empresaId);

    /**
     * Busca un cliente por su identificador único.
     *
     * @param id identificador del cliente
     * @return entidad {@link Cliente}
     * @throws org.springframework.web.server.ResponseStatusException si el cliente no existe
     */
    Cliente obtenerPorId(Long id);

    /**
     * Obtiene un cliente validando su pertenencia a la empresa activa.
     *
     * @param id identificador del cliente
     * @param empresaId identificador de la empresa
     * @return entidad {@link Cliente}
     * @throws org.springframework.web.server.ResponseStatusException si no existe o pertenece a otra empresa
     */
    Cliente obtenerPorIdYEmpresa(Long id, Long empresaId);

    /**
     * Consolida los indicadores clave (CSAT, tickets abiertos, críticos y días para renovar) de la Vista 360°.
     *
     * @param cliente entidad del cliente
     * @return objeto {@link KpisCliente360} calculado
     */
    KpisCliente360 obtenerKpisCliente360(Cliente cliente);

    /**
     * Registra un nuevo cliente B2B y genera credenciales temporales de portal si se especificó contacto.
     *
     * @param empresaId identificador de la empresa
     * @param dto formulario con datos del cliente y servicio contratado
     * @return resultado compuesto con el cliente persistido y credenciales de acceso
     * @throws IllegalArgumentException si el RUC ya existe para la misma empresa
     */
    ResultadoCreacionCliente crearCliente(Long empresaId, ClienteDto dto);

    /**
     * Modifica los datos de un cliente existente.
     *
     * @param id identificador del cliente
     * @param empresaId identificador de la empresa
     * @param dto nuevos datos del cliente
     * @return entidad {@link Cliente} actualizada
     * @throws org.springframework.web.server.ResponseStatusException si el cliente no existe
     */
    Cliente actualizarCliente(Long id, Long empresaId, ClienteDto dto);

    /**
     * Elimina un cliente y desvincula sus relaciones asociadas.
     *
     * @param id identificador del cliente
     * @param empresaId identificador de la empresa
     * @throws org.springframework.web.server.ResponseStatusException si el cliente no existe
     */
    void eliminarCliente(Long id, Long empresaId);

    /**
     * Convierte una entidad {@link Cliente} en su DTO correspondiente para edición.
     *
     * @param cliente entidad del cliente
     * @return objeto {@link ClienteDto} poblado
     */
    ClienteDto toDto(Cliente cliente);

    /**
     * Obtiene los contratos de servicios vigentes suscritos por el cliente.
     *
     * @param clienteId identificador del cliente
     * @return lista de contratos {@link ClienteServicio}
     */
    List<ClienteServicio> obtenerServiciosPorCliente(Long clienteId);

    /**
     * Agrega un nuevo contacto a la ficha del cliente, asignándole credenciales de portal.
     *
     * @param clienteId identificador del cliente
     * @param contacto entidad con los datos del contacto
     * @return resultado con el contacto persistido y sus credenciales
     */
    ResultadoContacto agregarContacto(Long clienteId, Contacto contacto);

    /**
     * Actualiza la información de un contacto específico.
     *
     * @param clienteId identificador del cliente
     * @param contactoId identificador del contacto
     * @param datosContacto nuevos datos
     * @return contacto actualizado
     */
    Contacto actualizarContacto(Long clienteId, Long contactoId, Contacto datosContacto);

    /**
     * Lista únicamente los contactos activos de un cliente.
     *
     * @param clienteId identificador del cliente
     * @return lista de contactos activos
     */
    List<Contacto> obtenerContactosActivos(Long clienteId);

    /**
     * Lista los contactos desactivados o inactivos de un cliente.
     *
     * @param clienteId identificador del cliente
     * @return lista de contactos inactivos
     */
    List<Contacto> obtenerContactosInactivos(Long clienteId);

    /**
     * Designa a un contacto como el contacto principal y genera sus credenciales de autoservicio.
     *
     * @param clienteId identificador del cliente
     * @param contactoId identificador del contacto
     * @return credenciales temporales generadas
     */
    CredencialesTemporales establecerContactoPrincipal(Long clienteId, Long contactoId);

    /**
     * Desactiva un contacto y suspende su acceso al portal de clientes.
     *
     * @param clienteId identificador del cliente
     * @param contactoId identificador del contacto
     */
    void desactivarContacto(Long clienteId, Long contactoId);

    /**
     * Reactiva un contacto inactivo y regenera su contraseña temporal de acceso.
     *
     * @param clienteId identificador del cliente
     * @param contactoId identificador del contacto
     * @return nuevas credenciales temporales generadas
     */
    CredencialesTemporales reactivarContacto(Long clienteId, Long contactoId);

    /**
     * Genera una nueva contraseña temporal para el contacto en el portal de autoservicio.
     *
     * @param clienteId identificador del cliente
     * @param contactoId identificador del contacto
     * @return credenciales con la nueva clave temporal
     */
    CredencialesTemporales restablecerCredencialesPortal(Long clienteId, Long contactoId);

    /**
     * Obtiene el historial de encuestas de satisfacción respondidas por el cliente.
     *
     * @param clienteId identificador del cliente
     * @return lista de encuestas registradas
     */
    List<EncuestaSatisfaccion> obtenerEncuestasPorCliente(Long clienteId);

    /**
     * Obtiene el listado de contratos y renovaciones vinculados al cliente.
     *
     * @param clienteId identificador del cliente
     * @return lista de renovaciones
     */
    List<Renovacion> obtenerRenovacionesPorCliente(Long clienteId);
}
