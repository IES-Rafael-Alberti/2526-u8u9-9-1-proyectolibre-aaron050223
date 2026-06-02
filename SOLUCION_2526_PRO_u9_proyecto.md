# Solución del proyecto

- **Proyecto:** <!-- Nombre del proyecto -->
- **Alumno/a:**  Aarón Gallardo Canto
- **Repositorio:** https://github.com/IES-Rafael-Alberti/2526-u8u9-9-1-proyectolibre-aaron050223

## 1. Resumen del proyecto

- **Problema que resuelve:** Programa para gestionar la reserva y reseñas de las pistas deportivdas de un pabellón.
- **Usuarios principales:** Público general que busca reservar una pista para jugar a cualquiera de los deportes disponibles y, si lo deseara, realizar alguna reseña para favorecer a la mejora de las instalaciones.
- **Funcionalidades principales:**
  - Realizar / eliminar / ver reservas.
  - Realizar / eliminar / ver reseñas.
- **Entidades principales:** 
  - Reserva: una reserva de pista para una fecha, turno y usuario.
  - Pista: catálogo/diccionario de pistas (id + deporte) usado por las reservas.
  - Reseña (Resena): valoración (nota + descripción) asociada a una reserva pasada. 
- **Estructura del proyecto:**
  - raíz: punto de entrada de la aplicación (Main.kt), configura logs, inicializa MongoDB/H2 y lanza el menú.
  - ui: interfaz de usuario por consola (MenuTerminal), menús y validación de entrada.
  - service: lógica de negocio (PabellonService), reglas y coordinación entre persistencias.
  - model: entidades del dominio (Reserva, Pista, Resena).
  - dao: interfaces DAO (contratos) para acceso a datos (ReservaDAO, PistaDAO, ResenaDAO).
  - dao.bdd: implementación SQL/H2 de DAOs (ReservaDAOH2, PistaDAOH2).
  - dao.mongo: implementación MongoDB Atlas (ResenaDAOMongo).
  - dao.memory: implementación en memoria para pruebas (ReservaDAOMemory).
  - config: configuración e inicialización de la base de datos H2 (DatabaseManager).

## 2. Instalación y ejecución

- **Requisitos previos:**
  - JDK 21.
  - Conexión a Internet para acceder a MongoDB Atlas.
  - Un clúster de MongoDB Atlas.
- **Configuración necesaria:**
  - Configurar la URI de MongoDB Atlas en src/main/kotlin/Main.kt (variable mongoUri).
  - La BD relacional es H2 en fichero local (jdbc:h2:./db/pabellon): se crea automáticamente al ejecutar.
  - La app crea/usa:
    - Carpeta db/ para H2.
    - Carpeta logs/ y fichero logs/mongo.log para logs de Mongo.
    - En Atlas: BD pabellon y colección resenas.
- **Datos de prueba incluidos:**
  - En H2 se cargan automáticamente las pistas iniciales (pistas: 1 Fútbol, 2 Baloncesto, 3 Pádel, 4 Fútbol Sala).

## 3. Diseño y modelo

