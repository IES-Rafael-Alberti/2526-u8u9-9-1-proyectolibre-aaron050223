-- Esquema H2: pistas (diccionario) + reservas (FK)

CREATE TABLE IF NOT EXISTS pistas (
  id INT PRIMARY KEY,
  deporte VARCHAR(50) NOT NULL
);

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

-- Datos iniciales de pistas (idempotente)
MERGE INTO pistas (id, deporte) KEY(id) VALUES
  (1, 'Fútbol'),
  (2, 'Baloncesto'),
  (3, 'Pádel'),
  (4, 'Fútbol Sala');
