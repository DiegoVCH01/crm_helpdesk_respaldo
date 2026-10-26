package com.serviclient.crm.service;

import com.serviclient.crm.entity.Cliente;
import com.serviclient.crm.service.HealthScoreService.FactorSalud;
import java.util.List;

/**
 * Contrato de servicio para el motor algorítmico de cálculo y evaluación del Health Score del cliente.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.HealthScoreService
 */
public interface IHealthScoreService {

    /**
     * Evalúa las dimensiones operativas de tickets, SLA, CSAT y contratos del cliente, recalculando su puntaje (0-100).
     *
     * @param cliente cliente a evaluar
     * @return nuevo puntaje calculado y persistido en la base de datos
     */
    int recalcularYActualizarHealthScore(Cliente cliente);

    /**
     * Obtiene el desglose cuantitativo de factores de riesgo y bonificaciones que componen la calificación del cliente.
     *
     * @param cliente cliente a evaluar
     * @return lista de factores con descripción, puntaje y nivel de impacto
     */
    List<FactorSalud> obtenerFactoresSalud(Cliente cliente);

    /**
     * Alias de conveniencia para consultar los factores de riesgo de un cliente.
     *
     * @param cliente cliente evaluado
     * @return lista de factores de salud y riesgo
     */
    default List<FactorSalud> obtenerFactoresRiesgo(Cliente cliente) {
        return obtenerFactoresSalud(cliente);
    }
}
