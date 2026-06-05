package org.iesra.dao.bdd


import org.iesra.config.DatabaseManager
import org.iesra.dao.ReservaDAO
import org.iesra.model.Reserva
import java.sql.SQLException

/**
 * Implementacion H2 de [ReservaDAO].
 *
 * Usa `PreparedStatement` (consultas parametrizadas) y `use { ... }`
 * para cerrar automaticamente la conexion, el statement y el result set.
 */
class ReservaDAOH2(val databaseManager: DatabaseManager) : ReservaDAO {

    /**
     * Inserta una nueva reserva. El `id` se genera en H2 (autoincrement),
     * por lo que se ignora el `id` del `Reserva` recibido.
     */
    override fun guardar(reserva: Reserva) {
        val sql = "INSERT INTO reservas (id_pista, fecha, turno, usuario) VALUES (?, ?, ?, ?)"

        try {
            databaseManager.conexion().use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.setInt(1, reserva.idPista)
                    ps.setString(2, reserva.fecha)
                    ps.setInt(3, reserva.turno)
                    ps.setString(4, reserva.usuario)

                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al guardar la reserva en H2: ${e.message}")
        }
    }

    /**
     * Devuelve las reservas de una pista concreta en una fecha concreta.
     * Usado para detectar turnos ocupados al crear una nueva reserva.
     */
    override fun buscarPorPistaYFecha(idPista: Int, fecha: String): List<Reserva> {
        val sql = "SELECT * FROM reservas WHERE id_pista = ? AND fecha = ?"
        val listaReservas = mutableListOf<Reserva>()

        try {
            databaseManager.conexion().use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.setInt(1, idPista)
                    ps.setString(2, fecha)

                    ps.executeQuery().use {
                        while (it.next()) {
                            val reserva = Reserva(
                                id = it.getInt("id"),
                                idPista = it.getInt("id_pista"),
                                fecha = it.getString("fecha"),
                                turno = it.getInt("turno"),
                                usuario = it.getString("usuario")
                            )
                            listaReservas.add(reserva)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al buscar reservas en H2: ${e.message}")
        }

        return listaReservas
    }

    /** Devuelve todas las reservas ordenadas por fecha, pista y turno. */
    override fun obtenerTodas(): List<Reserva> {
        val sql = "SELECT * FROM reservas ORDER BY fecha, id_pista, turno"
        val listaReservas = mutableListOf<Reserva>()

        try {
            databaseManager.conexion().use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.executeQuery().use {
                        while (it.next()) {
                            val reserva = Reserva(
                                id = it.getInt("id"),
                                idPista = it.getInt("id_pista"),
                                fecha = it.getString("fecha"),
                                turno = it.getInt("turno"),
                                usuario = it.getString("usuario")
                            )
                            listaReservas.add(reserva)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al obtener todas las reservas en H2: ${e.message}")
        }

        return listaReservas
    }

    /** Devuelve la reserva con el id indicado, o `null` si no existe. */
    override fun obtenerPorId(id: Int): Reserva? {
        val sql = "SELECT * FROM reservas WHERE id = ?"

        try {
            databaseManager.conexion().use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)

                    ps.executeQuery().use {
                        if (it.next()) {
                            return Reserva(
                                id = it.getInt("id"),
                                idPista = it.getInt("id_pista"),
                                fecha = it.getString("fecha"),
                                turno = it.getInt("turno"),
                                usuario = it.getString("usuario")
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al obtener reserva por id en H2: ${e.message}")
        }

        return null
    }

    /** Elimina la reserva con el id indicado. */
    override fun eliminarPorId(id: Int) {
        val sql = "DELETE FROM reservas WHERE id = ?"

        try {
            databaseManager.conexion().use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al eliminar la reserva en H2: ${e.message}")
        }
    }
}
