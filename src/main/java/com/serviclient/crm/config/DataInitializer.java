package com.serviclient.crm.config;

import com.serviclient.crm.repository.*;
import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Inicializador de datos de prueba coherentes con la arquitectura y el Dashboard principal.
 * Se ejecuta automáticamente al arrancar la aplicación si la base de datos no tiene datos de clientes/tickets.
 * Garantiza además que las credenciales del usuario Administrador demo siempre estén sincronizadas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final ContactoRepository contactoRepository;
    private final ClienteServicioRepository clienteServicioRepository;
    private final CategoriaTicketRepository categoriaTicketRepository;
    private final ConfiguracionSlaRepository configuracionSlaRepository;
    private final ConfiguracionCsatRepository configuracionCsatRepository;
    private final ConfiguracionHealthScoreRepository configuracionHealthScoreRepository;
    private final TicketRepository ticketRepository;
    private final TicketMensajeRepository ticketMensajeRepository;
    private final TicketHistorialRepository ticketHistorialRepository;
    private final TicketAdjuntoRepository ticketAdjuntoRepository;
    private final RenovacionRepository renovacionRepository;
    private final RenovacionSeguimientoRepository renovacionSeguimientoRepository;
    private final EncuestaCsatRepository encuestaCsatRepository;
    private final RespuestaCsatRepository respuestaCsatRepository;
    private final EncuestaSatisfaccionRepository encuestaSatisfaccionRepository;
    private final HealthScoreHistorialRepository healthScoreHistorialRepository;
    private final InteraccionRepository interaccionRepository;
    private final NotificacionRepository notificacionRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Punto de entrada de inicialización de datos ejecutado por Spring Boot al arrancar el contexto.
     * Verifica la existencia de datos y realiza la siembra inicial (roles, permisos, empresa demo, usuarios y tickets).
     *
     * @param args argumentos de línea de comandos suministrados a la aplicación
     */
    @Override
    @Transactional
    public void run(String... args) {
        log.info("Verificando estado de la base de datos...");

        // 1. Empresa Principal
        Empresa empresa = empresaRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
            Empresa e = Empresa.builder()
                    .nombreComercial("ServiClient")
                    .razonSocial("ServiClient Cloud Solutions S.A.C.")
                    .ruc("20601234567")
                    .industria("Tecnología & SaaS")
                    .tamanoEmpresa("50-200")
                    .pais("Perú")
                    .ciudad("Lima")
                    .direccion("Av. Javier Prado Este 4200, San Isidro")
                    .sitioWeb("https://serviclient.com")
                    .zonaHoraria("America/Lima")
                    .estado("ACTIVA")
                    .build();
            return empresaRepository.save(e);
        });

        // 2. Permisos y Roles
        Set<Permiso> todosPermisos = new HashSet<>();
        if (permisoRepository.count() == 0) {
            String[] modulos = {"CLIENTES", "TICKETS", "RENOVACIONES", "CSAT", "CONFIGURACION"};
            String[] acciones = {"VER", "CREAR", "EDITAR", "ELIMINAR"};
            for (String m : modulos) {
                for (String a : acciones) {
                    Permiso p = Permiso.builder()
                            .nombre(m + "_" + a)
                            .descripcion("Permite " + a.toLowerCase() + " en el módulo " + m.toLowerCase())
                            .modulo(m)
                            .accion(a)
                            .build();
                    todosPermisos.add(permisoRepository.save(p));
                }
            }
        } else {
            todosPermisos.addAll(permisoRepository.findAll());
        }

        RolEntity rolAdmin =rolRepository.findByNombre("Administrador")
                .orElseGet(() -> rolRepository.save(RolEntity.builder()
                                .nombre("Administrador")
                                .descripcion("Acceso total y configuración del sistema")
                                .estado("ACTIVO")
                                .permisos(todosPermisos)
                                .build()));

        RolEntity rolAgente =rolRepository.findByNombre("Agente de Soporte")
                .orElseGet(() ->rolRepository.save(RolEntity.builder()
                                .nombre("Agente de Soporte")
                                .descripcion("Atención y resolución de tickets e incidencias")
                                .estado("ACTIVO")
                                .permisos(todosPermisos)
                                .build()));

        RolEntity rolSupervisor =rolRepository.findByNombre("Supervisor Customer Success")
                .orElseGet(() ->rolRepository.save(RolEntity.builder()
                                .nombre("Supervisor Customer Success")
                                .descripcion("Gestión de salud de clientes, CSAT y renovaciones")
                                .estado("ACTIVO")
                                .permisos(todosPermisos)
                                .build()));

        // 3. Usuarios de prueba
        Usuario admin = usuarioRepository.findByEmail("admin@serviclient.com").map(u -> {
            u.setPassword(passwordEncoder.encode("admin123"));
            u.setEstado("ACTIVO");
            u.setRolEntity(rolAdmin);
            u.setNombre("Juan");
            u.setApellido("Pérez");
            return usuarioRepository.save(u);
        }).orElseGet(() -> usuarioRepository.save(Usuario.builder()
                .empresa(empresa)
                .rolEntity(rolAdmin)
                .nombre("Juan")
                .apellido("Pérez")
                .email("admin@serviclient.com")
                .password(passwordEncoder.encode("admin123"))
                .telefono("+51 987 654 321")
                .estado("ACTIVO")
                .ultimoAcceso(LocalDateTime.now())
                .build()));

        Usuario carlos = usuarioRepository.findByEmail("carlos.ruiz@serviclient.com").orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .empresa(empresa)
                        .rolEntity(rolAgente)
                        .nombre("Carlos")
                        .apellido("Ruiz")
                        .email("carlos.ruiz@serviclient.com")
                        .password(passwordEncoder.encode("admin123"))
                        .telefono("+51 981 111 222")
                        .estado("ACTIVO")
                        .ultimoAcceso(LocalDateTime.now().minusHours(2))
                        .build()));

        Usuario ana = usuarioRepository.findByEmail("cristofer.silva@serviclient.com").orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .empresa(empresa)
                        .rolEntity(rolAgente)
                        .nombre("Cristofer")
                        .apellido("Silva")
                        .email("cristofer.silva@serviclient.com")
                        .password(passwordEncoder.encode("admin123"))
                        .telefono("+51 982 333 444")
                        .estado("ACTIVO")
                        .ultimoAcceso(LocalDateTime.now().minusHours(1))
                        .build()));

        Usuario laura = usuarioRepository.findByEmail("laura.mendoza@serviclient.com").orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .empresa(empresa)
                        .rolEntity(rolSupervisor)
                        .nombre("Laura")
                        .apellido("Mendoza")
                        .email("laura.mendoza@serviclient.com")
                        .password(passwordEncoder.encode("admin123"))
                        .telefono("+51 983 555 666")
                        .estado("ACTIVO")
                        .ultimoAcceso(LocalDateTime.now().minusMinutes(30))
                        .build()));

        Usuario maria = usuarioRepository.findByEmail("maria.lopez@serviclient.com").orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .empresa(empresa)
                        .rolEntity(rolAgente)
                        .nombre("María")
                        .apellido("López")
                        .email("maria.lopez@serviclient.com")
                        .password(passwordEncoder.encode("admin123"))
                        .telefono("+51 984 777 888")
                        .estado("ACTIVO")
                        .ultimoAcceso(LocalDateTime.now().minusHours(3))
                        .build()));

        Usuario javier = usuarioRepository.findByEmail("javier.ramos@serviclient.com").orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .empresa(empresa)
                        .rolEntity(rolAgente)
                        .nombre("Javier")
                        .apellido("Ramos")
                        .email("javier.ramos@serviclient.com")
                        .password(passwordEncoder.encode("admin123"))
                        .telefono("+51 985 999 000")
                        .estado("ACTIVO")
                        .ultimoAcceso(LocalDateTime.now().minusMinutes(15))
                        .build()));

        // 4. Servicios
        Servicio srvErp = servicioRepository.findByCodigo("SRV-ERP").orElseGet(() ->
                servicioRepository.save(Servicio.builder()
                        .empresa(empresa)
                        .nombre("ERP Cloud Enterprise")
                        .descripcion("Sistema integral de planificación de recursos empresariales en la nube")
                        .codigo("SRV-ERP")
                        .estado("ACTIVO")
                        .build()));

        Servicio srvHelpdesk = servicioRepository.findByCodigo("SRV-HD").orElseGet(() ->
                servicioRepository.save(Servicio.builder()
                        .empresa(empresa)
                        .nombre("Helpdesk Omni-channel")
                        .descripcion("Plataforma de soporte y atención a usuarios multicanal")
                        .codigo("SRV-HD")
                        .estado("ACTIVO")
                        .build()));

        Servicio srvCrm = servicioRepository.findByCodigo("SRV-CRM").orElseGet(() ->
                servicioRepository.save(Servicio.builder()
                        .empresa(empresa)
                        .nombre("CRM Ventas Pro")
                        .descripcion("Gestión de relaciones comerciales y pipeline de ventas")
                        .codigo("SRV-CRM")
                        .estado("ACTIVO")
                        .build()));

        Servicio srvAnalytics = servicioRepository.findByCodigo("SRV-AI").orElseGet(() ->
                servicioRepository.save(Servicio.builder()
                        .empresa(empresa)
                        .nombre("Analítica Avanzada & IA")
                        .descripcion("Módulo de analítica predictiva y dashboards ejecutivos")
                        .codigo("SRV-AI")
                        .estado("ACTIVO")
                        .build()));

        Servicio srvSoportePremium = servicioRepository.findByCodigo("SRV-SP24").orElseGet(() ->
                servicioRepository.save(Servicio.builder()
                        .empresa(empresa)
                        .nombre("Soporte Premium 24/7")
                        .descripcion("Mesa de ayuda dedicada con SLA prioritario garantizado")
                        .codigo("SRV-SP24")
                        .estado("ACTIVO")
                        .build()));

        Servicio srvModuloAnalytics = servicioRepository.findByCodigo("SRV-ANL").orElseGet(() ->
                servicioRepository.save(Servicio.builder()
                        .empresa(empresa)
                        .nombre("Módulo Analytics")
                        .descripcion("Tableros BI y visualización de indicadores en tiempo real")
                        .codigo("SRV-ANL")
                        .estado("ACTIVO")
                        .build()));

        // 5. Categorías de Tickets
        CategoriaTicketEntity catSoporte = categoriaTicketRepository.findAll().stream()
                .filter(c -> c.getNombre() != null && c.getNombre().equalsIgnoreCase("Soporte Técnico"))
                .findFirst()
                .orElseGet(() -> categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                        .empresa(empresa)
                        .nombre("Soporte Técnico")
                        .descripcion("Problemas y consultas de operación técnica")
                        .estado("ACTIVO")
                        .build()));

        CategoriaTicketEntity catFacturacion = categoriaTicketRepository.findAll().stream()
                .filter(c -> c.getNombre() != null && c.getNombre().toUpperCase().contains("FACTURACI"))
                .findFirst()
                .orElseGet(() -> categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                        .empresa(empresa)
                        .nombre("Facturación & Cobranzas")
                        .descripcion("Consultas de pagos, recibos y contratos")
                        .estado("ACTIVO")
                        .build()));

        CategoriaTicketEntity catOnboarding = categoriaTicketRepository.findAll().stream()
                .filter(c -> c.getNombre() != null && c.getNombre().toUpperCase().contains("ONBOARDING"))
                .findFirst()
                .orElseGet(() -> categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                        .empresa(empresa)
                        .nombre("Capacitación & Onboarding")
                        .descripcion("Entrenamiento e inducción para nuevos usuarios")
                        .estado("ACTIVO")
                        .build()));

        CategoriaTicketEntity catFeature = categoriaTicketRepository.findAll().stream()
                .filter(c -> c.getNombre() != null && c.getNombre().toUpperCase().contains("FEATURE"))
                .findFirst()
                .orElseGet(() -> categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                        .empresa(empresa)
                        .nombre("Solicitud de Feature")
                        .descripcion("Requerimientos y mejoras solicitadas")
                        .estado("ACTIVO")
                        .build()));

        CategoriaTicketEntity catCritica = categoriaTicketRepository.findAll().stream()
                .filter(c -> c.getNombre() != null && c.getNombre().toUpperCase().contains("CRÍTICA"))
                .findFirst()
                .orElseGet(() -> categoriaTicketRepository.save(CategoriaTicketEntity.builder()
                        .empresa(empresa)
                        .nombre("Incidencia Crítica")
                        .descripcion("Interrupción de servicios críticos")
                        .estado("ACTIVO")
                        .build()));

        // 6. Configuraciones de SLA, CSAT y Health Score
        if (configuracionSlaRepository.count() == 0) {
            configuracionSlaRepository.save(ConfiguracionSla.builder()
                    .empresa(empresa)
                    .prioridad(PrioridadTicket.CRITICA)
                    .tiempoRespuestaMinutos(30)
                    .tiempoResolucionMinutos(240)
                    .estado("ACTIVO")
                    .build());

            configuracionSlaRepository.save(ConfiguracionSla.builder()
                    .empresa(empresa)
                    .prioridad(PrioridadTicket.ALTA)
                    .tiempoRespuestaMinutos(60)
                    .tiempoResolucionMinutos(480)
                    .estado("ACTIVO")
                    .build());

            configuracionSlaRepository.save(ConfiguracionSla.builder()
                    .empresa(empresa)
                    .prioridad(PrioridadTicket.MEDIA)
                    .tiempoRespuestaMinutos(120)
                    .tiempoResolucionMinutos(1440)
                    .estado("ACTIVO")
                    .build());

            configuracionSlaRepository.save(ConfiguracionSla.builder()
                    .empresa(empresa)
                    .prioridad(PrioridadTicket.BAJA)
                    .tiempoRespuestaMinutos(240)
                    .tiempoResolucionMinutos(2880)
                    .estado("ACTIVO")
                    .build());
        }

        if (configuracionCsatRepository.count() == 0) {
            configuracionCsatRepository.save(ConfiguracionCsat.builder()
                    .empresa(empresa)
                    .activo(true)
                    .escalaMin(1)
                    .escalaMax(5)
                    .pregunta("¿Qué tan satisfecho estás con la atención recibida?")
                    .mensajeAgradecimiento("Gracias por compartir tu opinión con ServiClient.")
                    .build());
        }

        if (configuracionHealthScoreRepository.count() == 0) {
            configuracionHealthScoreRepository.save(ConfiguracionHealthScore.builder()
                    .empresa(empresa)
                    .pesoTickets(new BigDecimal("30.00"))
                    .pesoCsat(new BigDecimal("30.00"))
                    .pesoActividad(new BigDecimal("15.00"))
                    .pesoRenovacion(new BigDecimal("25.00"))
                    .rangoSaludableMin(80)
                    .rangoObservacionMin(50)
                    .estado("ACTIVO")
                    .build());
        }

        // Si ya existen clientes y tickets completos, no duplicamos
        if (clienteRepository.count() >= 5 && ticketRepository.count() >= 5) {
            log.info("[✓] Base de datos contiene registros completos de clientes y tickets.");
            return;
        }

        log.info("Inicializando clientes, contratos, tickets y datos de demostración coherentes con el Dashboard...");

        // ==========================================
        // 7. CLIENTES (Exactos al Dashboard)
        // ==========================================

        // Cliente 1: TechCorp Industries (En Riesgo - Health Score 45)
        Cliente c1 = crearCliente(empresa, ana, "TechCorp Industries", "TechCorp Industries S.A.C.", "20554433221",
                "Tecnología & SaaS", "200-500", "Perú", "Lima", "Av. Canaval y Moreyra 480, San Isidro",
                "https://techcorp.pe", "ACTIVO", 45, EstadoSaludCliente.EN_RIESGO);

        Contacto cont1 = crearContacto(c1, "Roberto", "Gómez", "CTO", "roberto.gomez@techcorp.pe", "+51 999 111 222", true);
        ClienteServicio cs1 = crearClienteServicio(c1, srvErp, LocalDate.now().minusMonths(11), LocalDate.now().plusDays(12));

        // Cliente 2: Global Logistics SA (Saludable - Health Score 85)
        Cliente c2 = crearCliente(empresa, carlos, "Global Logistics SA", "Global Logistics del Perú S.A.", "20443322110",
                "Logística & Transporte", "100-250", "Perú", "Callao", "Av. Elmer Faucett 2050, Callao",
                "https://globallogistics.pe", "ACTIVO", 85, EstadoSaludCliente.SALUDABLE);

        Contacto cont2 = crearContacto(c2, "Mariana", "Vargas", "Gerente de Operaciones", "m.vargas@globallogistics.pe", "+51 988 222 333", true);
        ClienteServicio cs2 = crearClienteServicio(c2, srvSoportePremium, LocalDate.now().minusMonths(8), LocalDate.now().plusDays(25));

        // Cliente 3: Grupo Financiero Sur (Saludable - Health Score 95)
        Cliente c3 = crearCliente(empresa, maria, "Grupo Financiero Sur", "Grupo Financiero del Sur S.A.A.", "20112233445",
                "Banca & Finanzas", "1000+", "Perú", "Lima", "Av. Las Begonias 450, San Isidro",
                "https://grupofinancierosur.pe", "ACTIVO", 95, EstadoSaludCliente.SALUDABLE);

        Contacto cont3 = crearContacto(c3, "Luis", "Morales", "Director de Analítica", "l.morales@grupofinancierosur.pe", "+51 977 333 444", true);
        ClienteServicio cs3 = crearClienteServicio(c3, srvModuloAnalytics, LocalDate.now().minusMonths(6), LocalDate.now().plusDays(32));

        // Cliente 4: NovaTech Industries (En Observación - Health Score 65)
        Cliente c4 = crearCliente(empresa, carlos, "NovaTech Industries", "NovaTech Innovations S.A.C.", "20998877665",
                "Manufactura Inteligente", "250-500", "Perú", "Lima", "Av. Argentina 3080, Cercado de Lima",
                "https://novatech.pe", "ACTIVO", 65, EstadoSaludCliente.EN_OBSERVACION);

        Contacto cont4 = crearContacto(c4, "Andrés", "Castillo", "Jefe de Producción TI", "a.castillo@novatech.pe", "+51 966 444 555", true);
        ClienteServicio cs4 = crearClienteServicio(c4, srvErp, LocalDate.now().minusMonths(5), LocalDate.now().plusDays(45));

        // Cliente 5: InnovaSoft (Saludable - Health Score 90)
        Cliente c5 = crearCliente(empresa, laura, "InnovaSoft", "InnovaSoft Consulting S.A.C.", "20334455667",
                "Software & Consultoría", "50-100", "Perú", "Lima", "Calle Dean Valdivia 148, San Isidro",
                "https://innovasoft.pe", "ACTIVO", 90, EstadoSaludCliente.SALUDABLE);

        Contacto cont5 = crearContacto(c5, "Sofía", "Benítez", "Head of Digital", "s.benitez@innovasoft.pe", "+51 955 666 777", true);
        ClienteServicio cs5 = crearClienteServicio(c5, srvCrm, LocalDate.now().minusMonths(12), LocalDate.now().minusDays(5));

        // Cliente 6: FinTech Solutions (Saludable - Health Score 88 - Nuevo este mes)
        Cliente c6 = crearCliente(empresa, admin, "FinTech Solutions", "FinTech Solutions Latam S.A.C.", "20778899001",
                "Fintech & Pagos", "50-200", "Perú", "Lima", "Av. Pardo y Aliaga 640, San Isidro",
                "https://fintechsolutions.pe", "ACTIVO", 88, EstadoSaludCliente.SALUDABLE);

        Contacto cont6 = crearContacto(c6, "Diego", "Vega", "Chief Product Officer", "d.vega@fintechsolutions.pe", "+51 944 555 666", true);
        ClienteServicio cs6 = crearClienteServicio(c6, srvErp, LocalDate.now().minusDays(15), LocalDate.now().plusMonths(12));

        // Cliente 7: CloudNet Corp (Saludable - Health Score 82)
        Cliente c7 = crearCliente(empresa, javier, "CloudNet Corp", "CloudNet Services del Perú S.A.", "20889900112",
                "Telecomunicaciones & Cloud", "500-1000", "Perú", "Lima", "Av. Javier Prado Este 2500, San Borja",
                "https://cloudnetcorp.pe", "ACTIVO", 82, EstadoSaludCliente.SALUDABLE);

        Contacto cont7 = crearContacto(c7, "Patricia", "Rivas", "Líder de Soporte TI", "p.rivas@cloudnetcorp.pe", "+51 933 444 555", true);
        ClienteServicio cs7 = crearClienteServicio(c7, srvHelpdesk, LocalDate.now().minusMonths(14), LocalDate.now().minusDays(15));

        // Cliente 8: Retail Express Perú (En Observación - Health Score 64)
        Cliente c8 = crearCliente(empresa, carlos, "Retail Express Perú", "Retail Express Perú S.A.", "20123456789",
                "Retail & E-commerce", "100-250", "Perú", "Lima", "Av. Larco 743, Miraflores",
                "https://retailexpress.pe", "ACTIVO", 64, EstadoSaludCliente.EN_OBSERVACION);

        Contacto cont8 = crearContacto(c8, "Julio", "Navarro", "Jefe de Mesa de Ayuda", "j.navarro@retailexpress.pe", "+51 922 333 444", true);
        ClienteServicio cs8 = crearClienteServicio(c8, srvHelpdesk, LocalDate.now().minusMonths(9), LocalDate.now().plusDays(24));

        // Cliente 9: Banco Metropolitano (En Riesgo - Health Score 42)
        Cliente c9 = crearCliente(empresa, laura, "Banco Metropolitano", "Banco Metropolitano S.A.", "20665544332",
                "Banca Múltiple", "1000+", "Perú", "Lima", "Av. República de Panamá 3055, San Isidro",
                "https://bancometropolitano.pe", "ACTIVO", 42, EstadoSaludCliente.EN_RIESGO);

        Contacto cont9 = crearContacto(c9, "Fernando", "Castro", "Gerente de Infraestructura", "f.castro@bancometropolitano.pe", "+51 911 222 333", true);
        ClienteServicio cs9 = crearClienteServicio(c9, srvCrm, LocalDate.now().minusMonths(11), LocalDate.now().plusDays(12));

        // Cliente 10: Minera Los Andes (Saludable - Health Score 92)
        Cliente c10 = crearCliente(empresa, ana, "Minera Los Andes", "Compañía Minera Los Andes S.A.C.", "20556677889",
                "Minería & Energía", "500-1000", "Perú", "Arequipa", "Av. Cayma 600, Arequipa",
                "https://minerala.pe", "ACTIVO", 92, EstadoSaludCliente.SALUDABLE);

        Contacto cont10 = crearContacto(c10, "Lucía", "Salazar", "Jefa de Sistemas", "lucia.salazar@minerala.pe", "+51 900 111 222", true);
        ClienteServicio cs10 = crearClienteServicio(c10, srvAnalytics, LocalDate.now().minusMonths(4), LocalDate.now().plusMonths(8));

        // ==========================================
        // 8. TICKETS (Coherentes con el Dashboard)
        // ==========================================

        // Ticket 1: TK-4029 (Banco Metropolitano - Crítico / Abierto)
        Ticket t1 = crearTicket("TK-4029", empresa, c9, cont9, catCritica, carlos,
                "Falla en módulo de Facturación Electrónica SUNAT",
                "No se pueden emitir comprobantes desde las 08:30 AM. Error HTTP 500 al comunicarse con el web service de la SUNAT.",
                PrioridadTicket.CRITICA, EstadoTicket.ABIERTO,
                LocalDateTime.now().plusMinutes(25), LocalDateTime.now().plusHours(3),
                null, null, LocalDateTime.now().minusMinutes(35));

        crearMensaje(t1, null, cont9, TipoMensajeTicket.CLIENTE, "No se pueden emitir comprobantes desde las 08:30 AM. Urgente soporte.", LocalDateTime.now().minusMinutes(35));
        crearHistorial(t1, admin, "CREACION", "Ticket crítico registrado vía Portal Web", LocalDateTime.now().minusMinutes(35));

        // Ticket 2: TK-4030 (NovaTech Industries - Crítico / Abierto / Fuera de SLA)
        Ticket t2 = crearTicket("TK-4030", empresa, c4, cont4, catCritica, javier,
                "Nuevo ticket crítico registrado por NovaTech Industries",
                "Interrupción parcial de la sincronización de inventarios y pasarelas de pago.",
                PrioridadTicket.CRITICA, EstadoTicket.ABIERTO,
                LocalDateTime.now().minusMinutes(10), LocalDateTime.now().minusHours(1),
                null, null, LocalDateTime.now().minusHours(2));

        crearMensaje(t2, null, cont4, TipoMensajeTicket.CLIENTE, "Requerimos revisión del enlace de pagos urgentemente.", LocalDateTime.now().minusHours(2));
        crearHistorial(t2, javier, "CREACION", "Ticket asignado a Soporte L2", LocalDateTime.now().minusHours(2));

        // Ticket 3: TK-4028 (Retail Express - Alta / En Proceso)
        Ticket t3 = crearTicket("TK-4028", empresa, c8, cont8, catSoporte, ana,
                "Lentitud en la generación de reportes consolidados",
                "Los reportes mensuales de inventario tardan más de 10 minutos en exportar a Excel.",
                PrioridadTicket.ALTA, EstadoTicket.EN_PROCESO,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(4),
                LocalDateTime.now().minusHours(2), null, LocalDateTime.now().minusHours(3));

        crearMensaje(t3, null, cont8, TipoMensajeTicket.CLIENTE, "Los reportes tardan demasiado en procesar.", LocalDateTime.now().minusHours(3));
        crearMensaje(t3, ana, null, TipoMensajeTicket.AGENTE, "Hola Julio, estamos optimizando los índices en la base de datos.", LocalDateTime.now().minusHours(2));
        crearHistorial(t3, ana, "RESPUESTA", "Respuesta enviada al cliente", LocalDateTime.now().minusHours(2));

        // Ticket 4: TK-4025 (TechCorp Industries - Media / Resuelto / CSAT 5)
        Ticket t4 = crearTicket("TK-4025", empresa, c1, cont1, catOnboarding, laura,
                "Configuración de nuevo usuario administrador",
                "Requerimos habilitar credenciales de acceso para el nuevo analista financiero.",
                PrioridadTicket.MEDIA, EstadoTicket.RESUELTO,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(10),
                LocalDateTime.now().minusDays(1).plusMinutes(15), LocalDateTime.now().minusHours(12), LocalDateTime.now().minusDays(1));

        crearEncuestaYRespuesta(empresa, c1, cont1, t4, 5, "Excelente soporte, todo quedó habilitado en minutos.");

        // Ticket 5: TK-4018 (CloudNet Corp - Media / Resuelto / CSAT 5)
        Ticket t5 = crearTicket("TK-4018", empresa, c7, cont7, catSoporte, javier,
                "Ticket TK-4018 marcado como resuelto",
                "Ajuste en la configuración de webhooks de monitoreo de enlaces de red.",
                PrioridadTicket.MEDIA, EstadoTicket.RESUELTO,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(2).plusMinutes(20), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(2));

        crearEncuestaYRespuesta(empresa, c7, cont7, t5, 5, "Muy rápida la atención del equipo de Javier.");

        // Ticket 6: TK-4009 (InnovaSoft - Alta / Resuelto / CSAT 5)
        Ticket t6 = crearTicket("TK-4009", empresa, c5, cont5, catSoporte, carlos,
                "Encuesta CSAT recibida: 5/5",
                "Sincronización de contactos e historial de ventas completada sin interrupciones.",
                PrioridadTicket.ALTA, EstadoTicket.RESUELTO,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(3).plusMinutes(10), LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(3));

        crearEncuestaYRespuesta(empresa, c5, cont5, t6, 5, "Excelente atención de Carlos.");

        // Ticket 7: TK-4012 (FinTech Solutions - Baja / Resuelto / CSAT 4)
        Ticket t7 = crearTicket("TK-4012", empresa, c6, cont6, catFeature, maria,
                "Consulta sobre configuración de roles personalizados",
                "Deseamos restringir el acceso al módulo de conciliación para el perfil Operador.",
                PrioridadTicket.BAJA, EstadoTicket.RESUELTO,
                LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(4).plusMinutes(30), LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(4));

        crearEncuestaYRespuesta(empresa, c6, cont6, t7, 4, "Buena guía y documentación provista.");

        // Ticket 8: TK-4005 (Global Logistics - Media / Resuelto / CSAT 4)
        Ticket t8 = crearTicket("TK-4005", empresa, c2, cont2, catFacturacion, ana,
                "Actualización de razón social en comprobantes fiscales",
                "Se solicita actualizar la dirección fiscal en la plantilla de facturas emitidas.",
                PrioridadTicket.MEDIA, EstadoTicket.RESUELTO,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(5).plusMinutes(40), LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(5));

        crearEncuestaYRespuesta(empresa, c2, cont2, t8, 4, "Atención oportuna.");

        // Ticket 9: TK-4032 (TechCorp - Crítico / Pendiente)
        Ticket t9 = crearTicket("TK-4032", empresa, c1, cont1, catCritica, ana,
                "Falla intermitente en sincronización de stock con almacén",
                "Se detectan descalces de inventario en el turno noche.",
                PrioridadTicket.CRITICA, EstadoTicket.PENDIENTE,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(6),
                LocalDateTime.now().minusMinutes(45), null, LocalDateTime.now().minusHours(1));

        // Ticket 10: TK-4035 (Banco Metropolitano - Alta / Abierto)
        Ticket t10 = crearTicket("TK-4035", empresa, c9, cont9, catSoporte, javier,
                "Latencia en consultas de base de datos bancaria",
                "Tiempos de respuesta superiores a 2.5 segundos en módulo de transferencias.",
                PrioridadTicket.ALTA, EstadoTicket.ABIERTO,
                LocalDateTime.now().plusMinutes(50), LocalDateTime.now().plusHours(5),
                null, null, LocalDateTime.now().minusMinutes(20));

        // Ticket 11: TK-4038 (Minera Los Andes - Baja / Abierto)
        Ticket t11 = crearTicket("TK-4038", empresa, c10, cont10, catFeature, laura,
                "Habilitación de tablero de control de sensores IoT",
                "Solicitud para integrar telemetría de camiones autónomos en el módulo Analytics.",
                PrioridadTicket.BAJA, EstadoTicket.ABIERTO,
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusDays(2),
                null, null, LocalDateTime.now().minusHours(4));

        // ==========================================
        // 9. RENOVACIONES (Exactas al Dashboard)
        // ==========================================

        // 1. TechCorp Industries - Menor a 30 días (12 días restantes, En negociación, Resp: Ana Silva)
        Renovacion r1 = crearRenovacion(empresa, c1, cs1, ana, LocalDate.now().plusDays(12), 30,
                EstadoRenovacion.EN_NEGOCIACION, "Negociación en curso: Se presentó propuesta con 10% de descuento y SLA 24/7.");
        crearSeguimientoRenovacion(r1, ana, EstadoRenovacion.EN_NEGOCIACION, "Renovación actualizada a 'En negociación'. Cliente revisando addendum con CTO.");

        // 2. Banco Metropolitano - Menor a 30 días (12 días restantes, En negociación, Resp: Laura Mendoza)
        Renovacion r2 = crearRenovacion(empresa, c9, cs9, laura, LocalDate.now().plusDays(12), 30,
                EstadoRenovacion.EN_NEGOCIACION, "Cuenta en riesgo: reunión urgente agendada con Gerencia de TI.");
        crearSeguimientoRenovacion(r2, laura, EstadoRenovacion.EN_NEGOCIACION, "Reunión de alineamiento de soporte prioritario sostenida con Fernando Castro.");

        // 3. Retail Express Perú - Menor a 30 días (24 días restantes, Pendiente, Resp: Carlos Ruiz)
        crearRenovacion(empresa, c8, cs8, carlos, LocalDate.now().plusDays(24), 30,
                EstadoRenovacion.PENDIENTE, "Enviar propuesta de renovación de Helpdesk antes de fin de mes.");

        // 4. Global Logistics SA - Menor a 30 días (25 días restantes, Pendiente, Resp: Carlos Ruiz)
        crearRenovacion(empresa, c2, cs2, carlos, LocalDate.now().plusDays(25), 30,
                EstadoRenovacion.PENDIENTE, "Contrato de Soporte Premium 24/7 listo para formalización.");

        // 5. Grupo Financiero Sur - 31 a 90 días (32 días restantes, Pendiente, Resp: María López)
        crearRenovacion(empresa, c3, cs3, maria, LocalDate.now().plusDays(32), 60,
                EstadoRenovacion.PENDIENTE, "Evaluando expansión de licencias para el Módulo Analytics.");

        // 6. NovaTech Industries - 31 a 90 días (45 días restantes, Pendiente, Resp: Carlos Ruiz)
        crearRenovacion(empresa, c4, cs4, carlos, LocalDate.now().plusDays(45), 60,
                EstadoRenovacion.PENDIENTE, "Coordinar demo de nuevas funciones del ERP.");

        // 7. Minera Los Andes - Mayor a 90 días (180 días restantes, Pendiente, Resp: Ana Silva)
        crearRenovacion(empresa, c10, cs10, ana, LocalDate.now().plusMonths(6), 60,
                EstadoRenovacion.PENDIENTE, "Contrato vigente de Analítica Avanzada & IA.");

        // 8. FinTech Solutions - Mayor a 90 días (210 días restantes, Pendiente, Resp: Juan Pérez)
        crearRenovacion(empresa, c6, cs6, admin, LocalDate.now().plusMonths(7), 60,
                EstadoRenovacion.PENDIENTE, "Cliente nuevo en onboarding; renovación programada para el próximo año.");

        // 9. InnovaSoft - Completada (Renovado hace 5 días, Resp: Laura Mendoza)
        Renovacion r9 = crearRenovacion(empresa, c5, cs5, laura, LocalDate.now().plusMonths(12), 30,
                EstadoRenovacion.RENOVADO, "Renovado por 12 meses adicionales con expansión de 30 puestos.");
        r9.setFechaRenovacion(LocalDate.now().minusDays(5));
        renovacionRepository.save(r9);
        crearSeguimientoRenovacion(r9, laura, EstadoRenovacion.RENOVADO, "Firma de contrato de renovación por 1 año completada con Sofía Benítez.");

        // 10. CloudNet Corp - Completada (Renovado hace 15 días, Resp: Javier Ramos)
        Renovacion r10 = crearRenovacion(empresa, c7, cs7, javier, LocalDate.now().plusMonths(12), 30,
                EstadoRenovacion.RENOVADO, "Renovado satisfactoriamente con SLA Diamante.");
        r10.setFechaRenovacion(LocalDate.now().minusDays(15));
        renovacionRepository.save(r10);
        crearSeguimientoRenovacion(r10, javier, EstadoRenovacion.RENOVADO, "Contrato renovado con SLA extendido.");

        // ==========================================
        // 10. AUDITORÍA Y ACTIVIDAD RECIENTE
        // ==========================================
        crearAuditoria(empresa, javier, AccionAuditoria.INSERT, "TICKETS", t2.getId(), "Nuevo ticket crítico registrado por NovaTech Industries", "127.0.0.1", LocalDateTime.now().minusMinutes(28));
        crearAuditoria(empresa, carlos, AccionAuditoria.INSERT, "ENCUESTAS_CSAT", t6.getId(), "Encuesta CSAT recibida: 5/5 de InnovaSoft", "127.0.0.1", LocalDateTime.now().minusHours(2).minusMinutes(15));
        crearAuditoria(empresa, admin, AccionAuditoria.INSERT, "CLIENTES", c6.getId(), "Nuevo cliente registrado: FinTech Solutions", "127.0.0.1", LocalDateTime.now().minusHours(3).minusMinutes(50));
        crearAuditoria(empresa, ana, AccionAuditoria.UPDATE, "RENOVACIONES", r1.getId(), "Renovación actualizada a 'En negociación' para TechCorp Industries", "127.0.0.1", LocalDateTime.now().minusDays(1).plusHours(4));
        crearAuditoria(empresa, javier, AccionAuditoria.UPDATE, "TICKETS", t5.getId(), "Ticket TK-4018 marcado como resuelto para CloudNet Corp", "127.0.0.1", LocalDateTime.now().minusDays(1).plusHours(2));

        // 11. Notificaciones
        notificacionRepository.save(Notificacion.builder()
                .empresa(empresa)
                .usuario(admin)
                .tipo(TipoNotificacion.CLIENTE_EN_RIESGO)
                .titulo("Alerta: TechCorp Industries en Riesgo")
                .mensaje("Health Score cayó a 45 debido a tickets críticos y próxima renovación.")
                .entidadTipo("CLIENTE")
                .entidadId(c1.getId())
                .leida(false)
                .build());

        notificacionRepository.save(Notificacion.builder()
                .empresa(empresa)
                .usuario(carlos)
                .tipo(TipoNotificacion.TICKET_ASIGNADO)
                .titulo("Nuevo Ticket Asignado: TK-4029")
                .mensaje("Se te ha asignado el ticket crítico 'Falla en módulo de Facturación SUNAT'.")
                .entidadTipo("TICKET")
                .entidadId(t1.getId())
                .leida(false)
                .build());

        log.info("[✓] Datos de prueba del Dashboard inicializados con éxito (10 clientes, 11 tickets, 10 renovaciones, CSAT y auditoría).");
    }

    private Cliente crearCliente(Empresa emp, Usuario resp, String nombreComercial, String razonSocial,
                                 String ruc, String industria, String tamano, String pais, String ciudad,
                                 String direccion, String web, String estado, int healthScore, EstadoSaludCliente estadoSalud) {
        Cliente c = clienteRepository.save(Cliente.builder()
                .empresa(emp)
                .responsable(resp)
                .nombreComercial(nombreComercial)
                .razonSocial(razonSocial)
                .ruc(ruc)
                .industria(industria)
                .tamanoEmpresa(tamano)
                .pais(pais)
                .ciudad(ciudad)
                .direccion(direccion)
                .sitioWeb(web)
                .estado(estado)
                .createdAt(LocalDateTime.now().minusMonths(6))
                .build());

        healthScoreHistorialRepository.save(HealthScoreHistorial.builder()
                .cliente(c)
                .puntaje(new BigDecimal(healthScore + ".00"))
                .estado(estadoSalud)
                .puntajeTickets(new BigDecimal((healthScore * 0.3) + ""))
                .puntajeCsat(new BigDecimal((healthScore * 0.3) + ""))
                .puntajeActividad(new BigDecimal((healthScore * 0.15) + ""))
                .puntajeRenovacion(new BigDecimal((healthScore * 0.25) + ""))
                .build());

        return c;
    }

    private Contacto crearContacto(Cliente cliente, String nombre, String apellido, String cargo, String email, String tel, boolean principal) {
        return contactoRepository.save(Contacto.builder()
                .cliente(cliente)
                .nombre(nombre)
                .apellido(apellido)
                .cargo(cargo)
                .email(email)
                .telefono(tel)
                .esContactoPrincipal(principal)
                .estado("ACTIVO")
                .build());
    }

    private ClienteServicio crearClienteServicio(Cliente cliente, Servicio servicio, LocalDate inicio, LocalDate fin) {
        return clienteServicioRepository.save(ClienteServicio.builder()
                .cliente(cliente)
                .servicio(servicio)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .estado(EstadoServicioCliente.ACTIVO)
                .build());
    }

    private Ticket crearTicket(String numero, Empresa emp, Cliente cli, Contacto con, CategoriaTicketEntity cat,
                               Usuario agente, String asunto, String descripcion, PrioridadTicket prioridad,
                               EstadoTicket estado, LocalDateTime slaResp, LocalDateTime slaRes,
                               LocalDateTime fResp, LocalDateTime fRes, LocalDateTime createdAt) {
        return ticketRepository.save(Ticket.builder()
                .numeroTicket(numero)
                .empresa(emp)
                .cliente(cli)
                .contacto(con)
                .categoriaEntity(cat)
                .agente(agente)
                .asunto(asunto)
                .descripcion(descripcion)
                .prioridad(prioridad)
                .estado(estado)
                .slaRespuestaLimite(slaResp)
                .slaResolucionLimite(slaRes)
                .fechaPrimeraRespuesta(fResp)
                .fechaResolucion(fRes)
                .createdAt(createdAt != null ? createdAt : LocalDateTime.now())
                .build());
    }

    private void crearMensaje(Ticket ticket, Usuario usuario, Contacto contacto, TipoMensajeTicket tipo, String mensaje, LocalDateTime fecha) {
        ticketMensajeRepository.save(TicketMensaje.builder()
                .ticket(ticket)
                .usuario(usuario)
                .contacto(contacto)
                .tipo(tipo)
                .mensaje(mensaje)
                .createdAt(fecha != null ? fecha : LocalDateTime.now())
                .build());
    }

    private void crearHistorial(Ticket ticket, Usuario usuario, String tipoEvento, String descripcion, LocalDateTime fecha) {
        ticketHistorialRepository.save(TicketHistorial.builder()
                .ticket(ticket)
                .usuario(usuario)
                .tipoEvento(tipoEvento)
                .descripcion(descripcion)
                .createdAt(fecha != null ? fecha : LocalDateTime.now())
                .build());
    }

    private void crearEncuestaYRespuesta(Empresa emp, Cliente cli, Contacto con, Ticket ticket, int puntuacion, String comentario) {
        EncuestaCsat enc = encuestaCsatRepository.save(EncuestaCsat.builder()
                .empresa(emp)
                .cliente(cli)
                .contacto(con)
                .ticket(ticket)
                .estado(EstadoEncuestaCsat.RESPONDIDA)
                .fechaEnvio(LocalDateTime.now().minusDays(1))
                .fechaRespuesta(LocalDateTime.now().minusHours(18))
                .build());

        respuestaCsatRepository.save(RespuestaCsat.builder()
                .encuesta(enc)
                .puntuacion(puntuacion)
                .comentario(comentario)
                .build());

        encuestaSatisfaccionRepository.save(EncuestaSatisfaccion.builder()
                .ticket(ticket)
                .cliente(cli)
                .contacto(con)
                .puntuacion(puntuacion)
                .comentario(comentario)
                .fechaCreacion(LocalDateTime.now().minusHours(18))
                .build());
    }

    private Renovacion crearRenovacion(Empresa emp, Cliente cli, ClienteServicio cs, Usuario resp, LocalDate vencimiento, int diasAlerta, EstadoRenovacion estado, String notas) {
        return renovacionRepository.save(Renovacion.builder()
                .empresa(emp)
                .cliente(cli)
                .clienteServicio(cs)
                .responsable(resp)
                .fechaVencimiento(vencimiento)
                .diasAlerta(diasAlerta)
                .estado(estado)
                .notas(notas)
                .build());
    }

    private void crearSeguimientoRenovacion(Renovacion ren, Usuario user, EstadoRenovacion estado, String comentario) {
        renovacionSeguimientoRepository.save(RenovacionSeguimiento.builder()
                .renovacion(ren)
                .usuario(user)
                .fecha(LocalDateTime.now().minusDays(1))
                .estado(estado)
                .comentario(comentario)
                .build());
    }

    private void crearAuditoria(Empresa emp, Usuario user, AccionAuditoria accion, String entidad, Long registroId, String descripcion, String ip, LocalDateTime fecha) {
        auditoriaRepository.save(Auditoria.builder()
                .empresa(emp)
                .usuario(user)
                .accion(accion)
                .entidad(entidad)
                .registroId(registroId)
                .direccionIp(ip)
                .createdAt(fecha != null ? fecha : LocalDateTime.now())
                .build());
    }
}
