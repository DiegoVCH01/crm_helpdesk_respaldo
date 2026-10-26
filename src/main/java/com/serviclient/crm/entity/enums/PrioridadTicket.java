package com.serviclient.crm.entity.enums;

/**
 * Niveles de severidad y prioridad asignados a los tickets para determinar sus tiempos SLA.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
public enum PrioridadTicket {
    /** Incidencia menor sin impacto operativo crítico */
    BAJA("Baja", "badge-priority-low"),
    /** Incidencia regular con impacto operativo moderado */
    MEDIA("Media", "badge-priority-medium"),
    /** Afectación importante de funcionalidades con degradación notable */
    ALTA("Alta", "badge-priority-high"),
    /** Interrupción total de servicio o contingencia urgente */
    CRITICA("Crítica", "badge-priority-critical");

    private final String etiqueta;
    private final String cssClass;

    PrioridadTicket(String etiqueta, String cssClass) {
        this.etiqueta = etiqueta;
        this.cssClass = cssClass;
    }

    /**
     * Retorna la etiqueta legible de la prioridad.
     *
     * @return texto descriptivo de la prioridad
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Retorna la clase de estilo CSS para el badge representativo.
     *
     * @return clase CSS
     */
    public String getCssClass() {
        return cssClass;
    }
}
