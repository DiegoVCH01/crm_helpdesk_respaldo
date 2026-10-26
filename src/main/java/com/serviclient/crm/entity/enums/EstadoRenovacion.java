package com.serviclient.crm.entity.enums;

/**
 * Estados del proceso de negociación y retención en renovaciones de contratos.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
public enum EstadoRenovacion {
    /** Contrato próximo a vencer en espera de inicio de contacto */
    PENDIENTE("Pendiente", "badge-renovacion-pendiente"),
    /** Conversaciones comerciales o ajustes de propuesta en curso */
    EN_NEGOCIACION("En negociación", "badge-renovacion-negociacion"),
    /** Contrato exitosamente extendido o firmado por un nuevo período */
    RENOVADO("Renovado", "badge-renovacion-renovado"),
    /** Cliente decidió no continuar el servicio */
    NO_RENOVADO("No renovado", "badge-renovacion-norealizada"),
    /** Gestión expirada o desistida sin renovación */
    NO_REALIZADO("No realizada", "badge-renovacion-norealizada");

    private final String etiqueta;
    private final String cssClass;

    EstadoRenovacion(String etiqueta, String cssClass) {
        this.etiqueta = etiqueta;
        this.cssClass = cssClass;
    }

    /**
     * Retorna la etiqueta descriptiva del estado.
     *
     * @return nombre del estado
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Retorna la clase CSS para el badge de renovación.
     *
     * @return clase CSS
     */
    public String getCssClass() {
        return cssClass;
    }
}