- **Clases principales:**
  - MenuTerminal (src/main/kotlin/ui/MenuTerminal.kt) -> Interfaz por consola: menús, entradas y mostrar listados.
  - PabellonService (src/main/kotlin/service/PabellonService.kt) -> Lógica de negocio: valida reglas (no duplicar turnos, reseñas solo de reservas pasadas, rangos de nota/descripcion).
  - DatabaseManager (src/main/kotlin/config/DatabaseManager.kt) -> Configura y crea las tablas en H2.
  - ReservaDAOH2 / PistaDAOH2 (src/main/kotlin/dao/bdd/*) -> Acceso H2 para reservas y pistas.
  - ResenaDAOMongo (src/main/kotlin/dao/mongo/ResenaDAOMongo.kt) -> Acceso a MongoDB Atlas para reseñas.
  - Modelos: Reserva, Pista, Resena (src/main/kotlin/model/*).
- **Relaciones importantes:**
  - Interfaces + polimorfismo (DAO):
    - ReservaDAO, PistaDAO, ResenaDAO (interfaces) con implementaciones concretas en dao/bdd, dao/mongo, dao/memory.
  - Composición / inyección de dependencias:
    - MenuTerminal contiene un PabellonService.
    - PabellonService contiene ReservaDAO, PistaDAO, ResenaDAO.
- **Colecciones usadas:**
  - List:
    - Para listados de reservas/pistas/reseñas (DAOs devuelven listas).
  - Map:
    - MenuTerminal: horariosTurnos: Map<Int, String> para mostrar los turnos.
    - PabellonService.obtenerMapaPistas(): Map<Int, String> para traducir idPista -> deporte desde la tabla pistas.
  - Set:
    - En PabellonService.obtenerReservasPasadasSinResena(): Set<Int> con reservaId ya reseñadas para filtrar.
- **Principios SOLID aplicados:**
  - SRP (Single Responsibility Principle):
    - MenuTerminal solo gestiona UI/entrada-salida.
    - PabellonService concentra reglas de negocio.
    - DAOs se encargan solo de persistencia.
  - DIP (Dependency Inversion Principle):
    - PabellonService depende de interfaces (ReservaDAO, PistaDAO, ResenaDAO) y no de implementaciones concretas; permite cambiar H2/Mongo/memoria sin reescribir la lógica.
- **Patrones de diseño:**
  - DAO (Data Access Object):
    - ReservaDAO/PistaDAO/ResenaDAO separan el acceso a datos de la lógica de negocio.
  - Arquitectura por capas (UI -> Service -> DAO/Model):

## 4. Persistencia

### Ficheros

- **Ficheros usados:**
  - logs/mongo.log (ruta: ./logs/mongo.log)
- **Formato y contenido:**
  - Texto plano (.log), líneas de log generadas por el driver de MongoDB.
- **Lectura/escritura:**
  - Escritura: se vuelcan los logs del driver a ese fichero para evitar que salgan por terminal.
- **Clase responsable:**
  - Main.kt (src/main/kotlin/Main.kt), crea la carpeta logs/ con File("logs").mkdirs()
  - https://github.com/IES-Rafael-Alberti/2526-u8u9-9-1-proyectolibre-aaron050223/blob/3530076bb32a32d640f8ddbbda48e1ae1186c1ae/src/main/kotlin/Main.kt#L15-L17
- **Errores controlados:**
  - Si no se puede crear la carpeta o escribir el fichero, el programa puede seguir funcionando.

### MongoDB

- **Base de datos:** pabellon
- **Colecciones:**
  - resenas: almacena las reseñas asociadas a reservas pasadas (1 reseña por reserva, asegurado con índice único en reservaId).
- **Documento de ejemplo:**

```json
{
  "_id": {
    "$oid": "6a1dad399afe196779e84196"
  },
  "reservaId": 3,
  "nota": 3.5,
  "descripcion": "Muy bien pero la pista tenia zonas donde el parqué no estaba del todo bien puesto.",
  "createdAt": {
    "$numberLong": "1780329785881"
  }
}
```

- **Operaciones realizadas:**
  - Insertar: crear reseña.
  - Consultar: listar reseñas / buscar por reservaId.
  - Borrar: eliminar reseña por reservaId.
- **Clase responsable:**
  - ResenaDAOMongo (src/main/kotlin/dao/mongo/ResenaDAOMongo.kt)
    https://github.com/IES-Rafael-Alberti/2526-u8u9-9-1-proyectolibre-aaron050223/blob/3530076bb32a32d640f8ddbbda48e1ae1186c1ae/src/main/kotlin/dao/mongo/ResenaDAOMongo.kt#L13-L61

### Base de datos relacional

- **SGBD utilizado:** H2
- **Tablas y relaciones:** 
  - pistas(id, deporte)
  - reservas(id, id_pista, fecha, turno, usuario)
  - Relación: reservas.id_pista referencia a pistas.id
- **Operaciones CRUD:**
  - Create
  - Read
  - Delete
- **Consultas parametrizadas:**
  - ReservaDAOH2.buscarPorPistaYFecha (WHERE id_pista = ? AND fecha = ?)
    https://github.com/IES-Rafael-Alberti/2526-u8u9-9-1-proyectolibre-aaron050223/blob/3530076bb32a32d640f8ddbbda48e1ae1186c1ae/src/main/kotlin/dao/bdd/ReservaDAOH2.kt#L31-L61
- **Gestión de conexión y cierre:**
  - En DatabaseManager se usa `use{}` para cerrar automáticamente.
    https://github.com/IES-Rafael-Alberti/2526-u8u9-9-1-proyectolibre-aaron050223/blob/3530076bb32a32d640f8ddbbda48e1ae1186c1ae/src/main/kotlin/config/DatabaseManager.kt#L51-L57

## 5. Validaciones y errores

- **Expresiones regulares:** <!-- Dato, regex, ejemplo válido/no válido, enlace -->
- **Excepciones controladas:** <!-- Tipo de error y respuesta del programa -->
- **Excepciones propias:** <!-- Si existen, indicar clase y motivo -->

## 6. Pruebas y evidencias

- **Pruebas realizadas:** <!-- Manuales o automatizadas -->
- **Datos de prueba:** <!-- Qué datos se usaron -->
- **Evidencia de ejecución:** <!-- Salida de consola o captura -->
- **Evidencia de ficheros:** <!-- Fichero generado/leído -->
- **Evidencia de MongoDB:** <!-- Inserción/consulta -->
- **Evidencia de SQL:** <!-- CRUD realizado -->

## 7. Refactorización, documentación y Git

- **Refactorizaciones aplicadas:** <!-- Qué se mejoró y por qué -->
- **Código limpio:** <!-- Ejemplos concretos -->
- **Documentación:** <!-- KDoc, Dokka, README, diagramas... -->
- **Control de versiones:** <!-- Commits, ramas, conflictos si los hubo -->

## 8. Problemas encontrados y soluciones

| Problema | Solución aplicada | Enlace o evidencia |
|----------|-------------------|--------------------|
| <!-- Problema --> | <!-- Solución --> | <!-- Enlace --> |

## 9. Respuestas a los criterios de evaluación

Completa cada criterio con una respuesta breve (Por ejemplo, si habla de clases puedes listar las mas importantes, y entrar en detalle en alguna), técnica y con enlaces al código.

### 9.1. Diseño general

<!-- Temática, problema, entidades, funcionalidades, estructura y justificación. -->

### 9.2. Clases y objetos

<!-- Clases, propiedades, métodos, constructores, objetos instanciados y enlaces al código. -->

### 9.3. Encapsulación y visibilidad

<!-- Propiedades públicas/privadas, validaciones, métodos de modificación y decisiones. -->

### 9.4. Colecciones

<!-- Tipo de colección, información almacenada, motivo de elección y enlace al código. -->

### 9.5. Genéricos

<!-- Elemento genérico creado, problema que resuelve, ventaja y enlace al código. -->

### 9.6. Herencia, interfaces o clases abstractas

<!-- Relación entre clases/interfaces, ventaja, polimorfismo si existe y enlace al código. -->

### 9.7. Expresiones regulares

<!-- Dato validado, expresión regular, ejemplo válido, ejemplo no válido y enlace al código. -->

### 9.8. Ficheros

<!-- Ficheros, operaciones de lectura/escritura, formato, errores controlados y enlace al código. -->

### 9.9. MongoDB

<!-- Base de datos, colecciones, documentos, operaciones realizadas y enlace al código. -->

### 9.10. Base de datos relacional

<!-- SGBD, tablas, relaciones, script SQL, CRUD, conexión, cierre de recursos, consultas parametrizadas y enlace al código. -->

### 9.11. Excepciones

<!-- Errores controlados, excepciones propias, comportamiento ante error, ejemplos y enlace al código. -->

### 9.12. SOLID y buenas prácticas

<!-- Principios aplicados, clases donde aparecen, problema que evitan, mejora aportada y enlace al código. -->

### 9.13. Librerías externas

<!-- Nombre, finalidad, configuración, uso en código y motivo. -->

### 9.14. Pruebas y evidencias

<!-- Pruebas, datos, salidas, capturas si procede, ficheros generados, MongoDB y SQL. -->

### 9.15. Refactorización y código limpio

<!-- Técnicas aplicadas, mejoras conseguidas, ejemplos y enlaces. -->

### 9.16. Patrones de diseño

<!-- Patrón aplicado, ubicación, problema que resuelve, ventaja y enlace al código. -->

### 9.17. Documentación

<!-- Herramientas, partes documentadas, formato, ejemplo y enlace. -->

### 9.18. Control de versiones

<!-- Git, commits, ramas, conflictos si existen, repositorio e historial. -->

## 10. Conclusiones

- **Qué he aprendido:** <!-- Resumen -->
- **Qué mejoraría si tuviera más tiempo:** <!-- Mejoras realistas -->
- **Decisión técnica más importante:** <!-- Decisión y motivo -->

## 11. Autoevaluación

Indica en cada criterio el nivel o puntuación que consideras que has alcanzado. Usa la escala de la guía de evaluación: `0`, `2.5`, `5`, `7.5` o `10`. Justifica siempre la puntuación con evidencias concretas: clases, funciones, commits, capturas, documentación o enlaces al código.

### 11.1. Programación

| Criterio | Puntuación/Nivel | Justificación de la puntuación |
|----------|------------------|--------------------------------|
| Completitud de requisitos mínimos | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Justifica el cumplimiento de POO, colecciones, genéricos, herencia/interfaces, regex, excepciones, SOLID, librerías, pruebas y evidencias. --> |
| Acceso a ficheros | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Indica ficheros usados, formato, operaciones de lectura/escritura, clase responsable y control de errores. --> |
| Integración de MongoDB | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Indica base de datos, colecciones, documentos, operaciones y clase responsable. --> |
| Base de datos relacional y operaciones CRUD | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Indica SGBD, tablas, relaciones, script SQL, CRUD, conexión, cierre de recursos y consultas parametrizadas. --> |
| Preguntas de evaluación de Programación | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Justifica si las respuestas de Programación están completas, son técnicas e incluyen enlaces y evidencias. --> |

### 11.2. Entornos de Desarrollo

| Criterio | Puntuación/Nivel | Justificación de la puntuación |
|----------|------------------|--------------------------------|
| Refactorización y código limpio | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Refactorizaciones, técnicas aplicadas, mejoras y ejemplos. --> |
| Patrones de diseño | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Patrón usado, ubicación, problema resuelto y ventaja. --> |
| Documentación | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Herramientas, partes documentadas, formato y ejemplo. --> |
| Control de versiones | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Commits, ramas, repositorio, conflictos si existen e historial. --> |
| Preguntas de evaluación de Entornos de Desarrollo | <!-- 0 / 2.5 / 5 / 7.5 / 10 --> | <!-- Justifica si las respuestas de Entornos están completas, son técnicas e incluyen enlaces y evidencias. --> |
