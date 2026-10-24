package com.serviclient.crm.service;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.service.HealthScoreService.FactorSalud;
import java.util.List;

public interface IHealthScoreService {

    int recalcularYActualizarHealthScore(Cliente cliente);

    List<FactorSalud> obtenerFactoresSalud(Cliente cliente);

    default List<FactorSalud> obtenerFactoresRiesgo(Cliente cliente) {
        return obtenerFactoresSalud(cliente);
    }
}
