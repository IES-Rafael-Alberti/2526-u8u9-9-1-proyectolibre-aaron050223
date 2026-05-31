package org.iesra.service


import org.iesra.dao.ReservaDAO
import org.iesra.dao.ResenaDAO
import org.iesra.model.Reserva
import org.iesra.model.Resena
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class PabellonService(private val reservaDAO: ReservaDAO, private val resenaDAO: ResenaDAO) {

    private val formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)

    /** Intenta crear una nueva reserva. Devuelve false si el turno ya esta ocupado. */
    fun hacerReserva(idPista: Int, fecha: String, turno: Int, usuario: String): Boolean {
        val reservasDelDia = reservaDAO.buscarPorPistaYFecha(idPista, fecha)
        val estaOcupada = reservasDelDia.any { it.turno == turno }

        if (estaOcupada) {
            return false
        }

        val nuevaReserva = Reserva(id = 0, idPista = idPista, fecha = fecha, turno = turno, usuario = usuario)
        reservaDAO.guardar(nuevaReserva)

        return true
    }

    /** Devuelve la lista de turnos ocupados para una pista y fecha. */
    fun obtenerTurnosOcupados(idPista: Int, fecha: String): List<Int> {
        val reservasDelDia = reservaDAO.buscarPorPistaYFecha(idPista, fecha)
        return reservasDelDia.map { it.turno }
    }

    /** Devuelve todas las reservas registradas. */
    fun obtenerTodasLasReservas(): List<Reserva> {
        return reservaDAO.obtenerTodas()
    }

    /** Devuelve reservas pasadas sin reseña (disponibles para reseñar). */
    fun obtenerReservasPasadasSinResena(hoy: LocalDate = LocalDate.now()): List<Reserva> {
        val idsConResena = resenaDAO.obtenerTodas().mapTo(mutableSetOf()) { it.reservaId }
        return obtenerReservasPasadas(hoy).filterNot { idsConResena.contains(it.id) }
    }

    /** Crea una reseña para una reserva pasada. Devuelve false si no cumple reglas. */
    fun crearResena(reservaId: Int, nota: Double, descripcion: String, hoy: LocalDate = LocalDate.now()): Boolean {
        val desc = descripcion.trim()
        if (nota < 1.0 || nota > 5.0) return false
        if (desc.length !in 1..100) return false

        val reserva = reservaDAO.obtenerPorId(reservaId) ?: return false
        val fechaReserva = LocalDate.parse(reserva.fecha, formatter)
        if (!fechaReserva.isBefore(hoy)) return false
        if (resenaDAO.obtenerPorReservaId(reservaId) != null) return false

        return try {
            resenaDAO.guardar(reservaId = reservaId, nota = nota, descripcion = desc)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Devuelve todas las reseñas. */
    fun obtenerResenas(): List<Resena> {
        return resenaDAO.obtenerTodas()
    }

    /** Elimina una reseña por reservaId. */
    fun eliminarResenaPorReservaId(reservaId: Int): Boolean {
        return resenaDAO.eliminarPorReservaId(reservaId)
    }

    /** Devuelve reservas con fecha >= hoy (visibles para el publico). */
    fun obtenerReservasFuturas(hoy: LocalDate = LocalDate.now()): List<Reserva> {
        return reservaDAO.obtenerTodas()
            .filter { LocalDate.parse(it.fecha, formatter) >= hoy }
            .sortedWith(compareBy<Reserva>({ LocalDate.parse(it.fecha, formatter) }, { it.idPista }, { it.turno }, { it.id }))
    }

    /** Devuelve reservas con fecha < hoy (historico, util para reseñas). */
    fun obtenerReservasPasadas(hoy: LocalDate = LocalDate.now()): List<Reserva> {
        return reservaDAO.obtenerTodas()
            .filter { LocalDate.parse(it.fecha, formatter).isBefore(hoy) }
            .sortedWith(compareBy<Reserva>({ LocalDate.parse(it.fecha, formatter) }, { it.idPista }, { it.turno }, { it.id }))
    }

    /** Elimina una reserva por id. Devuelve true si existia y se elimino. */
    fun eliminarReservaPorId(id: Int): Boolean {
        val existe = reservaDAO.obtenerPorId(id) != null
        if (!existe) return false
        reservaDAO.eliminarPorId(id)
        return true
    }

    /**
     * Elimina una reserva solo si su fecha es >= hoy.
     * Devuelve false si no existe o si pertenece al historico.
     */
    fun eliminarReservaFuturaPorId(id: Int, hoy: LocalDate = LocalDate.now()): Boolean {
        val reserva = reservaDAO.obtenerPorId(id) ?: return false
        val fechaReserva = LocalDate.parse(reserva.fecha, formatter)
        if (fechaReserva.isBefore(hoy)) return false
        reservaDAO.eliminarPorId(id)
        return true
    }

    /** Devuelve cuantas reservas pertenecen al historico (fecha anterior a hoy). No borra nada. */
    fun limpiarReservasAntiguas(hoy: LocalDate = LocalDate.now()): Int {
        return reservaDAO.obtenerTodas().count { LocalDate.parse(it.fecha, formatter).isBefore(hoy) }
    }

}
