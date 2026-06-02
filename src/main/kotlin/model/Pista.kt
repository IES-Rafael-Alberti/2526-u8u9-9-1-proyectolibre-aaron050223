package org.iesra.model

/**
 * Representa una pista deportiva disponible para reservar.
 *
 * Se persiste en H2 (tabla `pistas`) y se usa como diccionario para
 * traducir `idPista` a un deporte legible (por ejemplo, `1 -> "Futbol"`).
 *
 * @property id Identificador unico de la pista (PK en H2).
 * @property deporte Nombre del deporte asociado (por ejemplo, `Futbol`).
 */
data class Pista(
    val id: Int,
    val deporte: String
)
