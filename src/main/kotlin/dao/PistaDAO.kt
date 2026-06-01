package org.iesra.dao

import org.iesra.model.Pista

interface PistaDAO {
    fun obtenerTodas(): List<Pista>
    fun obtenerPorId(id: Int): Pista?
}
