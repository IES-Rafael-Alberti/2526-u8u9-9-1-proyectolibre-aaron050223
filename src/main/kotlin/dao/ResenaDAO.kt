package org.iesra.dao

import org.iesra.model.Resena

/**
 * Contrato DAO para acceder a reseñas.
 *
 * En la implementacion real (MongoDB) se garantiza que solo exista
 * una reseña por reserva mediante un indice unico sobre `reservaId`.
 */
interface ResenaDAO {
    /**
     * Inserta una reseña.
     * @return la reseña creada con su `id` de MongoDB.
     */
    fun guardar(reservaId: Int, nota: Double, descripcion: String): Resena

    /** Devuelve todas las reseñas existentes. */
    fun obtenerTodas(): List<Resena>

    /** Devuelve la reseña asociada a la reserva indicada, o `null` si no existe. */
    fun obtenerPorReservaId(reservaId: Int): Resena?

    /**
     * Elimina la reseña asociada a la reserva indicada.
     * @return `true` si se elimino, `false` si no existia.
     */
    fun eliminarPorReservaId(reservaId: Int): Boolean
}
