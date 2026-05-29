package org.iesra.ui


import org.iesra.service.PabellonService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

class MenuTerminal(private val servicio: PabellonService) {

    private val nombresDeportes = mapOf(1 to "Fútbol", 2 to "Baloncesto", 3 to "Pádel", 4 to "Fútbol Sala")

    private val horariosTurnos = mapOf(
        1 to "09:00 - 10:30", 2 to "10:30 - 12:00", 3 to "12:00 - 13:30", 4 to "13:30 - 15:00",
        5 to "15:00 - 16:30", 6 to "16:30 - 18:00", 7 to "18:00 - 19:30", 8 to "19:30 - 21:00"
    )

    /** @return true si el programa debe seguir, false si el usuario decidio salir. */
    fun iniciarFlujoReserva(): Boolean {
        var bucle = true
        while (bucle) {
            println("\n==========================================")
            println("              MENÚ PRINCIPAL                     ")
            println("==========================================")

            val opcion = preguntarOpcionMenu()
            when (opcion) {
                1 -> flujoHacerReserva()
                2 -> flujoEliminarReserva()
                3 -> mostrarTodasLasReservas()
                4 -> {
                    println("\nPrograma finalizado.")
                    return false
                }
            }
        }

        return true
    }

    private fun preguntarOpcionMenu(): Int {
        var bucle = true
        var opcion = 0
        while (bucle) {
            println("\n--- SELECCIÓN ---")
            println("1. Hacer reserva")
            println("2. Eliminar reserva")
            println("3. Ver reservas")
            println("4. Salir")
            print("Elige una opción (1-4): ")

            val input = readln().toIntOrNull()
            if (input != null && input in 1..4) {
                opcion = input
                bucle = false
            } else {
                println("❌ Opción inválida. Evita usar letras y escribe un número del 1 al 4.")
            }
        }

        return opcion
    }

    private fun flujoHacerReserva() {
        val idPista = preguntarDeporte() ?: return

        val fecha = preguntarFecha()
        mostrarDisponibilidad(idPista, fecha)
        val turno = preguntarTurno()
        val nombreUsuario = preguntarNombre()

        val reservaCompletada = servicio.hacerReserva(idPista, fecha, turno, nombreUsuario)
        if (reservaCompletada) {
            println("\n✅ ¡ÉXITO! Reserva realizada correctamente a nombre de $nombreUsuario.")
        } else {
            println("\n❌ Lo sentimos, ese turno ya está reservado.")
        }
    }

    private fun preguntarDeporte(): Int? {
        var bucle = true
        var idPista: Int? = null

        while (bucle) {
            println("\n--- SELECCIÓN DE DEPORTE ---")
            nombresDeportes.forEach { (id, nombre) -> println("$id. $nombre") }
            println("0. Volver")
            print("Elige una opción (0-4): ")

            val input = readln().toIntOrNull()
            if (input != null) {
                if (input == 0) {
                    bucle = false
                } else if (input in 1..4) {
                    idPista = input
                    bucle = false
                } else {
                    println("❌ Opción inválida. Evita usar letras y escribe un número del 0 al 4.")
                }
            } else {
                println("❌ Opción inválida. Evita usar letras y escribe un número del 0 al 4.")
            }
        }

        return idPista
    }

    private fun flujoEliminarReserva() {
        val reservas = servicio.obtenerTodasLasReservas()
        if (reservas.isEmpty()) {
            println("\nNo hay reservas registradas.")
            return
        }

        mostrarTodasLasReservas()
        val id = preguntarIdReserva() ?: return

        val eliminado = servicio.eliminarReservaPorId(id)
        if (eliminado) {
            println("\n✅ Reserva eliminada correctamente.")
        } else {
            println("\n❌ No existe ninguna reserva con ese ID.")
        }
    }

    private fun preguntarIdReserva(): Int? {
        var bucle = true
        var id: Int? = null

        while (bucle) {
            print("\nIntroduce el ID de la reserva a eliminar (0 para volver): ")
            val input = readln().toIntOrNull()
            if (input != null) {
                if (input == 0) {
                    bucle = false
                } else if (input > 0) {
                    id = input
                    bucle = false
                } else {
                    println("❌ ID no válido. Debe ser un número mayor que 0.")
                }
            } else {
                println("❌ ID no válido. Debe ser un número.")
            }
        }

        return id
    }

    private fun mostrarTodasLasReservas() {
        val reservas = servicio.obtenerTodasLasReservas()
        if (reservas.isEmpty()) {
            println("\nNo hay reservas registradas.")
            return
        }

        println("\n--- LISTADO DE RESERVAS ---")
        reservas.forEach {
            val deporte = nombresDeportes[it.idPista] ?: "Pista ${it.idPista}"
            val horario = horariosTurnos[it.turno] ?: "Turno ${it.turno}"
            println("#${it.id} | $deporte | ${it.fecha} | $horario | ${it.usuario}")
        }
    }

    private fun preguntarFecha(): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)
        var bucle = true
        var fecha = ""
        while (bucle) {
            print("\nIntroduce la fecha (Ej: 28-05-2026): ")
            val input = readln().trim()
            try {
                val fechaIntroducida = LocalDate.parse(input, formatter)
                if (fechaIntroducida.isBefore(LocalDate.now())) {
                    println("❌ La fecha no puede ser anterior a la fecha actual.")
                } else {
                    fecha = input
                    bucle = false
                }
            } catch (e: DateTimeParseException) {
                println("❌ Formato incorrecto. Recuerda usar DD-MM-YYYY.")
            }
        }

        return fecha
    }

    private fun mostrarDisponibilidad(idPista: Int, fecha: String) {
        println("\n--- DISPONIBILIDAD PARA EL DÍA $fecha ---")
        val turnosOcupados = servicio.obtenerTurnosOcupados(idPista, fecha)

        for (turno in 1..8) {
            val estado = if (turnosOcupados.contains(turno)) "🔴 RESERVADO" else "🟢 LIBRE"
            println("Turno $turno (${horariosTurnos[turno]}): $estado")
        }
    }

    private fun preguntarTurno(): Int {
        var bucle = true
        var turno = 0
        while (bucle) {
            print("\nSelecciona el número de turno que deseas adquirir (1-8): ")
            val input = readln().toIntOrNull()

            if (input != null && input in 1..8) {
                turno = input
                bucle = false
            } else {
                println("❌ Error: Turno no válido. Debes introducir un número entre el 1 y el 8.")
            }
        }

        return turno
    }

    private fun preguntarNombre(): String {
        var bucle = true
        var nombre = ""
        while (bucle) {
            print("\nIntroduce el nombre de la persona que reserva: ")
            val input = readln().trim()

            if (input.isNotEmpty()) {
                nombre = input
                bucle = false
            } else {
                println("❌ Error: El nombre no puede estar vacío.")
            }
        }

        return nombre
    }
}
