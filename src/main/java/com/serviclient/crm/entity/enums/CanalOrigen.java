package com.serviclient.crm.entity.enums;

/**
 * Vías de comunicación o canales mediante los cuales un cliente reporta una incidencia o ticket.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
public enum CanalOrigen {
    /** Reportado mediante el portal web de autoservicio */
    PORTAL_WEB("Portal Web"),
    /** Ingresado vía correo electrónico */
    EMAIL("Correo Electrónico"),
    /** Reportado telefónicamente y registrado por un agente */
    TELEFONO("Teléfono"),
    /** Ingresado por canal de mensajería instantánea o chat */
    CHAT("Chat en Vivo");

    private final String etiqueta;

    CanalOrigen(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * Retorna la etiqueta descriptiva del canal en español.
     *
     * @return nombre del canal
     */
    public String getEtiqueta() {
        return etiqueta;
    }
}
