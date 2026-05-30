package org.iesra

import org.iesra.config.DatabaseManager
import org.iesra.dao.bdd.ReservaDAOH2
import org.iesra.service.PabellonService
import org.iesra.ui.MenuTerminal

/** Punto de entrada de la aplicacion. */
fun main() {
    val databaseManager = DatabaseManager()
    databaseManager.inicializarBBDD()

    val dao = ReservaDAOH2()
    val servicio = PabellonService(dao)
    val ui = MenuTerminal(servicio)

    var bucle = true
    do {
        val continuar = ui.iniciarFlujoReserva()
        if (!continuar) bucle = false
    } while (bucle)
}
