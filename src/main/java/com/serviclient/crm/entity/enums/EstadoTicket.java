package com.serviclient.crm.entity.enums;

/**
 * Estados del flujo de vida de un ticket de soporte técnico.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
public enum EstadoTicket {
    /** Ticket recién creado pendiente de atención inicial */
    ABIERTO("Abierto", "badge-status-open"),
    /** Ticket en diagnóstico o tratamiento activo por un agente */
    EN_PROCESO("En proceso", "badge-status-in-progress"),
    /** Ticket en espera de información del cliente o un tercero */
    PENDIENTE("Pendiente", "badge-status-pending"),
    /** Solución provista al cliente pendiente de confirmación */
    RESUELTO("Resuelto", "badge-status-resolved"),
    /** Caso finalizado satisfactoriamente o archivado */
    CERRADO("Cerrado", "badge-status-closed");

    private final String etiqueta;
    private final String cssClass;

    EstadoTicket(String etiqueta, String cssClass) {
        this.etiqueta = etiqueta;
        this.cssClass = cssClass;
    }

    /**
     * Retorna la etiqueta en español para visualización en pantalla.
     *
     * @return nombre descriptivo del estado
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Retorna la clase CSS de estilo para el badge visual.
     *
     * @return clase CSS
     */
    public String getCssClass() {
        return cssClass;
    }
}
