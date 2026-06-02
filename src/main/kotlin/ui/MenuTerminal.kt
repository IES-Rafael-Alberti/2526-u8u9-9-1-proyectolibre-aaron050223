package org.iesra.ui


import org.iesra.service.PabellonService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

class MenuTerminal(private val servicio: PabellonService) {

    private val nombresDeportes = servicio.obtenerMapaPistas()

    private val horariosTurnos = mapOf(
        1 to "09:00 - 10:30", 2 to "10:30 - 12:00", 3 to "12:00 - 13:30", 4 to "13:30 - 15:00",
        5 to "15:00 - 16:30", 6 to "16:30 - 18:00", 7 to "18:00 - 19:30", 8 to "19:30 - 21:00"
    )

    init {
        if (nombresDeportes.isEmpty()) {
            System.err.println("No se han encontrado pistas en la base de datos. Revisa la inicialización.")
        }
    }

    /** Valida el nombre de usuario: solo letras/espacios, sin espacios al inicio/final, longitud 2..30. */
    fun esNombreValido(nombre: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?: [A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$")
        return nombre.length in 2..30 && regex.matches(nombre)
    }

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
                4 -> flujoResenas()
                5 -> {
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
            println("4. Reseñas")
            println("5. Salir")
            print("Elige una opción (1-5): ")

            val input = readln().toIntOrNull()
            if (input != null && input in 1..5) {
                opcion = input
                bucle = false
            } else {
                println("❌ Opción inválida. Evita usar letras y escribe un número del 1 al 5.")
            }
        }

        return opcion
    }

    private fun flujoResenas() {
        var bucle = true
        while (bucle) {
            println("\n==========================================")
            println("                 RESEÑAS                 ")
            println("==========================================")

            val opcion = preguntarOpcionResenas()
            when (opcion) {
                1 -> flujoCrearResena()
                2 -> flujoVerResenas()
                3 -> flujoEliminarResena()
                4 -> bucle = false
            }
        }
    }

