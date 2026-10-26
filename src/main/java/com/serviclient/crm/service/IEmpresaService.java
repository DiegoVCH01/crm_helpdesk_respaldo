package com.serviclient.crm.service;

import com.serviclient.crm.dto.RegistroEmpresaDto;
import com.serviclient.crm.entity.Empresa;

/**
 * Contrato de servicio para el registro, onboarding y consulta de empresas proveedoras.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.EmpresaService
 */
public interface IEmpresaService {

    /**
     * Persiste de forma atómica y completa una nueva empresa junto a su administrador y configuración SLA/CSAT/Health.
     *
     * @param dto datos recopilados durante el Wizard de registro en 3 pasos
     * @return entidad {@link Empresa} registrada
     * @throws IllegalArgumentException si los datos requeridos no cumplen con las precondiciones de validación
     */
    Empresa registrarEmpresaCompleta(RegistroEmpresaDto dto);

    /**
     * Obtiene una empresa por su identificador primario.
     *
     * @param id identificador de la empresa
     * @return entidad {@link Empresa}
     * @throws org.springframework.web.server.ResponseStatusException si la empresa no existe
     */
    Empresa obtenerPorId(Long id);

    /**
     * Retorna la primera empresa registrada en el sistema para inicialización o fallback de contexto.
     *
     * @return entidad {@link Empresa} o {@code null} si aún no hay registros
     */
    Empresa obtenerPrimera();
}
