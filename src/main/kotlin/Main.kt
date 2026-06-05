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

/**
 * Punto de entrada de la aplicacion.
 *
 * Pasos del arranque:
 *  1. Crea la carpeta `logs/` y redirige el log del driver de MongoDB a
 *     `logs/mongo.log` para no ensuciar la terminal.
 *  2. Conecta a MongoDB Atlas, selecciona la BD `pabellon` y la coleccion
 *     `resenas`, y crea el indice unico sobre `reservaId`.
 *  3. Inicializa el esquema H2 (crea `pistas` y `reservas` con FK, y siembra
 *     las pistas iniciales).
 *  4. Construye los DAOs, el servicio y la UI de consola.
 *  5. Ejecuta el bucle del menu hasta que el usuario decide salir.
 *  6. Cierra el cliente de MongoDB antes de terminar.
 */
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

    val reservaDAO = ReservaDAOH2(databaseManager)
    val pistaDAO = PistaDAOH2()
    val servicio = PabellonService(reservaDAO, pistaDAO, resenaDAO)
    val ui = MenuTerminal(servicio)

    do {
        val noSalir = ui.iniciarFlujoReserva()
    } while (noSalir)

    mongoClient.close()
}
