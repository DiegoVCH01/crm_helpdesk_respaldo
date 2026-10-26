package com.serviclient.crm.service;

import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.service.CuentaPortalService.CredencialesTemporales;

/**
 * Contrato de servicio para la gestión de cuentas y credenciales de acceso al portal de autoservicio de clientes.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.CuentaPortalService
 */
public interface ICuentaPortalService {

    /**
     * Habilita o crea una cuenta de portal para un contacto, generando una contraseña temporal aleatoria.
     *
     * @param contacto contacto a quien se asigna la cuenta
     * @return credenciales temporales generadas (email y contraseña)
     */
    CredencialesTemporales activarOCrearCuenta(Contacto contacto);

    /**
     * Desactiva el acceso de un contacto al portal de clientes.
     *
     * @param contactoId identificador del contacto
     */
    void desactivarCuenta(Long contactoId);

    /**
     * Actualiza la dirección de correo electrónico vinculada a la cuenta del portal si el contacto la modificó.
     *
     * @param contacto contacto con los datos actualizados
     */
    void actualizarEmailSiExiste(Contacto contacto);
}
