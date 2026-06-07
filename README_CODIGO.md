## Estructura de paquetes

```
src/main/kotlin/
  org.iesra/
    config/   -> configuracion de BBDD
    dao/      -> interfaces DAO
    dao/bdd/  -> DAOs SQL (H2)
    dao/memory/ -> DAOs en memoria (para tests)
    dao/mongo/ -> DAOs MongoDB
    model/    -> entidades del dominio
    service/  -> logica de negocio
    ui/       -> interfaz por consola
```

## Punto de entrada

### `Main.kt`

`fun main()`

Arranque del programa:

1. Crea `logs/` y redirige el log del driver de MongoDB a `logs/mongo.log`.
2. Crea el `MongoClient`, abre la BD `pabellon` y la coleccion `resenas`,
   y llama a `ResenaDAOMongo.asegurarIndices()` para crear el indice unico
   sobre `reservaId`.
3. Inicializa H2 con `DatabaseManager.inicializarBBDD()` (crea `pistas`,
   `reservas` con FK y siembra las 4 pistas).
4. Instancia los DAOs, el servicio `PabellonService` y la UI `MenuTerminal`.
5. Ejecuta el bucle del menu hasta que el usuario decide salir.
6. Cierra el `MongoClient` al terminar.

## Capa de modelo (model/)

### `Reserva` (data class)

Representa una reserva de una pista para una fecha y un turno.

- `id: Int` -> autonumerico de H2 (PK).
- `idPista: Int` -> FK a `pistas.id`.
- `fecha: String` -> `dd-MM-uuuu`.
- `turno: Int` -> 1..8.
- `usuario: String` -> nombre de quien reserva.

### `Pista` (data class)

Diccionario de pistas.

- `id: Int` -> PK.
- `deporte: String` -> por ejemplo, "Futbol".

### `Resena` (data class)

Reseña asociada a una reserva pasada (se guarda en MongoDB).

- `id: String` -> `_id` de Mongo en hexadecimal.
- `reservaId: Int` -> FK logica a `reservas.id` en H2.
- `nota: Double` -> 1.0..5.0.
- `descripcion: String` -> 1..100 caracteres.

## Capa DAO - interfaces (dao/)

### `ReservaDAO`

Contrato para acceso a reservas (H2 o memoria).

- `guardar(reserva: Reserva)` -> inserta una nueva reserva.
- `buscarPorPistaYFecha(idPista, fecha): List<Reserva>` -> reservas de
  una pista y fecha concretas (se usa para detectar turno ocupado).
- `obtenerTodas(): List<Reserva>` -> todas las reservas.
- `obtenerPorId(id: Int): Reserva?` -> reserva por id o `null`.
- `eliminarPorId(id: Int)` -> elimina por id.

### `PistaDAO`

Contrato para acceso a `pistas`.

- `obtenerTodas(): List<Pista>` -> todas las pistas ordenadas por id.
- `obtenerPorId(id: Int): Pista?` -> pista por id o `null`.

### `ResenaDAO`

Contrato para acceso a reseñas (MongoDB).

- `guardar(reservaId, nota, descripcion): Resena` -> inserta y devuelve
  la reseña con su id.
- `obtenerTodas(): List<Resena>` -> todas las reseñas.
- `obtenerPorReservaId(reservaId: Int): Resena?` -> la reseña de una
  reserva concreta o `null`.
- `eliminarPorReservaId(reservaId: Int): Boolean` -> `true` si existia.

## Capa DAO - implementaciones

### `ReservaDAOH2` (dao/bdd)

Implementacion H2 de `ReservaDAO`. Usa `PreparedStatement` (consultas
parametrizadas) y `use{}` para cierre automatico de recursos.

- `guardar` -> `INSERT INTO reservas (id_pista, fecha, turno, usuario) VALUES (?, ?, ?, ?)`.
- `buscarPorPistaYFecha` -> `SELECT * FROM reservas WHERE id_pista = ? AND fecha = ?`.
- `obtenerTodas` -> `SELECT * FROM reservas ORDER BY fecha, id_pista, turno`.
- `obtenerPorId` -> `SELECT * FROM reservas WHERE id = ?`.
- `eliminarPorId` -> `DELETE FROM reservas WHERE id = ?`.

