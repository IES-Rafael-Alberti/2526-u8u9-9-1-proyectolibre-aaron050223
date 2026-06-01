package org.iesra.dao.bdd

import org.iesra.config.DatabaseManager
import org.iesra.dao.PistaDAO
import org.iesra.model.Pista
import java.sql.SQLException

class PistaDAOH2 : PistaDAO {
    override fun obtenerTodas(): List<Pista> {
        val sql = "SELECT * FROM pistas ORDER BY id"
        val pistas = mutableListOf<Pista>()

        try {
            val databaseManager = DatabaseManager()
            databaseManager.conexion().use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.executeQuery().use {
                        while (it.next()) {
                            pistas.add(
                                Pista(
                                    id = it.getInt("id"),
                                    deporte = it.getString("deporte")
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al obtener pistas en H2: ${e.message}")
        }

        return pistas
    }

    override fun obtenerPorId(id: Int): Pista? {
        val sql = "SELECT * FROM pistas WHERE id = ?"

        try {
            val databaseManager = DatabaseManager()
            databaseManager.conexion().use { conn ->
                conn.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use {
                        if (it.next()) {
                            return Pista(
                                id = it.getInt("id"),
                                deporte = it.getString("deporte")
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al obtener pista por id en H2: ${e.message}")
        }

        return null
    }
}
