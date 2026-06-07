package org.iesra.dao.memory

import org.iesra.dao.ReservaDAO
import org.iesra.model.Reserva

/**
 * Implementacion en memoria de [ReservaDAO].
 *
 * Util para tests automatizados (Kotest) y para evitar depender
 * de H2 en tiempo de test.
 */
class ReservaDAOMemory : ReservaDAO {
    private val reservas = mutableListOf<Reserva>()
    private var generadorId = 1

    /** Inserta una reserva asignandole un id incremental. */
    override fun guardar(reserva: Reserva) {
        val reservaConId = reserva.copy(id = generadorId++)
        reservas.add(reservaConId)
    }

    /** Devuelve las reservas de una pista y fecha concretas. */
    override fun buscarPorPistaYFecha(idPista: Int, fecha: String): List<Reserva> {
        return reservas.filter { it.idPista == idPista && it.fecha == fecha }
    }

    /** Devuelve una copia inmutable de todas las reservas. */
    override fun obtenerTodas(): List<Reserva> {
        return reservas.toList()
    }

    /** Devuelve la reserva con el id indicado, o `null` si no existe. */
    override fun obtenerPorId(id: Int): Reserva? {
        return reservas.firstOrNull { it.id == id }
    }

    /** Elimina la reserva con el id indicado. */
    override fun eliminarPorId(id: Int) {
        reservas.removeIf { it.id == id }
    }
}
