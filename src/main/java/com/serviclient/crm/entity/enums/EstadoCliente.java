package com.serviclient.crm.entity.enums;

/**
 * Estado general de retención y salud de una cuenta cliente B2B.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
public enum EstadoCliente {
    /** Cliente con métricas de uso y satisfacción óptimas */
    SALUDABLE("Saludable", "bg-emerald-50 text-emerald-700 border-emerald-200"),
    /** Estado preliminar de atención preventiva */
    OBSERVACION("Observación", "bg-amber-50 text-amber-700 border-amber-200"),
    /** Cliente con señales de degradación en soporte o actividad */
    EN_OBSERVACION("En Observación", "bg-amber-50 text-amber-700 border-amber-200"),
    /** Cliente con alto riesgo de abandono o cancelación (churn) */
    EN_RIESGO("En riesgo", "bg-rose-50 text-rose-700 border-rose-200");

    private final String etiqueta;
    private final String estiloBadge;

    EstadoCliente(String etiqueta, String estiloBadge) {
        this.etiqueta = etiqueta;
        this.estiloBadge = estiloBadge;
    }

    /**
     * Retorna la etiqueta descriptiva en español.
     *
     * @return texto del estado
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Retorna las clases Tailwind CSS para el badge de estado.
     *
     * @return clases CSS
     */
    public String getEstiloBadge() {
        return estiloBadge;
    }
}
