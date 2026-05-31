package org.iesra.dao

import org.iesra.model.Resena

interface ResenaDAO {
    fun guardar(reservaId: Int, nota: Double, descripcion: String): Resena
    fun obtenerTodas(): List<Resena>
    fun obtenerPorReservaId(reservaId: Int): Resena?
    fun eliminarPorReservaId(reservaId: Int): Boolean
}