    private fun preguntarOpcionResenas(): Int {
        var bucle = true
        var opcion = 0
        while (bucle) {
            println("\n--- SELECCIÓN ---")
            println("1. Hacer reseña")
            println("2. Ver reseñas")
            println("3. Eliminar reseña")
            println("4. Volver")
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

    private fun flujoCrearResena() {
        val reservas = servicio.obtenerReservasPasadasSinResena()
        if (reservas.isEmpty()) {
            println("\nNo hay reservas pasadas disponibles para reseñar.")
            return
        }

        println("\n--- RESERVAS PASADAS (SIN RESEÑA) ---")
        reservas.forEach {
            val deporte = nombresDeportes[it.idPista] ?: "Pista ${it.idPista}"
            val horario = horariosTurnos[it.turno] ?: "Turno ${it.turno}"
            println("#${it.id} | $deporte | ${it.fecha} | $horario | ${it.usuario}")
        }

        val reservaId = preguntarIdReservaResena() ?: return
        if (reservas.none { it.id == reservaId }) {
            println("\n❌ Debes elegir el ID de una de las reservas mostradas.")
            return
        }

        val nota = preguntarNotaResena() ?: return
        val descripcion = preguntarDescripcionResena() ?: return

        val ok = servicio.crearResena(reservaId = reservaId, nota = nota, descripcion = descripcion)
        if (ok) {
            println("\n✅ Reseña guardada correctamente.")
        } else {
            println("\n❌ No se pudo guardar la reseña. Revisa nota (1-5) y descripción (1-100), o puede que ya exista.")
        }
    }

    private fun flujoVerResenas() {
        val resenas = servicio.obtenerResenas()
        if (resenas.isEmpty()) {
            println("\nNo hay reseñas registradas.")
            return
        }

        val reservasPorId = servicio.obtenerTodasLasReservas().associateBy { it.id }

        println("\n--- LISTADO DE RESEÑAS ---")
        resenas.forEach { r ->
            val reserva = reservasPorId[r.reservaId]
            val cabecera = if (reserva != null) {
                val deporte = nombresDeportes[reserva.idPista] ?: "Pista ${reserva.idPista}"
                val horario = horariosTurnos[reserva.turno] ?: "Turno ${reserva.turno}"
                "Reserva #${reserva.id} | $deporte | ${reserva.fecha} | $horario | ${reserva.usuario}"
            } else {
                "Reserva #${r.reservaId}"
            }

            println("$cabecera | Nota: ${r.nota} | ${r.descripcion}")
        }
    }

    private fun flujoEliminarResena() {
        val resenas = servicio.obtenerResenas()
        if (resenas.isEmpty()) {
            println("\nNo hay reseñas para eliminar.")
            return
        }

        println("\n--- RESEÑAS REGISTRADAS ---")
        resenas.forEach { println("Reserva #${it.reservaId} | Nota: ${it.nota} | ${it.descripcion}") }

        val reservaId = preguntarIdReservaResena() ?: return
        val eliminado = servicio.eliminarResenaPorReservaId(reservaId)
        if (eliminado) {
            println("\n✅ Reseña eliminada correctamente.")
        } else {
            println("\n❌ No existe ninguna reseña para esa reserva.")
        }
    }

    private fun preguntarIdReservaResena(): Int? {
        var bucle = true
        var id: Int? = null

        while (bucle) {
            print("\nIntroduce el ID de la reserva (0 para volver): ")
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

    private fun preguntarNotaResena(): Double? {
        var bucle = true
        var nota: Double? = null

        while (bucle) {
            print("\nIntroduce una nota (1-5) (0 para volver): ")
            val input = readln().trim()
            if (input == "0") return null

            val normalizado = input.replace(',', '.')
            val valor = normalizado.toDoubleOrNull()
            if (valor != null && valor in 1.0..5.0) {
                nota = valor
                bucle = false
            } else {
                println("❌ Nota no válida. Debe ser un número entre 1 y 5.")
            }
        }

        return nota
    }

    private fun preguntarDescripcionResena(): String? {
        var bucle = true
        var descripcion: String? = null

        while (bucle) {
            print("\nIntroduce una descripción (1-100 caracteres) (0 para volver): ")
            val input = readln()
            if (input == "0") return null

            val desc = input.trim()
            if (desc.length in 1..100) {
                descripcion = desc
                bucle = false
            } else {
                println("❌ Descripción no válida. Debe tener entre 1 y 100 caracteres.")
            }
        }

        return descripcion
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
            nombresDeportes.toSortedMap().forEach { (id, nombre) -> println("$id. $nombre") }
            println("0. Volver")
            val maxId = (nombresDeportes.keys.maxOrNull() ?: 0)
            print("Elige una opción (0-$maxId): ")

            val input = readln().toIntOrNull()
            if (input != null) {
                if (input == 0) {
                    bucle = false
                } else if (nombresDeportes.containsKey(input)) {
                    idPista = input
                    bucle = false
                } else {
                    println("❌ Opción inválida. Evita usar letras y elige un id de pista válido.")
                }
            } else {
                println("❌ Opción inválida. Evita usar letras y elige un id de pista válido.")
            }
        }

        return idPista
    }

    private fun flujoEliminarReserva() {
        val reservas = servicio.obtenerReservasFuturas()
        if (reservas.isEmpty()) {
            println("\nNo hay reservas registradas.")
            return
        }

        mostrarTodasLasReservas()
        val id = preguntarIdReserva() ?: return

        val eliminado = servicio.eliminarReservaFuturaPorId(id)
        if (eliminado) {
            println("\n✅ Reserva eliminada correctamente.")
        } else {
            println("\n❌ No existe ninguna reserva futura con ese ID.")
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
        val reservas = servicio.obtenerReservasFuturas()
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

            if (esNombreValido(input)) {
                nombre = input
                bucle = false
            } else {
                println("❌ Nombre no válido. Solo letras y espacios (2..30), sin espacios al inicio/final.")
            }
        }

        return nombre
    }
}
