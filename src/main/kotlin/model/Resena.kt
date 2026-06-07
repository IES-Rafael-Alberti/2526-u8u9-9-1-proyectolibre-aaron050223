package org.iesra.model

/**
 * Reseña asociada a una reserva pasada.
 *
 * Se persiste en MongoDB Atlas (BD `pabellon`, coleccion `resenas`).
 * La unicidad de la reseña por reserva se garantiza con un indice
 * unico en el campo `reservaId` en MongoDB.
 *
 * @property id Identificador del documento en MongoDB (`_id`) como `String`.
 * @property reservaId Identificador de la reserva (FK logica a H2 `reservas.id`).
 * @property nota Valoracion entre `1.0` y `5.0`.
 * @property descripcion Texto de la reseña, longitud `1..100`.
 */
data class Resena(
    val id: String,
    val reservaId: Int,
    val nota: Double,
    val descripcion: String
)
