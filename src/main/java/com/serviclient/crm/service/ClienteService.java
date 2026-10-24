package com.serviclient.crm.service;

import com.serviclient.crm.repository.ClienteRepository;
import com.serviclient.crm.repository.ClienteServicioRepository;
import com.serviclient.crm.repository.ContactoRepository;
import com.serviclient.crm.repository.EncuestaSatisfaccionRepository;
import com.serviclient.crm.repository.HealthScoreHistorialRepository;
import com.serviclient.crm.repository.RenovacionRepository;
import com.serviclient.crm.repository.ServicioRepository;
import com.serviclient.crm.repository.TicketRepository;
import com.serviclient.crm.repository.UsuarioRepository;
import com.serviclient.crm.dto.ClienteDto;
import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.entity.enums.EstadoSaludCliente;
import com.serviclient.crm.entity.enums.EstadoServicioCliente;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio de lógica de negocio para la gestión de clientes B2B.
 *
 * <p>Proporciona operaciones CRUD sobre la entidad {@link com.serviclient.crm.entity.Cliente},
 * cálculo de métricas del panel de clientes, KPIs de la Vista 360° y la lógica de creación de
 * clientes con validación de unicidad de RUC.
 *
 * <p><b>Reglas de negocio:</b>
 *
 * <ul>
 *   <li>El RUC es único por empresa. Se lanza {@link IllegalArgumentException} si ya existe un
 *       cliente con el mismo RUC para la misma empresa.
 *   <li>El Health Score se inicializa en 80 al crear un cliente y se recalcula automáticamente
 *       cuando cambian sus tickets, CSAT o renovaciones.
 * </ul>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.controller.ClienteController
 * @see com.serviclient.crm.entity.Cliente
 * @see HealthScoreService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService implements IClienteService {

  private final ClienteRepository clienteRepository;
  private final ContactoRepository contactoRepository;
  private final ServicioRepository servicioRepository;
  private final ClienteServicioRepository clienteServicioRepository;
  private final HealthScoreHistorialRepository healthScoreHistorialRepository;
  private final UsuarioRepository usuarioRepository;
  private final TicketRepository ticketRepository;
  private final EncuestaSatisfaccionRepository encuestaSatisfaccionRepository;
  private final RenovacionRepository renovacionRepository;
  private final EmpresaService empresaService;
  private final HealthScoreService healthScoreService;
  private final CuentaPortalService cuentaPortalService;


  @Transactional(readOnly = true)
  public MetricasClientes obtenerMetricas(Long empresaId) {
    List<Cliente> todos = clienteRepository.findByEmpresaId(empresaId);
    long total = todos.size();
    long saludables =
        todos.stream().filter(c -> c.getEstadoSalud() == EstadoCliente.SALUDABLE).count();
    long observacion =
        todos.stream().filter(c -> c.getEstadoSalud() == EstadoCliente.OBSERVACION).count();
    long riesgo = todos.stream().filter(c -> c.getEstadoSalud() == EstadoCliente.EN_RIESGO).count();

    return MetricasClientes.builder()
        .totalClientes(total)
        .saludables(saludables)
        .enObservacion(observacion)
        .enRiesgo(riesgo)
        .build();
  }

  /**
   * Lista los clientes de una empresa con soporte de búsqueda y filtro por estado de salud. El
   * filtro por estado se aplica en memoria después de la consulta paginada al repositorio.
   *
   * @param empresaId identificador de la empresa
   * @param busqueda término libre de búsqueda por nombre comercial; puede ser {@code null}
   * @param estado filtro por {@link com.serviclient.crm.entity.enums.EstadoCliente}; puede ser
   *     {@code null}
   * @param pageable configuración de página y ordenamiento
   * @return página de clientes filtrada
   */
  @Transactional(readOnly = true)
  public Page<Cliente> listarClientes(
      Long empresaId, String busqueda, EstadoCliente estado, Pageable pageable) {
    Page<Cliente> page = clienteRepository.buscarConFiltros(empresaId, busqueda, pageable);
    if (estado != null) {
      List<Cliente> filtered =
          page.getContent().stream()
              .filter(c -> c.getEstadoSalud() == estado)
              .collect(Collectors.toList());
      return new PageImpl<>(filtered, pageable, filtered.size());
    }
    return page;
  }

  // --- APACHE POI (Exportar Excel) ---
  @Transactional(readOnly = true)
  public byte[] exportarClientesExcel(Long empresaId, String busqueda, EstadoCliente estado) {

      Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("nombreComercial").ascending());
      List<Cliente> clientes = listarClientes(empresaId, busqueda, estado, pageable).getContent();

      try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          Sheet sheet = workbook.createSheet("Clientes");

          CellStyle headerStyle = workbook.createCellStyle();
          Font headerFont = workbook.createFont();
          headerFont.setBold(true);
          headerStyle.setFont(headerFont);
          headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
          headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

          String[] columnas = {"Nombre Comercial", "Razón Social", "RUC", "Industria", "Responsable", "Servicio Contratado", "Estado Salud", "Health Score"};
          Row headerRow = sheet.createRow(0);
          for (int i = 0; i < columnas.length; i++) {
              Cell cell = headerRow.createCell(i);
              cell.setCellValue(columnas[i]);
              cell.setCellStyle(headerStyle);
          }

          int rowIdx = 1;
          for (Cliente cliente : clientes) {
              Row row = sheet.createRow(rowIdx++);
              row.createCell(0).setCellValue(cliente.getNombreComercial() != null ? cliente.getNombreComercial() : "");
              row.createCell(1).setCellValue(cliente.getRazonSocial() != null ? cliente.getRazonSocial() : "");
              row.createCell(2).setCellValue(cliente.getRuc() != null ? cliente.getRuc() : "");
              row.createCell(3).setCellValue(cliente.getIndustria() != null ? cliente.getIndustria() : "");
              row.createCell(4).setCellValue(cliente.getResponsable() != null ? cliente.getResponsable().getNombreCompleto() : "Sin asignar");
              row.createCell(5).setCellValue(cliente.getServicioContratado() != null ? cliente.getServicioContratado() : "");
              row.createCell(6).setCellValue(cliente.getEstadoSalud() != null ? cliente.getEstadoSalud().getEtiqueta() : "");
              row.createCell(7).setCellValue(cliente.getHealthScore() != null ? cliente.getHealthScore() : 0);
          }

          for (int i = 0; i < columnas.length; i++) {
              sheet.autoSizeColumn(i);
          }

          workbook.write(out);
          return out.toByteArray();
          
      } catch (IOException e) {
          log.error("Error al generar el archivo Excel de clientes", e);
          throw new RuntimeException("No se pudo generar el archivo Excel.");
      }
  }

  @Transactional(readOnly = true)
  public List<Cliente> listarTodosPorEmpresa(Long empresaId) {
    return clienteRepository.findByEmpresaId(empresaId);
  }

  @Transactional(readOnly = true)
  public Cliente obtenerPorId(Long id) {
    return clienteRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));
  }

  @Transactional(readOnly = true)
  public Cliente obtenerPorIdYEmpresa(Long id, Long empresaId) {
    return clienteRepository
        .findByIdAndEmpresaId(id, empresaId)
        .orElseThrow(
            () -> {
              log.warn("Acceso rechazado a cliente. clienteId={}, empresaId={}", id, empresaId);

              return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado.");
            });
  }

  @Transactional(readOnly = true)
  public KpisCliente360 obtenerKpisCliente360(Cliente cliente) {
    long abiertos = ticketRepository.countByClienteIdAndEstadoNot(cliente.getId(), EstadoTicket.CERRADO);
    long criticos =
        ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(
            cliente.getId(), PrioridadTicket.CRITICA, EstadoTicket.CERRADO);

    return KpisCliente360.builder()
        .csat(cliente.getCsatPromedio() != null ? cliente.getCsatPromedio() : 0.0)
        .ticketsAbiertos(abiertos)
        .ticketsCriticos(criticos)
        .diasParaRenovacion(cliente.getDiasParaRenovacion())
        .build();
  }

  @Transactional
  public ResultadoCreacionCliente crearCliente(Long empresaId, ClienteDto dto) {
    Empresa empresa = empresaService.obtenerPorId(empresaId);
    if (clienteRepository.findByEmpresaIdAndRuc(empresaId, dto.getRuc()).isPresent()) {
      log.warn(
          "Registro de cliente rechazado por RUC duplicado. empresaId={}, ruc={}",
          empresaId,
          dto.getRuc());

      throw new IllegalArgumentException("Ya existe un cliente registrado con este RUC.");
    }
    Usuario responsable = null;
    if (dto.getResponsableId() != null) {
      responsable = usuarioRepository.findById(dto.getResponsableId()).orElse(null);
      if (responsable != null
          && (responsable.getEmpresa() == null
              || !empresaId.equals(responsable.getEmpresa().getId()))) {
        throw new IllegalArgumentException("El responsable no pertenece a la empresa actual.");
      }
    }

    Cliente cliente =
        Cliente.builder()
            .empresa(empresa)
            .nombreComercial(dto.getNombreComercial())
            .razonSocial(dto.getRazonSocial())
            .ruc(dto.getRuc())
            .industria(dto.getIndustria())
            .tamanoEmpresa(dto.getTamanoEmpresa())
            .pais(dto.getPais())
            .ciudad(dto.getCiudad())
            .direccion(dto.getDireccion())
            .sitioWeb(dto.getSitioWeb())
            .responsable(responsable)
            .estado("ACTIVO")
            .logoUrl(dto.getLogoUrl())
            .build();

    Cliente guardado = clienteRepository.save(cliente);
    log.info(
        "Cliente creado correctamente. clienteId={}, empresaId={}", guardado.getId(), empresaId);

    // Crear o vincular Servicio
    String nombreServicio =
        (dto.getServicioContratado() != null && !dto.getServicioContratado().isBlank())
            ? dto.getServicioContratado()
            : "Plan SaaS Cloud";

    Servicio servicio =
        servicioRepository.findByEmpresaId(empresaId).stream()
            .filter(s -> s.getNombre().equalsIgnoreCase(nombreServicio))
            .findFirst()
            .orElseGet(
                () ->
                    servicioRepository.save(
                        Servicio.builder()
                            .empresa(empresa)
                            .nombre(nombreServicio)
                            .descripcion(nombreServicio + " para cliente")
                            .codigo("SRV-" + Math.abs(nombreServicio.hashCode() % 10000))
                            .estado("ACTIVO")
                            .build()));

    LocalDate fechaIni = dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now();
    LocalDate fechaFin =
        dto.getFechaRenovacion() != null ? dto.getFechaRenovacion() : LocalDate.now().plusYears(1);

    ClienteServicio cs =
        ClienteServicio.builder()
            .cliente(guardado)
            .servicio(servicio)
            .fechaInicio(fechaIni)
            .fechaFin(fechaFin)
            .estado(EstadoServicioCliente.ACTIVO)
            .build();
    clienteServicioRepository.save(cs);

    CuentaPortalService.CredencialesTemporales credencialesPortal = null;

    // Contacto principal
    if (dto.getContactoNombre() != null
        && !dto.getContactoNombre().isBlank()
        && dto.getContactoEmail() != null
        && !dto.getContactoEmail().isBlank()) {
      Contacto contacto =
          Contacto.builder()
              .cliente(guardado)
              .nombre(dto.getContactoNombre())
              .apellido(dto.getContactoApellido())
              .cargo(dto.getContactoCargo())
              .email(dto.getContactoEmail())
              .telefono(dto.getContactoTelefono())
              .esContactoPrincipal(true)
              .estado("ACTIVO")
              .build();
      contactoRepository.save(contacto);
      guardado.getContactos().add(contacto);
      credencialesPortal = cuentaPortalService.activarOCrearCuenta(contacto);
    }

    // Historial Health Score inicial
    int scoreInicial = dto.getHealthScore() != null ? dto.getHealthScore() : 80;
    HealthScoreHistorial hsh =
        HealthScoreHistorial.builder()
            .cliente(guardado)
            .puntaje(new BigDecimal(scoreInicial))
            .estado(
                scoreInicial >= 80
                    ? EstadoSaludCliente.SALUDABLE
                    : (scoreInicial >= 50
                        ? EstadoSaludCliente.EN_OBSERVACION
                        : EstadoSaludCliente.EN_RIESGO))
            .puntajeTickets(new BigDecimal("30.00"))
            .puntajeCsat(new BigDecimal("30.00"))
            .puntajeActividad(new BigDecimal("15.00"))
            .puntajeRenovacion(new BigDecimal("25.00"))
            .build();
    healthScoreHistorialRepository.save(hsh);

    healthScoreService.recalcularYActualizarHealthScore(guardado);

    return new ResultadoCreacionCliente(guardado, credencialesPortal);
  }

  @Transactional
  public Cliente actualizarCliente(Long id, Long empresaId, ClienteDto dto) {
    Cliente cliente = obtenerPorIdYEmpresa(id, empresaId);

    Usuario responsable = null;
    if (dto.getResponsableId() != null) {
      responsable = usuarioRepository.findById(dto.getResponsableId()).orElse(null);
      if (responsable != null
          && (responsable.getEmpresa() == null
              || !empresaId.equals(responsable.getEmpresa().getId()))) {
        throw new IllegalArgumentException("El responsable no pertenece a la empresa actual.");
      }
    }

    cliente.setNombreComercial(dto.getNombreComercial());
    cliente.setRazonSocial(dto.getRazonSocial());
    cliente.setRuc(dto.getRuc());
    cliente.setIndustria(dto.getIndustria());
    cliente.setTamanoEmpresa(dto.getTamanoEmpresa());
    cliente.setPais(dto.getPais());
    cliente.setCiudad(dto.getCiudad());
    cliente.setDireccion(dto.getDireccion());
    cliente.setSitioWeb(dto.getSitioWeb());
    cliente.setResponsable(responsable);

    if (dto.getLogoUrl() != null) cliente.setLogoUrl(dto.getLogoUrl());

    // Actualizar servicio contratado
    if (dto.getServicioContratado() != null && !dto.getServicioContratado().isBlank()) {
      Empresa empresa = empresaService.obtenerPorId(empresaId);
      String nombreServicio = dto.getServicioContratado();

      Servicio servicio =
          servicioRepository.findByEmpresaId(empresaId).stream()
              .filter(s -> s.getNombre().equalsIgnoreCase(nombreServicio))
              .findFirst()
              .orElseGet(
                  () ->
                      servicioRepository.save(
                          Servicio.builder()
                              .empresa(empresa)
                              .nombre(nombreServicio)
                              .descripcion(nombreServicio + " para cliente")
                              .codigo("SRV-" + Math.abs(nombreServicio.hashCode() % 10000))
                              .estado("ACTIVO")
                              .build()));

      // Actualizar o crear ClienteServicio
      List<ClienteServicio> serviciosActuales = clienteServicioRepository.findByClienteId(id);
      if (!serviciosActuales.isEmpty()) {
        ClienteServicio cs = serviciosActuales.get(0);
        cs.setServicio(servicio);
        if (dto.getFechaInicio() != null) cs.setFechaInicio(dto.getFechaInicio());
        if (dto.getFechaRenovacion() != null) cs.setFechaFin(dto.getFechaRenovacion());
        clienteServicioRepository.save(cs);
      } else {
        LocalDate fechaIni = dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now();
        LocalDate fechaFin =
            dto.getFechaRenovacion() != null
                ? dto.getFechaRenovacion()
                : LocalDate.now().plusYears(1);
        ClienteServicio cs =
            ClienteServicio.builder()
                .cliente(cliente)
                .servicio(servicio)
                .fechaInicio(fechaIni)
                .fechaFin(fechaFin)
                .estado(EstadoServicioCliente.ACTIVO)
                .build();
        clienteServicioRepository.save(cs);
      }
    }

    return clienteRepository.save(cliente);
  }

  @Transactional
  public void eliminarCliente(Long id, Long empresaId) {
    Cliente cliente = obtenerPorIdYEmpresa(id, empresaId);
    clienteRepository.delete(cliente);
  }

  @Transactional(readOnly = true)
  public ClienteDto toDto(Cliente cliente) {
    ClienteDto dto = new ClienteDto();
    dto.setId(cliente.getId());
    dto.setNombreComercial(cliente.getNombreComercial());
    dto.setRazonSocial(cliente.getRazonSocial());
    dto.setRuc(cliente.getRuc());
    dto.setIndustria(cliente.getIndustria());
    dto.setTamanoEmpresa(cliente.getTamanoEmpresa());
    dto.setPais(cliente.getPais());
    dto.setCiudad(cliente.getCiudad());
    dto.setDireccion(cliente.getDireccion());
    dto.setSitioWeb(cliente.getSitioWeb());
    dto.setLogoUrl(cliente.getLogoUrl());

    if (cliente.getResponsable() != null) {
      dto.setResponsableId(cliente.getResponsable().getId());
    }
    dto.setServicioContratado(cliente.getServicioContratado());
    dto.setFechaInicio(cliente.getFechaInicio());
    dto.setFechaRenovacion(cliente.getFechaRenovacion());
    return dto;
  }

  @Transactional(readOnly = true)
  public List<ClienteServicio> obtenerServiciosPorCliente(Long clienteId) {
    return clienteServicioRepository.findByClienteId(clienteId);
  }

  @Transactional
  public ResultadoContacto agregarContacto(Long clienteId, Contacto contacto) {

    contacto.setId(null);
    Cliente cliente = obtenerPorId(clienteId);

    List<Contacto> activos =
        contactoRepository.findByClienteId(clienteId).stream()
            .filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado()))
            .toList();

    contacto.setCliente(cliente);
    contacto.setEstado("ACTIVO");

    boolean existePrincipal =
        activos.stream().anyMatch(c -> Boolean.TRUE.equals(c.getEsPrincipal()));

    boolean seraPrincipal = !existePrincipal;

    contacto.setEsContactoPrincipal(seraPrincipal);

    Contacto guardado = contactoRepository.save(contacto);

    CuentaPortalService.CredencialesTemporales credenciales = null;

    if (seraPrincipal) {
      credenciales = cuentaPortalService.activarOCrearCuenta(guardado);
    }

    return new ResultadoContacto(guardado, credenciales);
  }

  @Transactional
  public Contacto actualizarContacto(Long clienteId, Long contactoId, Contacto datosContacto) {

    Contacto contacto =
        contactoRepository
            .findById(contactoId)
            .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado."));

    // Seguridad: comprobar que realmente pertenece al cliente
    if (!contacto.getCliente().getId().equals(clienteId)) {
      throw new IllegalArgumentException("El contacto no pertenece a este cliente.");
    }

    if (datosContacto.getNombre() == null || datosContacto.getNombre().isBlank()) {
      throw new IllegalArgumentException("El nombre del contacto es obligatorio.");
    }

    if (datosContacto.getEmail() == null || datosContacto.getEmail().isBlank()) {
      throw new IllegalArgumentException("El correo del contacto es obligatorio.");
    }

    // Solo permitimos modificar información personal.
    // Principal/secundario y estado se manejan con sus propias acciones.
    contacto.setNombre(datosContacto.getNombre().trim());

    contacto.setApellido(
        datosContacto.getApellido() != null ? datosContacto.getApellido().trim() : null);

    contacto.setCargo(datosContacto.getCargo() != null ? datosContacto.getCargo().trim() : null);

    contacto.setEmail(datosContacto.getEmail().trim());

    contacto.setTelefono(
        datosContacto.getTelefono() != null ? datosContacto.getTelefono().trim() : null);

    Contacto actualizado = contactoRepository.save(contacto);

    /*
     * Si este contacto alguna vez tuvo una CuentaPortal,
     * también sincronizamos su correo.
     *
     * No cambia:
     * - contraseña
     * - estado de la cuenta
     * - requiereCambioPassword
     */
    cuentaPortalService.actualizarEmailSiExiste(actualizado);

    return actualizado;
  }

  @Transactional(readOnly = true)
  public List<Contacto> obtenerContactosActivos(Long clienteId) {
    return contactoRepository.findByClienteId(clienteId).stream()
        .filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Contacto> obtenerContactosInactivos(Long clienteId) {
    return contactoRepository.findByClienteId(clienteId).stream()
        .filter(c -> "INACTIVO".equalsIgnoreCase(c.getEstado()))
        .toList();
  }

  @Transactional
  public CuentaPortalService.CredencialesTemporales establecerContactoPrincipal(
      Long clienteId, Long contactoId) {

    List<Contacto> contactos = contactoRepository.findByClienteId(clienteId);

    Contacto nuevoPrincipal =
        contactos.stream()
            .filter(c -> c.getId().equals(contactoId))
            .filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado()))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("El contacto no existe o está inactivo."));

    if (Boolean.TRUE.equals(nuevoPrincipal.getEsPrincipal())) {
      return null;
    }

    for (Contacto contacto : contactos) {

      if (!"ACTIVO".equalsIgnoreCase(contacto.getEstado())) {
        continue;
      }

      boolean seraPrincipal = contacto.getId().equals(contactoId);

      if (Boolean.TRUE.equals(contacto.getEsPrincipal()) && !seraPrincipal) {

        cuentaPortalService.desactivarCuenta(contacto.getId());
      }

      contacto.setEsContactoPrincipal(seraPrincipal);
      contactoRepository.save(contacto);
    }

    return cuentaPortalService.activarOCrearCuenta(nuevoPrincipal);
  }

  @Transactional
  public void desactivarContacto(Long clienteId, Long contactoId) {
    Contacto contacto =
        contactoRepository
            .findById(contactoId)
            .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado."));

    if (!contacto.getCliente().getId().equals(clienteId)) {
      throw new IllegalArgumentException("El contacto no pertenece a este cliente.");
    }

    if (!"ACTIVO".equalsIgnoreCase(contacto.getEstado())) {
      throw new IllegalArgumentException("El contacto ya está inactivo.");
    }

    List<Contacto> otrosActivos =
        contactoRepository.findByClienteId(clienteId).stream()
            .filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado()))
            .filter(c -> !c.getId().equals(contactoId))
            .toList();

    /*
     * Si es principal y todavía existen otros contactos,
     * primero debe elegirse cuál será el nuevo principal.
     */
    if (Boolean.TRUE.equals(contacto.getEsPrincipal()) && !otrosActivos.isEmpty()) {

      throw new IllegalStateException(
          "Este contacto es el principal. Primero selecciona otro contacto como principal.");
    }

    contacto.setEstado("INACTIVO");
    contacto.setEsContactoPrincipal(false);

    contactoRepository.save(contacto);
    cuentaPortalService.desactivarCuenta(contactoId);
  }

  @Transactional
  public CuentaPortalService.CredencialesTemporales reactivarContacto(
      Long clienteId, Long contactoId) {

    Contacto contacto =
        contactoRepository
            .findById(contactoId)
            .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado."));

    if (!contacto.getCliente().getId().equals(clienteId)) {
      throw new IllegalArgumentException("El contacto no pertenece a este cliente.");
    }

    boolean existePrincipal =
        contactoRepository.findByClienteId(clienteId).stream()
            .filter(c -> !c.getId().equals(contactoId))
            .filter(c -> "ACTIVO".equalsIgnoreCase(c.getEstado()))
            .anyMatch(c -> Boolean.TRUE.equals(c.getEsPrincipal()));

    contacto.setEstado("ACTIVO");

    boolean seraPrincipal = !existePrincipal;

    contacto.setEsContactoPrincipal(seraPrincipal);

    contactoRepository.save(contacto);

    if (seraPrincipal) {
      return cuentaPortalService.activarOCrearCuenta(contacto);
    }

    return null;
  }

  @Transactional
  public CuentaPortalService.CredencialesTemporales restablecerCredencialesPortal(
      Long clienteId, Long contactoId) {

    Contacto contacto =
        contactoRepository
            .findById(contactoId)
            .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado."));

    if (!contacto.getCliente().getId().equals(clienteId)) {
      throw new IllegalArgumentException("El contacto no pertenece al cliente.");
    }

    if (!"ACTIVO".equalsIgnoreCase(contacto.getEstado())) {
      throw new IllegalStateException("El contacto está inactivo.");
    }

    if (!Boolean.TRUE.equals(contacto.getEsPrincipal())) {
      throw new IllegalStateException(
          "Solo el contacto principal puede tener acceso al Portal Cliente.");
    }

    return cuentaPortalService.activarOCrearCuenta(contacto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<EncuestaSatisfaccion> obtenerEncuestasPorCliente(Long clienteId) {
    return encuestaSatisfaccionRepository.findByClienteId(clienteId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Renovacion> obtenerRenovacionesPorCliente(Long clienteId) {
    return renovacionRepository.findByClienteId(clienteId);
  }
}
