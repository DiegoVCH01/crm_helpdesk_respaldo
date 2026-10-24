package com.serviclient.crm.controller;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dto.DashboardDto;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.service.IDashboardService;
import com.serviclient.crm.service.IEmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador MVC para el Dashboard general interactivo de ServiClient.
 *
 * <p>Expone el panel de control con métricas agregadas de clientes, salud operativa,
 * mesa de ayuda (tickets y SLA), satisfacción CSAT, renovaciones y flujo de actividad.</p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.IDashboardService
 * @see com.serviclient.crm.dto.DashboardDto
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Panel principal interactivo con KPIs de clientes, tickets, SLA, CSAT y renovaciones")
public class DashboardController {

    private final IDashboardService dashboardService;
    private final IEmpresaService empresaService;

    /**
     * Renderiza la vista principal del Dashboard con datos actualizados en tiempo real.
     *
     * @param periodo filtro de rango temporal ("7d", "30d", "90d", "1y", "todo")
     * @param tabRenovacion pestaña activa para el panel de renovaciones
     * @param authentication usuario autenticado en la sesión
     * @param model modelo de atributos para Thymeleaf
     * @return nombre de la plantilla Thymeleaf {@code "dashboard/index"}
     */
    @Operation(summary = "Ver Dashboard principal", description = "Muestra el resumen general ejecutivo con KPIs, métricas de salud, tickets y renovaciones")
    @ApiResponse(responseCode = "200", description = "Vista del Dashboard renderizada con éxito")
    @GetMapping
    public String index(@Parameter(description = "Rango de tiempo para métricas") 
                        @RequestParam(value = "periodo", defaultValue = "30d") String periodo,
                        @Parameter(description = "Pestaña activa en tabla de renovaciones") 
                        @RequestParam(value = "tabRenovacion", defaultValue = "MENOR_30") String tabRenovacion,
                        Authentication authentication,
                        Model model) {

        Long empresaId = 1L;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails && userDetails.getEmpresaId() != null) {
            empresaId = userDetails.getEmpresaId();
        } else {
            Empresa primera = empresaService.obtenerPrimera();
            empresaId = (primera != null) ? primera.getId() : 1L;
        }
        DashboardDto dashboard = dashboardService.obtenerDashboard(empresaId, periodo, tabRenovacion);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("periodo", periodo);
        model.addAttribute("tabRenovacion", tabRenovacion);

        return "dashboard/index";
    }
}
