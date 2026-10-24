package com.serviclient.crm.service;

import com.serviclient.crm.dto.DashboardDto;

public interface IDashboardService {

    DashboardDto obtenerDashboard(Long empresaId, String periodo, String tabRenovacion);
}
