package com.serviclient.crm.service;

import com.google.common.base.Preconditions;
import com.serviclient.crm.dao.ConfiguracionCsatDAO;
import com.serviclient.crm.dao.ConfiguracionHealthScoreDAO;
import com.serviclient.crm.dao.ConfiguracionSlaDAO;
import com.serviclient.crm.dao.EmpresaDAO;
import com.serviclient.crm.dao.RolDAO;
import com.serviclient.crm.dao.UsuarioDAO;
import com.serviclient.crm.dto.RegistroEmpresaDto;
import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaService {

  private final EmpresaDAO empresaDAO;
  private final RolDAO rolDAO;
  private final UsuarioDAO usuarioDAO;
  private final ConfiguracionSlaDAO configuracionSlaDAO;
  private final ConfiguracionCsatDAO configuracionCsatDAO;
  private final ConfiguracionHealthScoreDAO configuracionHealthScoreDAO;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public Empresa registrarEmpresaCompleta(RegistroEmpresaDto dto) {

    log.info("Iniciando registro de una nueva empresa");
    // --- GUAVA PRECONDITIONS
    Preconditions.checkNotNull(dto, "El formulario de registro de empresa no puede ser nulo");
    Preconditions.checkArgument(
        dto.getNombreComercial() != null && !dto.getNombreComercial().isBlank(),
        "El nombre comercial es obligatorio");
    Preconditions.checkArgument(
        dto.getAdminEmail() != null && !dto.getAdminEmail().isBlank(),
        "El correo del administrador es obligatorio");
    Preconditions.checkArgument(
        dto.getAdminPassword() != null && !dto.getAdminPassword().isBlank(),
        "La contraseña del administrador es obligatoria");


    if (usuarioDAO.existsByEmail(dto.getAdminEmail())) {
      log.warn("Registro de empresa rechazado: correo de administrador ya registrado");
      throw new IllegalArgumentException(
          "Ya existe un usuario registrado con el correo: " + dto.getAdminEmail());
    }

    // 1. Crear Empresa
    Empresa empresa =
        Empresa.builder()
            .nombreComercial(dto.getNombreComercial())
            .razonSocial(dto.getRazonSocial())
            .ruc(dto.getRuc())
            .industria(dto.getIndustria())
            .tamanoEmpresa(dto.getTamanoEmpresa())
            .pais(dto.getPais())
            .ciudad(dto.getCiudad())
            .direccion(dto.getDireccion())
            .sitioWeb(dto.getSitioWeb())
            .logoUrl(dto.getLogoUrl())
            .zonaHoraria("America/Lima")
            .estado("ACTIVA")
            .build();

    Empresa empresaGuardada = empresaDAO.save(empresa);

    // 2. Obtener el rol global Administrador
    RolEntity rolAdmin =
        rolDAO
            .findByNombre("Administrador")
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No se encontró el rol global Administrador."));

    // 3. Crear Configuraciones de SLA, CSAT y Health Score
    int sla1ra =
        dto.getSlaPrimerRespuestaHoras() != null ? dto.getSlaPrimerRespuestaHoras() * 60 : 120;
    int slaResol = dto.getSlaResolucionHoras() != null ? dto.getSlaResolucionHoras() * 60 : 1440;

    configuracionSlaDAO.save(
        ConfiguracionSla.builder()
            .empresa(empresaGuardada)
            .prioridad(PrioridadTicket.CRITICA)
            .tiempoRespuestaMinutos(Math.max(30, sla1ra / 4))
            .tiempoResolucionMinutos(Math.max(240, slaResol / 4))
            .estado("ACTIVO")
            .build());

    configuracionSlaDAO.save(
        ConfiguracionSla.builder()
            .empresa(empresaGuardada)
            .prioridad(PrioridadTicket.ALTA)
            .tiempoRespuestaMinutos(Math.max(60, sla1ra / 2))
            .tiempoResolucionMinutos(Math.max(480, slaResol / 2))
            .estado("ACTIVO")
            .build());

    configuracionSlaDAO.save(
        ConfiguracionSla.builder()
            .empresa(empresaGuardada)
            .prioridad(PrioridadTicket.MEDIA)
            .tiempoRespuestaMinutos(sla1ra)
            .tiempoResolucionMinutos(slaResol)
            .estado("ACTIVO")
            .build());

    configuracionSlaDAO.save(
        ConfiguracionSla.builder()
            .empresa(empresaGuardada)
            .prioridad(PrioridadTicket.BAJA)
            .tiempoRespuestaMinutos(sla1ra * 2)
            .tiempoResolucionMinutos(slaResol * 2)
            .estado("ACTIVO")
            .build());

    configuracionCsatDAO.save(
        ConfiguracionCsat.builder()
            .empresa(empresaGuardada)
            .activo(dto.getActivarCsat() != null ? dto.getActivarCsat() : true)
            .escalaMin(1)
            .escalaMax(5)
            .pregunta("¿Qué tan satisfecho estás con la atención recibida?")
            .build());

    configuracionHealthScoreDAO.save(
        ConfiguracionHealthScore.builder()
            .empresa(empresaGuardada)
            .pesoTickets(new BigDecimal("30.00"))
            .pesoCsat(new BigDecimal("30.00"))
            .pesoActividad(new BigDecimal("15.00"))
            .pesoRenovacion(new BigDecimal("25.00"))
            .rangoSaludableMin(80)
            .rangoObservacionMin(50)
            .estado("ACTIVO")
            .build());

    // 4. Crear Usuario Administrador
    Usuario admin =
        Usuario.builder()
            .empresa(empresaGuardada)
            .rolEntity(rolAdmin)
            .nombre(dto.getAdminNombre())
            .apellido(dto.getAdminApellido())
            .email(dto.getAdminEmail())
            .telefono(dto.getAdminTelefono())
            .password(passwordEncoder.encode(dto.getAdminPassword()))
            .debeCambiarPassword(false)
            .estado("ACTIVO")
            .build();

    usuarioDAO.save(admin);

    log.info(
    "Empresa registrada correctamente. empresaId={}",
    empresaGuardada.getId());

    return empresaGuardada;
  }

  @Transactional(readOnly = true)
  public Empresa obtenerPorId(Long id) {
    return empresaDAO
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con id: " + id));
  }
}
