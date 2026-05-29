package org.iesra.model

data class Reserva (
    val id: Int,
    val idPista: Int,
    val fecha: String,
    val turno: Int,
    val usuario: String
)