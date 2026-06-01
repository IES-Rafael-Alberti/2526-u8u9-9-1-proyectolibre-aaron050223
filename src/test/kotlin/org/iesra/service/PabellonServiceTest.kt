package org.iesra.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.iesra.dao.PistaDAO
import org.iesra.dao.ResenaDAO
import org.iesra.dao.memory.ReservaDAOMemory
import org.iesra.model.Pista
import org.iesra.model.Resena
import java.time.LocalDate

class PabellonServiceTest : StringSpec({

    val hoy = LocalDate.of(2026, 6, 1)

    fun servicioConMemoria(): Triple<PabellonService, ReservaDAOMemory, FakeResenaDAO> {
        val reservaDAO = ReservaDAOMemory()
        val pistaDAO = FakePistaDAO(
            listOf(
                Pista(1, "Futbol"),
                Pista(2, "Baloncesto"),
                Pista(3, "Padel"),
                Pista(4, "Futbol Sala")
            )
        )
        val resenaDAO = FakeResenaDAO()
        val service = PabellonService(reservaDAO, pistaDAO, resenaDAO)
        return Triple(service, reservaDAO, resenaDAO)
    }

    "hacerReserva debe devolver true si el turno esta libre" {
        val (servicio, _, _) = servicioConMemoria()

        val ok = servicio.hacerReserva(idPista = 1, fecha = "10-06-2026", turno = 3, usuario = "Ana")
        ok shouldBe true
    }

    "hacerReserva debe devolver false si el turno ya esta ocupado" {
        val (servicio, _, _) = servicioConMemoria()

        servicio.hacerReserva(idPista = 1, fecha = "10-06-2026", turno = 3, usuario = "Ana") shouldBe true
        servicio.hacerReserva(idPista = 1, fecha = "10-06-2026", turno = 3, usuario = "Pepe") shouldBe false
    }

    "obtenerTurnosOcupados debe devolver los turnos ocupados de una pista y fecha" {
        val (servicio, _, _) = servicioConMemoria()

        servicio.hacerReserva(1, "10-06-2026", 1, "A")
        servicio.hacerReserva(1, "10-06-2026", 4, "B")
        servicio.hacerReserva(2, "10-06-2026", 2, "C")

        servicio.obtenerTurnosOcupados(1, "10-06-2026") shouldContainExactly listOf(1, 4)
    }

    "obtenerReservasFuturas debe incluir hoy y futuro, y excluir pasado" {
        val (servicio, _, _) = servicioConMemoria()

        // pasado
        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        // hoy
        servicio.hacerReserva(1, "01-06-2026", 2, "B")
        // futuro
        servicio.hacerReserva(2, "02-06-2026", 3, "C")

        val futuras = servicio.obtenerReservasFuturas(hoy)
        futuras.map { it.fecha } shouldContainExactly listOf("01-06-2026", "02-06-2026")
    }

    "obtenerReservasPasadas debe incluir solo pasado" {
        val (servicio, _, _) = servicioConMemoria()

        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        servicio.hacerReserva(1, "01-06-2026", 2, "B")

        val pasadas = servicio.obtenerReservasPasadas(hoy)
        pasadas.map { it.fecha } shouldContainExactly listOf("31-05-2026")
    }

    "eliminarReservaFuturaPorId debe borrar solo si la reserva es de hoy en adelante" {
        val (servicio, reservaDAO, _) = servicioConMemoria()

        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        servicio.hacerReserva(1, "01-06-2026", 2, "B")

        val reservas = reservaDAO.obtenerTodas().sortedBy { it.fecha }
        val idPasada = reservas.first { it.fecha == "31-05-2026" }.id
        val idHoy = reservas.first { it.fecha == "01-06-2026" }.id

        servicio.eliminarReservaFuturaPorId(idPasada, hoy) shouldBe false
        servicio.eliminarReservaFuturaPorId(idHoy, hoy) shouldBe true
    }

    "crearResena debe fallar si la nota esta fuera de 1..5" {
        val (servicio, reservaDAO, _) = servicioConMemoria()

        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        val idReserva = reservaDAO.obtenerTodas().single().id

        servicio.crearResena(idReserva, 0.5, "Bien", hoy) shouldBe false
        servicio.crearResena(idReserva, 5.5, "Bien", hoy) shouldBe false
    }

    "crearResena debe fallar si la descripcion no esta entre 1 y 100" {
        val (servicio, reservaDAO, _) = servicioConMemoria()

        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        val idReserva = reservaDAO.obtenerTodas().single().id

        servicio.crearResena(idReserva, 4.0, "   ", hoy) shouldBe false
        servicio.crearResena(idReserva, 4.0, "x".repeat(101), hoy) shouldBe false
    }

    "crearResena debe permitir reseñar solo reservas pasadas" {
        val (servicio, reservaDAO, _) = servicioConMemoria()

        servicio.hacerReserva(1, "01-06-2026", 1, "A") // hoy
        val idReserva = reservaDAO.obtenerTodas().single().id

        servicio.crearResena(idReserva, 4.0, "Correcto", hoy) shouldBe false
    }

    "crearResena debe crear una reseña si todo es valido" {
        val (servicio, reservaDAO, resenaDAO) = servicioConMemoria()

        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        val idReserva = reservaDAO.obtenerTodas().single().id

        servicio.crearResena(idReserva, 4.5, "Muy bien", hoy) shouldBe true
        (resenaDAO.obtenerPorReservaId(idReserva) != null) shouldBe true
    }

    "crearResena debe fallar si ya existe reseña para esa reserva" {
        val (servicio, reservaDAO, _) = servicioConMemoria()

        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        val idReserva = reservaDAO.obtenerTodas().single().id

        servicio.crearResena(idReserva, 4.0, "Primera", hoy) shouldBe true
        servicio.crearResena(idReserva, 5.0, "Segunda", hoy) shouldBe false
    }

    "obtenerReservasPasadasSinResena debe devolver solo reservas pasadas sin reseña" {
        val (servicio, reservaDAO, _) = servicioConMemoria()

        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        servicio.hacerReserva(1, "30-05-2026", 2, "B")
        val ids = reservaDAO.obtenerTodas().associateBy { it.fecha }
        val idReseñada = ids["30-05-2026"]!!.id

        servicio.crearResena(idReseñada, 4.0, "Ok", hoy) shouldBe true

        val disponibles = servicio.obtenerReservasPasadasSinResena(hoy)
        disponibles.map { it.fecha } shouldContainExactly listOf("31-05-2026")
    }

    "eliminarResenaPorReservaId debe devolver true si elimina, false si no existe" {
        val (servicio, reservaDAO, _) = servicioConMemoria()

        servicio.hacerReserva(1, "31-05-2026", 1, "A")
        val idReserva = reservaDAO.obtenerTodas().single().id

        servicio.eliminarResenaPorReservaId(idReserva) shouldBe false
        servicio.crearResena(idReserva, 4.0, "Ok", hoy) shouldBe true
        servicio.eliminarResenaPorReservaId(idReserva) shouldBe true
        servicio.eliminarResenaPorReservaId(idReserva) shouldBe false
    }
})

private class FakePistaDAO(private val pistas: List<Pista>) : PistaDAO {
    override fun obtenerTodas(): List<Pista> = pistas
    override fun obtenerPorId(id: Int): Pista? = pistas.firstOrNull { it.id == id }
}

private class FakeResenaDAO : ResenaDAO {
    private val porReservaId = mutableMapOf<Int, Resena>()
    private var autoinc = 1

    override fun guardar(reservaId: Int, nota: Double, descripcion: String): Resena {
        if (porReservaId.containsKey(reservaId)) {
            throw IllegalStateException("Ya existe reseña para reservaId=$reservaId")
        }
        val resena = Resena(id = autoinc++.toString(), reservaId = reservaId, nota = nota, descripcion = descripcion)
        porReservaId[reservaId] = resena
        return resena
    }

    override fun obtenerTodas(): List<Resena> = porReservaId.values.toList()

    override fun obtenerPorReservaId(reservaId: Int): Resena? = porReservaId[reservaId]

    override fun eliminarPorReservaId(reservaId: Int): Boolean = porReservaId.remove(reservaId) != null
}
