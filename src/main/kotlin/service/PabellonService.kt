package org.iesra.service


import org.iesra.dao.ReservaDAO
import org.iesra.model.Reserva
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class PabellonService(private val reservaDAO: ReservaDAO) {

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

    /** Elimina una reserva por id. Devuelve true si existia y se elimino. */
    fun eliminarReservaPorId(id: Int): Boolean {
        val existe = reservaDAO.obtenerTodas().any { it.id == id }
        if (!existe) return false
        reservaDAO.eliminarPorId(id)
        return true
    }

    /** Elimina reservas con fecha anterior a hoy y devuelve cuantas se han borrado. */
    fun limpiarReservasAntiguas(hoy: LocalDate = LocalDate.now()): Int {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)
        val expiradas = reservaDAO.obtenerTodas().filter {
            val fechaReserva = LocalDate.parse(it.fecha, formatter)
            fechaReserva.isBefore(hoy)
        }

        expiradas.forEach { reservaDAO.eliminarPorId(it.id) }
        return expiradas.size
    }

}
