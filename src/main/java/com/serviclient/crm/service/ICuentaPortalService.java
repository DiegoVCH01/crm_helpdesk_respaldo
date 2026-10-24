package com.serviclient.crm.service;

import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.service.CuentaPortalService.CredencialesTemporales;

public interface ICuentaPortalService {

    CredencialesTemporales activarOCrearCuenta(Contacto contacto);

    void desactivarCuenta(Long contactoId);

    void actualizarEmailSiExiste(Contacto contacto);
}
