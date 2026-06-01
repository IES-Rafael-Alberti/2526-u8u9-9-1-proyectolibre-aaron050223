package org.iesra

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.iesra.config.DatabaseManager
import org.iesra.dao.bdd.PistaDAOH2
import org.iesra.dao.bdd.ReservaDAOH2
import org.iesra.dao.mongo.ResenaDAOMongo
import org.iesra.service.PabellonService
import org.iesra.ui.MenuTerminal
import java.io.File

/** Punto de entrada de la aplicacion. */
fun main() {
    File("logs").mkdirs()
    System.setProperty("org.slf4j.simpleLogger.logFile", "logs/mongo.log")
    val mongoUri = "uri"

    val mongoClient: MongoClient = MongoClients.create(ConnectionString(mongoUri))
    val mongoDatabase = mongoClient.getDatabase("pabellon")
    val resenasCollection = mongoDatabase.getCollection("resenas")
    val resenaDAO = ResenaDAOMongo(resenasCollection).also { it.asegurarIndices() }

    val databaseManager = DatabaseManager()
    databaseManager.inicializarBBDD()

    val reservaDAO = ReservaDAOH2()
    val pistaDAO = PistaDAOH2()
    val servicio = PabellonService(reservaDAO, pistaDAO, resenaDAO)
    val ui = MenuTerminal(servicio)

    var bucle = true
    do {
        val continuar = ui.iniciarFlujoReserva()
        if (!continuar) bucle = false
    } while (bucle)

    mongoClient.close()
}
