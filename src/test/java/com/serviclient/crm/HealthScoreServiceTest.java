package com.serviclient.crm;

import com.serviclient.crm.dao.ClienteDAO;
import com.serviclient.crm.dao.TicketDAO;
import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.enums.EstadoCliente;
import com.serviclient.crm.entity.enums.EstadoTicket;
import com.serviclient.crm.entity.enums.PrioridadTicket;
import com.serviclient.crm.repository.EncuestaSatisfaccionRepository;
import com.serviclient.crm.repository.HealthScoreHistorialRepository;
import com.serviclient.crm.service.HealthScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthScoreServiceTest {

    @Mock
    private TicketDAO ticketDAO;

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private EncuestaSatisfaccionRepository encuestaRepository;

    @Mock
    private HealthScoreHistorialRepository healthScoreHistorialRepository;

    @InjectMocks
    private HealthScoreService healthScoreService;

    private Empresa empresa;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nombreComercial("ServiClient")
                .factorTicketsCriticos(true)
                .factorCsat(true)
                .factorRenovacionProxima(true)
                .build();

        cliente = Cliente.builder()
                .id(10L)
                .empresa(empresa)
                .nombreComercial("NovaTech Solutions")
                .build();
        cliente.setFechaInicio(LocalDate.now().minusMonths(6));
        cliente.setFechaRenovacion(LocalDate.now().plusDays(15));
        cliente.setHealthScore(80);
    }

    @Test
    void testRecalcularHealthScoreEnRiesgo() {
        // Mock: 3 tickets abiertos, 2 de ellos críticos
        when(ticketDAO.countByClienteIdAndEstadoNot(eq(10L), eq(EstadoTicket.CERRADO)))
                .thenReturn(3L);
        when(ticketDAO.countByClienteIdAndPrioridadAndEstadoNot(eq(10L), eq(PrioridadTicket.CRITICA), eq(EstadoTicket.CERRADO)))
                .thenReturn(2L);
        // Mock: CSAT bajo de 2.5
        when(encuestaRepository.obtenerPromedioCsatPorCliente(10L))
                .thenReturn(2.5);

        int score = healthScoreService.recalcularYActualizarHealthScore(cliente);

        // 100 - (3 * 5 = 15) - (2 * 15 = 30) - 20 (CSAT < 3.0) = 35
        assertEquals(35, score);
        assertEquals(EstadoCliente.EN_RIESGO, cliente.getEstadoSalud());
    }

    @Test
    void testFactoresDeRiesgoDetectados() {
        when(ticketDAO.countByClienteIdAndPrioridadAndEstadoNot(eq(10L), eq(PrioridadTicket.CRITICA), eq(EstadoTicket.CERRADO)))
                .thenReturn(1L);

        List<HealthScoreService.FactorSalud> factores = healthScoreService.obtenerFactoresRiesgo(cliente);

        assertFalse(factores.isEmpty());
        assertTrue(factores.stream().anyMatch(f -> f.getTitulo().equals("Tickets Críticos Abiertos")));
    }
}