Captura `SQLException` y muestra el error por stderr sin tirar la app.

### `PistaDAOH2` (dao/bdd)

Implementacion H2 de `PistaDAO`.

- `obtenerTodas` -> `SELECT * FROM pistas ORDER BY id`.
- `obtenerPorId` -> `SELECT * FROM pistas WHERE id = ?`.

Mismo patron de cierre con `use { ... }` y captura de `SQLException`.

### `ResenaDAOMongo` (dao/mongo)

Implementacion MongoDB (Atlas) de `ResenaDAO`. Trabaja con `Document` y
una extension privada al final del fichero (`toResena()`) que mapea
`Document <-> Resena`.

- `asegurarIndices()` -> crea un indice unico sobre `reservaId`.
- `guardar` -> `insertOne` con campos `reservaId`, `nota`, `descripcion`,
  `createdAt`. Propaga `MongoWriteException` si el indice unico falla.
- `obtenerTodas` -> `collection.find()`.
- `obtenerPorReservaId` -> `find(eq("reservaId", reservaId)).first()`.
- `eliminarPorReservaId` -> `deleteOne(eq(...))`, devuelve `true` si
  `deletedCount > 0`.

### `ReservaDAOMemory` (dao/memory)

Implementacion en memoria de `ReservaDAO` usada en los tests de Kotest.
Mantiene un `MutableList<Reserva>` y un id incremental.

## Configuracion de BBDD

### `DatabaseManager` (config)

Gestiona la conexion y la inicializacion del esquema H2.

- `conexion(): Connection` -> `DriverManager.getConnection("jdbc:h2:./db/pabellon", ...)`.
- `inicializarBBDD()` -> crea la tabla `pistas`, siembra los 4 deportes
  con `MERGE`, y crea `reservas` con FK a `pistas.id`.
  Si el script se actualiza, mantener sincronizado con `sql/schema.sql`.

## Capa de servicio

### `PabellonService` (service)

Concentra las reglas de negocio. Depende de interfaces DAO (DIP), asi
que se puede testear con fakes en memoria sin tocar H2 ni Mongo.

- `obtenerPistas(): List<Pista>` -> todas las pistas (H2).
- `obtenerMapaPistas(): Map<Int, String>` -> traductor rapido `id -> deporte`.
- `hacerReserva(idPista, fecha, turno, usuario): Boolean` -> valida que
  el turno no este ocupado; si lo esta devuelve `false`.
- `obtenerTurnosOcupados(idPista, fecha): List<Int>` -> turnos reservados
  para una pista y fecha.
- `obtenerTodasLasReservas(): List<Reserva>` -> todas las reservas.
- `obtenerReservasPasadasSinResena(hoy): List<Reserva>` -> reservas
  pasadas que aun no tienen reseña. Construye un `Set<Int>` con los
  `reservaId` reseñados para filtrar eficientemente.
- `crearResena(reservaId, nota, descripcion, hoy): Boolean` -> valida
  nota (1..5) y descripcion (1..100), que la reserva exista, que sea
  pasada y que no tenga ya reseña. Devuelve `true` si guarda.
- `obtenerResenas(): List<Resena>` -> todas las reseñas.
- `eliminarResenaPorReservaId(reservaId): Boolean` -> elimina en Mongo.
- `obtenerReservasFuturas(hoy): List<Reserva>` -> reservas con fecha
  `>= hoy`, ordenadas por fecha/pista/turno.
- `obtenerReservasPasadas(hoy): List<Reserva>` -> reservas con fecha
  `< hoy`, ordenadas por fecha/pista/turno.
- `eliminarReservaPorId(id): Boolean` -> elimina sin aplicar reglas de fecha.
- `eliminarReservaFuturaPorId(id, hoy): Boolean` -> solo borra si la
  reserva es de hoy en adelante.
- `limpiarReservasAntiguas(hoy): Int` -> cuenta reservas pasadas. Ya no
  borra: el historico se conserva para reseñas.

