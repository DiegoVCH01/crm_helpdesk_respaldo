package com.serviclient.crm.repository;

import com.serviclient.crm.dao.*;
import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("dev")
class JpaRepositoriesTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EncuestaSatisfaccionRepository encuestaRepository;

    @Autowired
    private RenovacionRepository renovacionRepository;

    private Empresa empresa;
    private RolEntity rolAdmin;
    private Usuario admin;
    private Cliente cliente;
    private Servicio servicio;
    private ClienteServicio clienteServicio;
    private CategoriaTicketEntity catInfra;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .nombreComercial("Test Corp")
                .razonSocial("Test Corp S.A.C.")
                .ruc("20123456789")
                .industria("Tecnología")
                .pais("Perú")
                .ciudad("Lima")
                .estado("ACTIVA")
                .build();
        empresa = entityManager.persistAndFlush(empresa);

        rolAdmin = RolEntity.builder()
                .empresa(empresa)
                .nombre("Administrador")
                .descripcion("Admin rol")
                .estado("ACTIVO")
                .build();
        rolAdmin = entityManager.persistAndFlush(rolAdmin);

        admin = Usuario.builder()
                .empresa(empresa)
                .rolEntity(rolAdmin)
                .nombre("Admin")
                .apellido("Test")
                .email("admin.test@testcorp.com")
                .password("$2a$10$abcdefghijklmnopqrstuv")
                .estado("ACTIVO")
                .build();
        admin = entityManager.persistAndFlush(admin);

        cliente = Cliente.builder()
                .empresa(empresa)
                .nombreComercial("Cliente Alpha")
                .razonSocial("Alpha S.A.")
                .ruc("20987654321")
                .responsable(admin)
                .estado("ACTIVO")
                .build();
        cliente = entityManager.persistAndFlush(cliente);

        servicio = Servicio.builder()
                .empresa(empresa)
                .nombre("SaaS Premium")
                .codigo("SRV-PREM")
                .estado("ACTIVO")
                .build();
        servicio = entityManager.persistAndFlush(servicio);

        clienteServicio = ClienteServicio.builder()
                .cliente(cliente)
                .servicio(servicio)
                .fechaInicio(LocalDate.now().minusMonths(3))
                .fechaFin(LocalDate.now().plusMonths(9))
                .estado(EstadoServicioCliente.ACTIVO)
                .build();
        clienteServicio = entityManager.persistAndFlush(clienteServicio);

        catInfra = CategoriaTicketEntity.builder()
                .empresa(empresa)
                .nombre("Infraestructura")
                .descripcion("Infra")
                .estado("ACTIVO")
                .build();
        catInfra = entityManager.persistAndFlush(catInfra);
    }

    @Test
    @DisplayName("Debe persistir y buscar empresa por ID y usuario por email")
    void testEmpresaYUsuarioRepository() {
        Optional<Empresa> empresaEncontrada = empresaRepository.findById(empresa.getId());
        assertTrue(empresaEncontrada.isPresent());
        assertEquals("Test Corp", empresaEncontrada.get().getNombreComercial());

        Optional<Usuario> usuarioEncontrado = usuarioDAO.findByEmail("admin.test@testcorp.com");
        assertTrue(usuarioEncontrado.isPresent());
        assertEquals(Rol.ADMIN, usuarioEncontrado.get().getRol());
        assertEquals(empresa.getId(), usuarioEncontrado.get().getEmpresa().getId());
    }

    @Test
    @DisplayName("Debe buscar clientes por empresa y aplicar filtros de búsqueda")
    void testClienteRepositoryFiltros() {
        Page<Cliente> resultado = clienteRepository.buscarConFiltros(
                empresa.getId(),
                "Alpha",
                PageRequest.of(0, 10)
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Cliente Alpha", resultado.getContent().get(0).getNombreComercial());

        long total = clienteRepository.countByEmpresaId(empresa.getId());
        assertEquals(1, total);
    }

    @Test
    @DisplayName("Debe registrar y consultar tickets con SLA y filtros por estado y prioridad")
    void testTicketRepository() {
        Ticket ticket = Ticket.builder()
                .numeroTicket("TK-9999")
                .empresa(empresa)
                .cliente(cliente)
                .contacto(null)
                .categoriaEntity(catInfra)
                .agente(admin)
                .asunto("Incidencia crítica de servidor")
                .descripcion("El servidor no responde peticiones")
                .prioridad(PrioridadTicket.CRITICA)
                .estado(EstadoTicket.ABIERTO)
                .createdAt(LocalDateTime.now())
                .slaRespuestaLimite(LocalDateTime.now().plusHours(2))
                .slaResolucionLimite(LocalDateTime.now().plusHours(24))
                .build();
        entityManager.persistAndFlush(ticket);

        long criticosAbiertos = ticketRepository.countByClienteIdAndPrioridadAndEstadoNot(
                cliente.getId(),
                PrioridadTicket.CRITICA,
                EstadoTicket.CERRADO
        );
        assertEquals(1, criticosAbiertos);

        long countPorEstado = ticketRepository.countByEmpresaIdAndEstado(empresa.getId(), EstadoTicket.ABIERTO);
        assertEquals(1, countPorEstado);
    }

    @Test
    @DisplayName("Debe calcular promedio de CSAT correctamente por cliente")
    void testEncuestaSatisfaccionPromedio() {
        Ticket ticket1 = Ticket.builder()
                .numeroTicket("TK-1001")
                .empresa(empresa)
                .cliente(cliente)
                .categoriaEntity(catInfra)
                .asunto("Consulta 1")
                .descripcion("Detalle")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.CERRADO)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        ticket1 = entityManager.persistAndFlush(ticket1);

        EncuestaSatisfaccion encuesta1 = EncuestaSatisfaccion.builder()
                .ticket(ticket1)
                .cliente(cliente)
                .puntuacion(5)
                .comentario("Excelente atención")
                .fechaCreacion(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(encuesta1);

        Double promedio = encuestaRepository.obtenerPromedioCsatPorCliente(cliente.getId());
        assertNotNull(promedio);
        assertEquals(5.0, promedio);
    }

    @Test
    @DisplayName("Debe listar renovaciones ordenadas por fecha de vencimiento")
    void testRenovacionRepository() {
        Renovacion renovacion = Renovacion.builder()
                .empresa(empresa)
                .cliente(cliente)
                .clienteServicio(clienteServicio)
                .responsable(admin)
                .fechaVencimiento(LocalDate.now().plusDays(20))
                .estado(EstadoRenovacion.EN_NEGOCIACION)
                .build();
        entityManager.persistAndFlush(renovacion);

        List<Renovacion> lista = renovacionRepository.findByEmpresaIdOrderByFechaVencimientoAsc(empresa.getId());
        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        assertEquals(EstadoRenovacion.EN_NEGOCIACION, lista.get(0).getEstado());
    }
}
