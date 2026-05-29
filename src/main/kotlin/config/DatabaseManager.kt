package org.iesra.config

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseManager {
    /** URL JDBC de la base de datos H2 en fichero local. */
    private val URL = "jdbc:h2:./db/pabellon"
    private val USER = "sa"
    private val PASSWORD = ""

    /** Devuelve una nueva conexion a la base de datos. */
    fun conexion(): Connection {
        return DriverManager.getConnection(URL, USER, PASSWORD)
    }

    /** Inicializa el esquema necesario para la aplicacion. */
    fun inicializarBBDD() {
        val sqlCrearTabla = """
            CREATE TABLE IF NOT EXISTS reservas (
                id INT AUTO_INCREMENT PRIMARY KEY,
                id_pista INT NOT NULL,
                fecha VARCHAR(10) NOT NULL,
                turno INT NOT NULL,
                usuario VARCHAR(100) NOT NULL
            );
        """.trimIndent()

        try {
            conexion().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(sqlCrearTabla)
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al inicializar la base de datos: ${e.message}")
        }
    }
}