## Capa de UI (consola)

### `MenuTerminal` (ui)

Interfaz por consola. Solo se ocupa de entrada/salida y validacion
sintactica (rangos, regex); la logica vive en `PabellonService`.

Propiedades / utilidades:

- `nombresDeportes: Map<Int, String>` -> cache de `pistas` cargada al
  construirse.
- `horariosTurnos: Map<Int, String>` -> mapa de turno a franja horaria.

Metodos publicos:

- `esNombreValido(nombre: String): Boolean` -> valida con regex
  `^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?: [A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$` y longitud 2..30.
  Usado por `preguntarNombre` y por el test Kotest de regex.
- `iniciarFlujoReserva(): Boolean` -> bucle principal del menu.

Metodos privados (flujos de menu):

- `preguntarOpcionMenu()` -> lee 1..5 y valida.
- `flujoHacerReserva()` -> pide pista, fecha, turno y nombre; muestra
  disponibilidad y llama a `servicio.hacerReserva`.
- `flujoEliminarReserva()` -> lista reservas futuras y permite borrar
  por id con `servicio.eliminarReservaFuturaPorId`.
- `mostrarTodasLasReservas()` -> muestra reservas futuras.
- `flujoResenas()` -> submenu de reseñas.
- `preguntarOpcionResenas()` -> lee 1..4 y valida.
- `flujoCrearResena()` -> muestra reservas pasadas sin reseña, pide id,
  nota y descripcion, y guarda con `servicio.crearResena`.
- `flujoVerResenas()` -> lista reseñas y, si puede, cruza con la
  reserva para mostrar contexto.
- `flujoEliminarResena()` -> lista reseñas y permite borrar por
  `reservaId` con `servicio.eliminarResenaPorReservaId`.

Metodos privados de validacion / lectura:

- `preguntarDeporte(): Int?` -> pide id de pista (0 = volver).
- `preguntarFecha(): String` -> valida formato `dd-MM-uuuu` y que la
  fecha no sea pasada (captura `DateTimeParseException`).
- `preguntarTurno(): Int` -> valida 1..8.
- `preguntarNombre(): String` -> valida con `esNombreValido`.
- `mostrarDisponibilidad(idPista, fecha)` -> muestra los 8 turnos
  como libre / reservado.
- `preguntarIdReserva(): Int?` y `preguntarIdReservaResena(): Int?` ->
  piden un id positivo (0 = volver).
- `preguntarNotaResena(): Double?` -> 1..5, acepta coma o punto
  (0 = volver).
- `preguntarDescripcionResena(): String?` -> 1..100 caracteres
  (0 = volver).

## Tests (src/test/kotlin/)

### `PabellonServiceTest` (StringSpec, Kotest)

Cubre la logica de negocio del `PabellonService` usando
`ReservaDAOMemory`, `FakePistaDAO` y `FakeResenaDAO` (no tocan H2 ni
Mongo). Cubre:

- reservas: crear OK, evitar duplicado de turno, turnos ocupados,
  filtrado futuras/pasadas, eliminar solo futuras.
- reseñas: nota invalida, descripcion invalida, solo pasadas, una
  unica por reserva, listado de pasadas sin reseña, eliminar.

### `MenuTerminalRegexTest` (StringSpec, Kotest)

Cubre `esNombreValido` con casos validos y no validos (numeros,
espacios al inicio/final, longitud).

## Resumen de archivos

```
src/main/kotlin/
  Main.kt
  config/DatabaseManager.kt
  dao/
    ReservaDAO.kt
    PistaDAO.kt
    ResenaDAO.kt
    bdd/ReservaDAOH2.kt
    bdd/PistaDAOH2.kt
    memory/ReservaDAOMemory.kt
    mongo/ResenaDAOMongo.kt
  model/
    Reserva.kt
    Pista.kt
    Resena.kt
  service/PabellonService.kt
  ui/MenuTerminal.kt

src/test/kotlin/
  org/iesra/service/PabellonServiceTest.kt

sql/
  schema.sql
```
