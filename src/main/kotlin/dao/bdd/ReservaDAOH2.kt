package org.iesra.dao.bdd


import org.iesra.config.DatabaseManager
import org.iesra.dao.ReservaDAO
import org.iesra.model.Reserva
import java.sql.SQLException

class ReservaDAOH2 : ReservaDAO {

    override fun guardar(reserva: Reserva) {
        val sql = "INSERT INTO reservas (id_pista, fecha, turno, usuario) VALUES (?, ?, ?, ?)"

        try {
            val databaseManager = DatabaseManager()
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

    override fun buscarPorPistaYFecha(idPista: Int, fecha: String): List<Reserva> {
        val sql = "SELECT * FROM reservas WHERE id_pista = ? AND fecha = ?"
        val listaReservas = mutableListOf<Reserva>()

        try {
            val databaseManager = DatabaseManager()
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

    override fun obtenerTodas(): List<Reserva> {
        val sql = "SELECT * FROM reservas ORDER BY fecha, id_pista, turno"
        val listaReservas = mutableListOf<Reserva>()

        try {
            val databaseManager = DatabaseManager()
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

    override fun obtenerPorId(id: Int): Reserva? {
        val sql = "SELECT * FROM reservas WHERE id = ?"

        try {
            val databaseManager = DatabaseManager()
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

    override fun eliminarPorId(id: Int) {
        val sql = "DELETE FROM reservas WHERE id = ?"

        try {
            val databaseManager = DatabaseManager()
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
