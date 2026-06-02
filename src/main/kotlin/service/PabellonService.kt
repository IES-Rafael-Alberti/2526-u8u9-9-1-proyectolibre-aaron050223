package org.iesra.service


import org.iesra.dao.ReservaDAO
import org.iesra.dao.PistaDAO
import org.iesra.dao.ResenaDAO
import org.iesra.model.Pista
import org.iesra.model.Reserva
import org.iesra.model.Resena
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

/**
 * Capa de servicio: concentra las reglas de negocio del pabellon.
 *
 * Recibe los DAOs por inyeccion de dependencias (DIP) y se encarga
 * de:
 *  - evitar reservas duplicadas en el mismo turno.
 *  - separar reservas futuras y pasadas.
 *  - validar y crear reseñas solo para reservas pasadas.
 *
 * Las fechas se manejan como `String` en formato `dd-MM-uuuu`,
 * tal y como se guardan en H2.
 */
class PabellonService(
    private val reservaDAO: ReservaDAO,
    private val pistaDAO: PistaDAO,
    private val resenaDAO: ResenaDAO
) {

    private val formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)

    /** Devuelve la lista completa de pistas. */
    fun obtenerPistas(): List<Pista> {
        return pistaDAO.obtenerTodas()
    }

    /** Devuelve las pistas como `Map<id, deporte>` para traducciones rapidas. */
    fun obtenerMapaPistas(): Map<Int, String> {
        return pistaDAO.obtenerTodas().associate { it.id to it.deporte }
    }

    /**
     * Intenta crear una nueva reserva.
     *
     * @return `true` si se guardo, `false` si el turno ya estaba ocupado.
     */
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

    /** Devuelve todas las reservas registradas (sin filtrar por fecha). */
    fun obtenerTodasLasReservas(): List<Reserva> {
        return reservaDAO.obtenerTodas()
    }

    /**
     * Devuelve reservas pasadas que aun no tienen reseña
     * (candidatas para ser reseñadas).
     */
    fun obtenerReservasPasadasSinResena(hoy: LocalDate = LocalDate.now()): List<Reserva> {
        val idsConResena = resenaDAO.obtenerTodas().mapTo(mutableSetOf()) { it.reservaId }
        return obtenerReservasPasadas(hoy).filterNot { idsConResena.contains(it.id) }
    }

    /**
     * Crea una reseña para una reserva pasada aplicando las reglas:
     *  - `nota` en `1.0..5.0`.
     *  - `descripcion` (sin espacios al inicio/final) en `1..100`.
     *  - la reserva debe existir.
     *  - la reserva debe ser estrictamente pasada (`fecha < hoy`).
     *  - no debe existir ya una reseña para esa reserva.
     *
     * @return `true` si se guardo, `false` en caso contrario.
     */
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

    /**
     * Elimina una reseña por `reservaId`.
     * @return `true` si existia y se elimino, `false` en caso contrario.
     */
    fun eliminarResenaPorReservaId(reservaId: Int): Boolean {
        return resenaDAO.eliminarPorReservaId(reservaId)
    }

    /**
     * Devuelve reservas con fecha `>= hoy` (visibles para el publico),
     * ordenadas por fecha, pista y turno.
     */
    fun obtenerReservasFuturas(hoy: LocalDate = LocalDate.now()): List<Reserva> {
        return reservaDAO.obtenerTodas()
            .filter { LocalDate.parse(it.fecha, formatter) >= hoy }
            .sortedWith(compareBy<Reserva>({ LocalDate.parse(it.fecha, formatter) }, { it.idPista }, { it.turno }, { it.id }))
    }

    /**
     * Devuelve reservas con fecha `< hoy` (historico, usado para reseñas),
     * ordenadas por fecha, pista y turno.
     */
    fun obtenerReservasPasadas(hoy: LocalDate = LocalDate.now()): List<Reserva> {
        return reservaDAO.obtenerTodas()
            .filter { LocalDate.parse(it.fecha, formatter).isBefore(hoy) }
            .sortedWith(compareBy<Reserva>({ LocalDate.parse(it.fecha, formatter) }, { it.idPista }, { it.turno }, { it.id }))
    }

    /**
     * Elimina una reserva por id.
     * @return `true` si existia y se elimino, `false` en caso contrario.
     */
    fun eliminarReservaPorId(id: Int): Boolean {
        val existe = reservaDAO.obtenerPorId(id) != null
        if (!existe) return false
        reservaDAO.eliminarPorId(id)
        return true
    }

    /**
     * Elimina una reserva solo si su fecha es `>= hoy`.
     * @return `false` si no existe o si pertenece al historico.
     */
    fun eliminarReservaFuturaPorId(id: Int, hoy: LocalDate = LocalDate.now()): Boolean {
        val reserva = reservaDAO.obtenerPorId(id) ?: return false
        val fechaReserva = LocalDate.parse(reserva.fecha, formatter)
        if (fechaReserva.isBefore(hoy)) return false
        reservaDAO.eliminarPorId(id)
        return true
    }

    /**
     * Devuelve cuantas reservas pertenecen al historico (`fecha < hoy`).
     * Ya no borra nada: se conserva el historico para reseñas.
     */
    fun limpiarReservasAntiguas(hoy: LocalDate = LocalDate.now()): Int {
        return reservaDAO.obtenerTodas().count { LocalDate.parse(it.fecha, formatter).isBefore(hoy) }
    }

}
