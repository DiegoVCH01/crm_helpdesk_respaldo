package com.serviclient.crm.service;

import com.serviclient.crm.dto.DashboardDto;

/**
 * Contrato de servicio para la generación y consolidación de métricas analíticas del Dashboard ejecutivo.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.DashboardService
 */
public interface IDashboardService {

    /**
     * Construye y consolida el DTO con los indicadores del panel interactivo para el período y pestaña indicados.
     *
     * @param empresaId identificador de la empresa
     * @param periodo filtro temporal ("7d", "30d", "90d", "1y", "todo")
     * @param tabRenovacion pestaña activa en la tabla de vencimientos ("MENOR_30", "DE_30_A_90", etc.)
     * @return instancia de {@link DashboardDto} con KPIs, series para gráficos y alertas
     */
    DashboardDto obtenerDashboard(Long empresaId, String periodo, String tabRenovacion);
}
