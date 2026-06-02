package org.iesra.dao

import org.iesra.model.Pista

/**
 * Contrato DAO para acceder al catalogo de pistas (diccionario `id -> deporte`).
 */
interface PistaDAO {
    /** Devuelve todas las pistas disponibles, ordenadas por `id`. */
    fun obtenerTodas(): List<Pista>

    /** Devuelve la pista con el id indicado, o `null` si no existe. */
    fun obtenerPorId(id: Int): Pista?
}
