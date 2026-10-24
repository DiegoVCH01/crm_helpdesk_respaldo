package com.serviclient.crm.service;

import com.serviclient.crm.dto.RegistroEmpresaDto;
import com.serviclient.crm.entity.Empresa;

public interface IEmpresaService {

    Empresa registrarEmpresaCompleta(RegistroEmpresaDto dto);

    Empresa obtenerPorId(Long id);

    Empresa obtenerPrimera();
}
