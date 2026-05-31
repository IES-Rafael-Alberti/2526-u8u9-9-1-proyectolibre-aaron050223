package org.iesra

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.iesra.config.DatabaseManager
import org.iesra.dao.bdd.ReservaDAOH2
import org.iesra.dao.mongo.ResenaDAOMongo
import org.iesra.service.PabellonService
import org.iesra.ui.MenuTerminal

/** Punto de entrada de la aplicacion. */
fun main() {
    // IMPORTANTE: no subas credenciales reales al repositorio.
    // Sustituye este placeholder por tu URI de MongoDB Atlas en local.
    val mongoUri = "mongodb+srv://userAlberti:QrlS6OcFnKhm1rn7@basedatosaaron.evvnhth.mongodb.net/?appName=basedatosaaron"

    val mongoClient: MongoClient = MongoClients.create(ConnectionString(mongoUri))
    val mongoDatabase = mongoClient.getDatabase("pabellon")
    val resenasCollection = mongoDatabase.getCollection("Reseñas")
    val resenaDAO = ResenaDAOMongo(resenasCollection).also { it.asegurarIndices() }

    val databaseManager = DatabaseManager()
    databaseManager.inicializarBBDD()

    val dao = ReservaDAOH2()
    val servicio = PabellonService(dao, resenaDAO)
    val ui = MenuTerminal(servicio)

    var bucle = true
    do {
        val continuar = ui.iniciarFlujoReserva()
        if (!continuar) bucle = false
    } while (bucle)

    mongoClient.close()
}
