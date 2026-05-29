package org.iesra.dao.memory

import org.iesra.dao.ReservaDAO
import org.iesra.model.Reserva

class ReservaDAOMemory : ReservaDAO {
    private val reservas = mutableListOf<Reserva>()
    private var generadorId = 1

    override fun guardar(reserva: Reserva) {
        val reservaConId = reserva.copy(id = generadorId++)
        reservas.add(reservaConId)
    }

    override fun buscarPorPistaYFecha(idPista: Int, fecha: String): List<Reserva> {
        return reservas.filter { it.idPista == idPista && it.fecha == fecha }
    }

    override fun obtenerTodas(): List<Reserva> {
        return reservas.toList()
    }

    override fun eliminarPorId(id: Int) {
        reservas.removeIf { it.id == id }
    }
}
