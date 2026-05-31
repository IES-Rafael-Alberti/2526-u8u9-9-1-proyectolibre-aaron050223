package org.iesra.model

/** Reseña asociada a una reserva pasada. Se persiste en MongoDB. */
data class Resena(
    val id: String,
    val reservaId: Int,
    val nota: Double,
    val descripcion: String
)
