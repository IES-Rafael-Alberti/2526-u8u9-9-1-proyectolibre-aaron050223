package org.iesra

import org.iesra.dao.memory.ReservaDAOMemory
import org.iesra.service.PabellonService
import org.iesra.ui.MenuTerminal

/** Punto de entrada de la aplicacion. */
fun main() {

    val dao = ReservaDAOMemory()
    val servicio = PabellonService(dao)
    val ui = MenuTerminal(servicio)

    var bucle = true
    do {
        val continuar = ui.iniciarFlujoReserva()
        if (!continuar) bucle = false
    } while (bucle)
}
