package org.iesra.dao.mongo

import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.Document
import org.bson.types.ObjectId
import org.iesra.dao.ResenaDAO
import org.iesra.model.Resena

/**
 * Implementacion MongoDB (Atlas) de [ResenaDAO].
 *
 * Trabaja con la coleccion `pabellon.resenas` usando [Document] como
 * representacion intermedia. La conversion `Document <-> Resena`
 * se hace en una extension privada al final del fichero.
 */
class ResenaDAOMongo(private val collection: MongoCollection<Document>) : ResenaDAO {

    /**
     * Crea el indice unico sobre el campo `reservaId`.
     *
     * Esto garantiza a nivel de base de datos que solo pueda existir
     * una reseña por reserva, incluso si la validacion logica falla.
     */
    fun asegurarIndices() {
        // Una sola reseña por reserva.
        collection.createIndex(Indexes.ascending("reservaId"), IndexOptions().unique(true))
    }

    /**
     * Inserta una reseña. Si la reserva ya tiene reseña, Mongo lanza
     * [MongoWriteException] y la excepcion se propaga para que el
     * servicio decida como reaccionar.
     */
    override fun guardar(reservaId: Int, nota: Double, descripcion: String): Resena {
        val doc = Document()
            .append("reservaId", reservaId)
            .append("nota", nota)
            .append("descripcion", descripcion)
            .append("createdAt", System.currentTimeMillis())

        try {
            collection.insertOne(doc)
        } catch (e: MongoWriteException) {
            // Se deja propagar para que el servicio pueda decidir como reaccionar.
            throw e
        }

        val id = (doc.getObjectId("_id") ?: ObjectId()).toHexString()
        return Resena(id = id, reservaId = reservaId, nota = nota, descripcion = descripcion)
    }

    /** Devuelve todas las reseñas de la coleccion. */
    override fun obtenerTodas(): List<Resena> {
        return collection.find().map { it.toResena() }.toList()
    }

    /**
     * Devuelve la reseña asociada a la reserva indicada.
     * @return `Resena` si existe, `null` en caso contrario.
     */
    override fun obtenerPorReservaId(reservaId: Int): Resena? {
        val doc = collection.find(Filters.eq("reservaId", reservaId)).first() ?: return null
        return doc.toResena()
    }

    /**
     * Elimina la reseña de la reserva indicada.
     * @return `true` si se elimino, `false` si no existia.
     */
    override fun eliminarPorReservaId(reservaId: Int): Boolean {
        val result = collection.deleteOne(Filters.eq("reservaId", reservaId))
        return result.deletedCount > 0
    }
}

/** Convierte un [Document] de MongoDB en [Resena]. */
private fun Document.toResena(): Resena {
    val id = (getObjectId("_id") ?: ObjectId()).toHexString()
    return Resena(
        id = id,
        reservaId = getInteger("reservaId"),
        nota = getDouble("nota"),
        descripcion = getString("descripcion")
    )
}
