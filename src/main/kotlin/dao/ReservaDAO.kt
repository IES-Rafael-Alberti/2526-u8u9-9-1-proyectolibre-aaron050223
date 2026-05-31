package org.iesra.dao

import org.iesra.model.Reserva

interface ReservaDAO {
    fun guardar(reserva: Reserva)
    fun buscarPorPistaYFecha(idPista: Int, fecha: String): List<Reserva>
    fun obtenerTodas(): List<Reserva>
    fun obtenerPorId(id: Int): Reserva?
    fun eliminarPorId(id: Int)
}
