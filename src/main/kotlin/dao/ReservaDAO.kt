package org.iesra.dao

import org.iesra.model.Reserva

/**
 * Contrato DAO para acceder a reservas.
 *
 * Permite desacoplar la logica de negocio (`PabellonService`) de la
 * tecnologia de persistencia (H2, memoria, ...).
 */
interface ReservaDAO {
    /** Inserta una nueva reserva. */
    fun guardar(reserva: Reserva)

    /**
     * Devuelve las reservas existentes para una pista y fecha concretas.
     * Se usa para detectar turnos ocupados al crear una reserva.
     */
    fun buscarPorPistaYFecha(idPista: Int, fecha: String): List<Reserva>

    /** Devuelve todas las reservas registradas. */
    fun obtenerTodas(): List<Reserva>

    /** Devuelve la reserva con el id indicado, o `null` si no existe. */
    fun obtenerPorId(id: Int): Reserva?

    /** Elimina la reserva con el id indicado. */
    fun eliminarPorId(id: Int)
}
