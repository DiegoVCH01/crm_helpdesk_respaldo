package com.serviclient.crm.entity.enums;

/**
 * Categorías algorítmicas de clasificación del Health Score de un cliente (0 a 100).
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
public enum EstadoSaludCliente {
    /** Puntaje entre 80 y 100 puntos */
    SALUDABLE("Saludable"),
    /** Puntaje entre 50 y 79 puntos */
    EN_OBSERVACION("En Observación"),
    /** Puntaje entre 0 y 49 puntos */
    EN_RIESGO("En Riesgo");

    private final String descripcion;

    EstadoSaludCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Retorna la descripción textual de la categoría de salud.
     *
     * @return descripción del estado de salud
     */
    public String getDescripcion() {
        return descripcion;
    }
}
