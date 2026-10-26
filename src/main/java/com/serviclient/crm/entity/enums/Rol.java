package com.serviclient.crm.entity.enums;

/**
 * Roles de seguridad y acceso del sistema CRM Helpdesk.
 * <p>
 * Define los perfiles de usuario interno (ADMIN, AGENTE, SUPERVISOR) y del portal externo (CLIENTE).
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
public enum Rol {
    /** Administrador con acceso total a la configuración y gestión de la empresa */
    ADMIN("Administrador"),
    /** Agente operativo de atención de tickets y soporte técnico */
    AGENTE("Agente de Soporte"),
    /** Supervisor de retención, métricas y Customer Success */
    SUPERVISOR("Supervisor"),
    /** Contacto cliente con acceso al portal de autoservicio */
    CLIENTE("Cliente");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Retorna la descripción legible del rol.
     *
     * @return descripción en español
     */
    public String getDescripcion() {
        return descripcion;
    }
}
