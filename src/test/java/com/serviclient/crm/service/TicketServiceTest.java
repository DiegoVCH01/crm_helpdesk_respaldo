package com.serviclient.crm.service;

import com.serviclient.crm.dao.*;
import com.serviclient.crm.dto.TicketDto;
import com.serviclient.crm.entity.*;
import com.serviclient.crm.entity.enums.*;
import com.serviclient.crm.repository.ContactoRepository;
import com.serviclient.crm.repository.RespuestaCsatRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketDAO ticketDAO;

    @Mock
    private TicketMensajeDAO ticketMensajeDAO;

    @Mock
    private TicketHistorialDAO ticketHistorialDAO;

    @Mock
    private EncuestaSatisfaccionDAO encuestaDAO;

    @Mock
    private EncuestaCsatDAO encuestaCsatDAO;

    @Mock
    private RespuestaCsatDAO respuestaCsatDAO;

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private ContactoDAO contactoDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @Mock
    private CategoriaTicketDAO categoriaTicketDAO;

    @Mock
    private ConfiguracionSlaDAO configuracionSlaDAO;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private HealthScoreService healthScoreService;

    @InjectMocks
    private TicketService ticketService;

    private Empresa empresa;
    private RolEntity rolAgente;
    private Usuario agente;
    private Cliente cliente;
    private Ticket ticket;
    private CategoriaTicketEntity catSoftware;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nombreComercial("ServiClient")
                .build();

        rolAgente = RolEntity.builder()
                .id(2L)
                .nombre("Agente de Soporte")
                .build();

        agente = Usuario.builder()
                .id(2L)
                .empresa(empresa)
                .rolEntity(rolAgente)
                .nombre("Carlos")
                .apellido("Ruiz")
                .email("carlos@serviclient.com")
                .build();

        cliente = Cliente.builder()
                .id(10L)
                .empresa(empresa)
                .nombreComercial("NovaTech")
                .build();

        catSoftware = CategoriaTicketEntity.builder()
                .id(1L)
                .empresa(empresa)
                .nombre("Soporte Técnico")
                .build();

        ticket = Ticket.builder()
                .id(100L)
                .numeroTicket("TK-4029")
                .empresa(empresa)
                .cliente(cliente)
                .categoriaEntity(catSoftware)
                .asunto("Problema de acceso")
                .descripcion("No puede iniciar sesión")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .createdAt(LocalDateTime.now())
                .slaRespuestaLimite(LocalDateTime.now().plusHours(4))
                .slaResolucionLimite(LocalDateTime.now().plusHours(48))
                .build();
    }

    @Test
    @DisplayName("Debe crear un ticket calculando plazos de SLA y guardando el mensaje inicial")
    void testCrearTicket() {
        TicketDto dto = new TicketDto();
        dto.setClienteId(10L);
        dto.setAgenteId(2L);
        dto.setAsunto("Falla en pasarela");
        dto.setDescripcion("Error 500 al pagar");
        dto.setCategoria(CategoriaTicket.FACTURACION);
        dto.setPrioridad(PrioridadTicket.CRITICA);
        dto.setCanalOrigen(CanalOrigen.PORTAL_WEB);

        when(empresaService.obtenerPorId(1L)).thenReturn(empresa);
        when(clienteDAO.findById(10L)).thenReturn(Optional.of(cliente));
        when(usuarioDAO.findById(2L)).thenReturn(Optional.of(agente));
        when(categoriaTicketDAO.findByEmpresaIdAndNombre(eq(1L), anyString())).thenReturn(Optional.of(catSoftware));
        when(configuracionSlaDAO.findByEmpresaIdAndPrioridad(eq(1L), eq(PrioridadTicket.CRITICA))).thenReturn(Optional.empty());
        when(ticketDAO.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(101L);
            return t;
        });

        Ticket creado = ticketService.crearTicket(1L, dto, agente);

        assertNotNull(creado);
        assertNotNull(creado.getCodigo());
        assertTrue(creado.getCodigo().startsWith("TK-"));
        assertEquals(EstadoTicket.ABIERTO, creado.getEstado());
        assertNotNull(creado.getFechaLimitePrimeraRespuesta());
        assertNotNull(creado.getFechaLimiteResolucion());

        verify(ticketMensajeDAO, times(1)).save(any(TicketMensaje.class));
        verify(ticketHistorialDAO, times(1)).save(any(TicketHistorial.class));
        verify(healthScoreService, times(1)).recalcularYActualizarHealthScore(cliente);
    }

    @Test
    @DisplayName("Debe agregar mensaje de conversación y registrar primera respuesta si es agente")
    void testAgregarMensajeRespuesta() {
        when(ticketDAO.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketMensajeDAO.save(any(TicketMensaje.class))).thenAnswer(i -> i.getArgument(0));

        ticketService.enviarMensaje(100L, agente, "Estamos revisando su caso.", false);

        assertTrue(ticket.isPrimeraRespuestaCumplida());
        assertNotNull(ticket.getFechaPrimeraRespuesta());
        verify(ticketMensajeDAO, times(1)).save(any(TicketMensaje.class));
        verify(ticketHistorialDAO, times(1)).save(any(TicketHistorial.class));
    }

    @Test
    @DisplayName("Debe agregar nota interna sin alterar los tiempos de primera respuesta al cliente")
    void testAgregarNotaInterna() {
        when(ticketDAO.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketMensajeDAO.save(any(TicketMensaje.class))).thenAnswer(i -> i.getArgument(0));

        ticketService.enviarMensaje(100L, agente, "Nota confidencial del equipo técnico.", true);

        assertFalse(ticket.isPrimeraRespuestaCumplida());
        assertNull(ticket.getFechaPrimeraRespuesta());
        verify(ticketMensajeDAO, times(1)).save(any(TicketMensaje.class));
        verify(ticketHistorialDAO, never()).save(any(TicketHistorial.class));
    }

    @Test
    @DisplayName("Debe cerrar el ticket y generar encuesta de satisfacción cuando enviarCsat es true")
    void testCerrarTicketConCsat() {
        when(ticketDAO.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketDAO.save(any(Ticket.class))).thenReturn(ticket);
        when(encuestaCsatDAO.save(any(EncuestaCsat.class))).thenAnswer(i -> i.getArgument(0));

        ticketService.cerrarTicket(100L, true, "Caso solucionado", agente);

        assertEquals(EstadoTicket.CERRADO, ticket.getEstado());
        assertNotNull(ticket.getFechaResolucion());
        assertNotNull(ticket.getFechaCierre());

        verify(encuestaCsatDAO, times(1)).save(any(EncuestaCsat.class));
        verify(ticketHistorialDAO, times(1)).save(any(TicketHistorial.class));
        verify(healthScoreService, times(1)).recalcularYActualizarHealthScore(cliente);
    }

    @Test
    @DisplayName("Debe asignar agente a un ticket y registrar en auditoría")
    void testAsignarAgente() {
        when(ticketDAO.findById(100L)).thenReturn(Optional.of(ticket));
        when(usuarioDAO.findById(2L)).thenReturn(Optional.of(agente));
        when(ticketDAO.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.asignarAgente(100L, 2L, agente);

        assertEquals(agente, ticket.getAgente());
        verify(ticketHistorialDAO, times(1)).save(any(TicketHistorial.class));
    }
}
