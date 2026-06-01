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
        // Script equivalente en el repo: sql/schema.sql
        val sqlCrearPistas = """
            CREATE TABLE IF NOT EXISTS pistas (
                id INT PRIMARY KEY,
                deporte VARCHAR(50) NOT NULL
            );
        """.trimIndent()

        val sqlSeedPistas = """
            MERGE INTO pistas (id, deporte) KEY(id) VALUES
              (1, 'Fútbol'),
              (2, 'Baloncesto'),
              (3, 'Pádel'),
              (4, 'Fútbol Sala');
        """.trimIndent()

        val sqlCrearReservas = """
            CREATE TABLE IF NOT EXISTS reservas (
                id INT AUTO_INCREMENT PRIMARY KEY,
                id_pista INT NOT NULL,
                fecha VARCHAR(10) NOT NULL,
                turno INT NOT NULL,
                usuario VARCHAR(100) NOT NULL,
                CONSTRAINT fk_reservas_pistas
                    FOREIGN KEY (id_pista) REFERENCES pistas(id)
                    ON UPDATE RESTRICT
                    ON DELETE RESTRICT
            );
        """.trimIndent()

        try {
            conexion().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(sqlCrearPistas)
                    stmt.execute(sqlSeedPistas)
                    stmt.execute(sqlCrearReservas)
                }
            }
        } catch (e: SQLException) {
            System.err.println("Error al inicializar la base de datos: ${e.message}")
        }
    }
}
