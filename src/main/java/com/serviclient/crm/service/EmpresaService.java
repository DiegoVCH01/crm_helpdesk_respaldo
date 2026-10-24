package com.serviclient.crm.service;

import com.google.common.base.Preconditions;
import com.serviclient.crm.repository.ConfiguracionCsatRepository;
import com.serviclient.crm.repository.ConfiguracionHealthScoreRepository;
import com.serviclient.crm.repository.ConfiguracionSlaRepository;
import com.serviclient.crm.repository.EmpresaRepository;
import com.serviclient.crm.repository.RolRepository;
import com.serviclient.crm.repository.UsuarioRepository;
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
public class EmpresaService implements IEmpresaService {

  private final EmpresaRepository empresaRepository;
  private final RolRepository rolRepository;
  private final UsuarioRepository usuarioRepository;
  private final ConfiguracionSlaRepository configuracionSlaRepository;
  private final ConfiguracionCsatRepository configuracionCsatRepository;
  private final ConfiguracionHealthScoreRepository configuracionHealthScoreRepository;
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


    if (usuarioRepository.existsByEmail(dto.getAdminEmail())) {
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

    Empresa empresaGuardada = empresaRepository.save(empresa);

    // 2. Obtener el rol global Administrador
    RolEntity rolAdmin =
        rolRepository
            .findByNombre("Administrador")
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No se encontró el rol global Administrador."));

    // 3. Crear Configuraciones de SLA, CSAT y Health Score
    int sla1ra =
        dto.getSlaPrimerRespuestaHoras() != null ? dto.getSlaPrimerRespuestaHoras() * 60 : 120;
    int slaResol = dto.getSlaResolucionHoras() != null ? dto.getSlaResolucionHoras() * 60 : 1440;

    configuracionSlaRepository.save(
        ConfiguracionSla.builder()
            .empresa(empresaGuardada)
            .prioridad(PrioridadTicket.CRITICA)
            .tiempoRespuestaMinutos(Math.max(30, sla1ra / 4))
            .tiempoResolucionMinutos(Math.max(240, slaResol / 4))
            .estado("ACTIVO")
            .build());

    configuracionSlaRepository.save(
        ConfiguracionSla.builder()
            .empresa(empresaGuardada)
            .prioridad(PrioridadTicket.ALTA)
            .tiempoRespuestaMinutos(Math.max(60, sla1ra / 2))
            .tiempoResolucionMinutos(Math.max(480, slaResol / 2))
            .estado("ACTIVO")
            .build());

    configuracionSlaRepository.save(
        ConfiguracionSla.builder()
            .empresa(empresaGuardada)
            .prioridad(PrioridadTicket.MEDIA)
            .tiempoRespuestaMinutos(sla1ra)
            .tiempoResolucionMinutos(slaResol)
            .estado("ACTIVO")
            .build());

    configuracionSlaRepository.save(
        ConfiguracionSla.builder()
            .empresa(empresaGuardada)
            .prioridad(PrioridadTicket.BAJA)
            .tiempoRespuestaMinutos(sla1ra * 2)
            .tiempoResolucionMinutos(slaResol * 2)
            .estado("ACTIVO")
            .build());

    configuracionCsatRepository.save(
        ConfiguracionCsat.builder()
            .empresa(empresaGuardada)
            .activo(dto.getActivarCsat() != null ? dto.getActivarCsat() : true)
            .escalaMin(1)
            .escalaMax(5)
            .pregunta("¿Qué tan satisfecho estás con la atención recibida?")
            .build());

    configuracionHealthScoreRepository.save(
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

    usuarioRepository.save(admin);

    log.info(
    "Empresa registrada correctamente. empresaId={}",
    empresaGuardada.getId());

    return empresaGuardada;
  }

  @Override
  @Transactional(readOnly = true)
  public Empresa obtenerPorId(Long id) {
    return empresaRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con id: " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public Empresa obtenerPrimera() {
    return empresaRepository.findFirstByOrderByIdAsc()
        .orElseThrow(() -> new IllegalStateException("No existe ninguna empresa registrada."));
  }
}
