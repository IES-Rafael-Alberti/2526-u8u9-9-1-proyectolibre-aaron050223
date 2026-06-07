package org.iesra.model

/**
 * Representa una reserva de una pista deportiva para una fecha y un turno concretos.
 *
 * Se persiste en H2 (tabla `reservas`) con una FK a `pistas.id`.
 *
 * @property id Identificador autonumerico en H2. Vale `0` cuando aun no se ha guardado.
 * @property idPista Identificador de la pista reservada (FK a `pistas.id`).
 * @property fecha Fecha de la reserva en formato `dd-MM-uuuu` (por ejemplo, `28-05-2026`).
 * @property turno Numero de turno dentro del dia (1..8).
 * @property usuario Nombre de la persona que reserva.
 */
data class Reserva (
    val id: Int,
    val idPista: Int,
    val fecha: String,
    val turno: Int,
    val usuario: String
)