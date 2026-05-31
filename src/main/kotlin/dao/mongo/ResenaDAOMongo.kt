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

class ResenaDAOMongo(private val collection: MongoCollection<Document>) : ResenaDAO {

    fun asegurarIndices() {
        // Una sola reseña por reserva.
        collection.createIndex(Indexes.ascending("reservaId"), IndexOptions().unique(true))
    }

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

    override fun obtenerTodas(): List<Resena> {
        return collection.find().map { it.toResena() }.toList()
    }

    override fun obtenerPorReservaId(reservaId: Int): Resena? {
        val doc = collection.find(Filters.eq("reservaId", reservaId)).first() ?: return null
        return doc.toResena()
    }

    override fun eliminarPorReservaId(reservaId: Int): Boolean {
        val result = collection.deleteOne(Filters.eq("reservaId", reservaId))
        return result.deletedCount > 0
    }
}

private fun Document.toResena(): Resena {
    val id = (getObjectId("_id") ?: ObjectId()).toHexString()
    return Resena(
        id = id,
        reservaId = getInteger("reservaId"),
        nota = getDouble("nota"),
        descripcion = getString("descripcion")
    )
}
