package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

// --- MODELOS BÁSICOS ---
@Serializable
data class MensajeRespuesta(
    val status: String,
    val mensaje: String,
    val autor: String
)
@Serializable
data class ActividadRespuesta(
    val id: String = "",
    val id_usuario: String,
    val id_bus: String,
    val id_ruta: String,
    val fecha_abordaje: Long,
    val metodo_registro: String = "manual"
)

// --- MODELOS DE LOGIN ---
@Serializable
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

@Serializable
data class LoginResponse(
    val status: String,
    val mensaje: String,
    val rol: String = "",
    val uid: String = "",
    val nombre_completo: String = ""
)

// --- MODELOS DE REGISTRO ---
@Serializable
data class RegisterRequest(
    val correo: String,
    val contrasena: String,
    val nombre_completo: String,
    val rol: String
)

@Serializable
data class RegisterResponse(
    val status: String,
    val mensaje: String,
    val uid: String = "",
    val rol: String = "",
    val nombre_completo: String = ""
)

// --- MODELOS DE UBICACIÓN ---
@Serializable
data class UbicacionRequest(
    val id_bus: String,
    val id_recorrido: String,
    val latitud: Double,
    val longitud: Double,
    val geohash: String,
    val velocidad: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class UbicacionResponse(
    val status: String,
    val mensaje: String
)

// --- MODELOS DE BUSES ---
@Serializable
data class BusRespuesta(
    val id: String,
    val placa: String,
    val modelo: String,
    val id_ruta: String,
    val id_conductor_actual: String,
    val estado: String = "activo"
)

@Serializable
data class BusConductorResponse(
    val status: String,
    val mensaje: String,
    val bus: BusRespuesta? = null
)

@Serializable
data class BusActivo(
    val id_bus: String,
    val latitud: Double,
    val longitud: Double,
    val geohash: String,
    val velocidad: Double = 0.0,
    val timestamp: Long = 0L
)

@Serializable
data class BusesActivosResponse(
    val status: String,
    val mensaje: String,
    val buses: List<BusActivo>
)
@Serializable
data class ParaderoRespuesta(
    val id: String = "",
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val geohash: String = "",
    val estado: String = "activo"
)
@Serializable
data class UsuarioRespuesta(
    val uid: String,
    val correo: String,
    val nombre_completo: String,
    val rol: String,
    val activo: Boolean,
    val fecha_registro: Long
)
@Serializable
data class IncidenciaRespuesta(
    val id: String = "",
    val id_usuario: String,
    val descripcion: String,
    val estado: String = "pendiente",
    val fecha_reporte: Long,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)
@Serializable
data class RutaRespuesta(
    val id: String = "",
    val nombre_ruta: String,
    val origen: String,
    val destino: String,
    val estado: String = "activa",
    val geometria: String = "[]"
)

// --- SEED: Crear los usuarios de prueba al arrancar ---
fun seedUsuarios() {
    transaction {
        val existentes = Usuarios.selectAll().count()
        if (existentes == 0L) {
            Usuarios.insert {
                it[uid] = "uid_admin_001"
                it[correo] = "admin@popabus.com"
                it[contrasena] = "admin123"
                it[nombreCompleto] = "Administrador Principal"
                it[rol] = "administrador"
                it[activo] = true
            }
            Usuarios.insert {
                it[uid] = "uid_conductor_001"
                it[correo] = "conductor@popabus.com"
                it[contrasena] = "conductor123"
                it[nombreCompleto] = "Juan Conductor"
                it[rol] = "conductor"
                it[activo] = true
            }
            Usuarios.insert {
                it[uid] = "uid_pasajero_001"
                it[correo] = "pasajero@popabus.com"
                it[contrasena] = "pasajero123"
                it[nombreCompleto] = "Pedro Pasajero"
                it[rol] = "pasajero"
                it[activo] = true
            }
            println("🌱 Usuarios de prueba creados en la base de datos")
        } else {
            println("👥 Ya existen $existentes usuarios en la base de datos")
        }
    }
}

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    // Crea los usuarios de prueba al arrancar (solo la primera vez)
    seedUsuarios()

    routing {
        // --- Endpoint de prueba ---
        get("/") {
            call.respond(
                MensajeRespuesta(
                    status = "ok",
                    mensaje = "PopaBus Backend funcionando",
                    autor = "Ranger Dev"
                )
            )
        }

        // --- Login ---
        post("/api/auth/login") {
            val request = call.receive<LoginRequest>()

            val usuario = transaction {
                Usuarios.selectAll()
                    .where { Usuarios.correo eq request.correo }
                    .firstOrNull()
            }

            if (usuario != null && usuario[Usuarios.contrasena] == request.contrasena) {
                if (!usuario[Usuarios.activo]) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        LoginResponse(
                            status = "error",
                            mensaje = "Cuenta suspendida"
                        )
                    )
                    return@post
                }

                call.respond(
                    LoginResponse(
                        status = "ok",
                        mensaje = "Login exitoso",
                        rol = usuario[Usuarios.rol],
                        uid = usuario[Usuarios.uid],
                        nombre_completo = usuario[Usuarios.nombreCompleto]
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    LoginResponse(
                        status = "error",
                        mensaje = "Correo o contraseña incorrectos"
                    )
                )
            }
        }

        // --- Registro ---
        post("/api/auth/register") {
            val request = call.receive<RegisterRequest>()

            val yaExiste = transaction {
                Usuarios.selectAll()
                    .where { Usuarios.correo eq request.correo }
                    .any()
            }

            if (yaExiste) {
                call.respond(
                    HttpStatusCode.Conflict,
                    RegisterResponse(
                        status = "error",
                        mensaje = "Este correo ya está registrado"
                    )
                )
                return@post
            }

            val nuevoUid = "uid_${System.currentTimeMillis()}"

            transaction {
                Usuarios.insert {
                    it[uid] = nuevoUid
                    it[correo] = request.correo
                    it[contrasena] = request.contrasena
                    it[nombreCompleto] = request.nombre_completo
                    it[rol] = request.rol
                    it[activo] = true
                }
            }

            println("✅ Nuevo usuario registrado en BD: ${request.nombre_completo} (${request.correo})")

            call.respond(
                RegisterResponse(
                    status = "ok",
                    mensaje = "Usuario registrado exitosamente",
                    uid = nuevoUid,
                    rol = request.rol,
                    nombre_completo = request.nombre_completo
                )
            )
        }

        // --- Recibir ubicación (conductor) ---
        post("/api/ubicacion") {
            val request = call.receive<UbicacionRequest>()

            // Guarda o actualiza la ubicación en la BD
            transaction {
                val existe = Ubicaciones.selectAll()
                    .where { Ubicaciones.idBus eq request.id_bus }
                    .any()

                if (existe) {
                    Ubicaciones.update({ Ubicaciones.idBus eq request.id_bus }) {
                        it[idRecorrido] = request.id_recorrido
                        it[latitud] = request.latitud
                        it[longitud] = request.longitud
                        it[geohash] = request.geohash
                        it[velocidad] = request.velocidad
                        it[timestamp] = request.timestamp
                    }
                } else {
                    Ubicaciones.insert {
                        it[idBus] = request.id_bus
                        it[idRecorrido] = request.id_recorrido
                        it[latitud] = request.latitud
                        it[longitud] = request.longitud
                        it[geohash] = request.geohash
                        it[velocidad] = request.velocidad
                        it[timestamp] = request.timestamp
                    }
                }
            }

            println("📍 Ubicación guardada en BD: Bus ${request.id_bus} → ${request.latitud}, ${request.longitud}")

            call.respond(
                UbicacionResponse(
                    status = "ok",
                    mensaje = "Ubicación registrada"
                )
            )
        }

        // --- Bus asignado a un conductor ---
        get("/api/buses/conductor/{uid}") {
            val uid = call.parameters["uid"] ?: ""

            // Por ahora seguimos usando el bus de prueba en memoria
            val busEncontrado = if (uid == "uid_conductor_001") {
                BusRespuesta(
                    id = "bus_test_001",
                    placa = "ABC-123",
                    modelo = "2024",
                    id_ruta = "ruta_test_001",
                    id_conductor_actual = "uid_conductor_001",
                    estado = "activo"
                )
            } else null

            if (busEncontrado != null) {
                call.respond(
                    BusConductorResponse(
                        status = "ok",
                        mensaje = "Bus encontrado",
                        bus = busEncontrado
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    BusConductorResponse(
                        status = "error",
                        mensaje = "No tienes ningún bus asignado"
                    )
                )
            }
        }

        // --- Buses activos (para el pasajero) ---
        get("/api/buses/activos") {
            val listaBuses = transaction {
                Ubicaciones.selectAll().map { fila ->
                    BusActivo(
                        id_bus = fila[Ubicaciones.idBus],
                        latitud = fila[Ubicaciones.latitud],
                        longitud = fila[Ubicaciones.longitud],
                        geohash = fila[Ubicaciones.geohash],
                        velocidad = fila[Ubicaciones.velocidad],
                        timestamp = fila[Ubicaciones.timestamp]
                    )
                }
            }

            println("🚌 Pasajero consultó buses activos: ${listaBuses.size} encontrados")

            call.respond(
                BusesActivosResponse(
                    status = "ok",
                    mensaje = "Buses activos",
                    buses = listaBuses
                )
            )
        }
        // --- LISTAR TODOS LOS BUSES ---
        get("/api/buses") {
            val listaBuses = transaction {
                Buses.selectAll().map { fila ->
                    BusRespuesta(
                        id = fila[Buses.id],
                        placa = fila[Buses.placa],
                        modelo = fila[Buses.modelo],
                        id_ruta = fila[Buses.idRuta],
                        id_conductor_actual = fila[Buses.idConductorActual],
                        estado = fila[Buses.estado]
                    )
                }
            }
            println("🚌 Listando ${listaBuses.size} buses")
            call.respond(listaBuses)
        }

        // --- CREAR BUS ---
        post("/api/buses") {
            val request = call.receive<BusRespuesta>()

            val nuevoId = "bus_${System.currentTimeMillis()}"
            val busCreado = transaction {
                Buses.insert {
                    it[id] = nuevoId
                    it[placa] = request.placa
                    it[modelo] = request.modelo
                    it[idRuta] = request.id_ruta
                    it[idConductorActual] = request.id_conductor_actual
                    it[estado] = "activo"
                }
                BusRespuesta(
                    id = nuevoId,
                    placa = request.placa,
                    modelo = request.modelo,
                    id_ruta = request.id_ruta,
                    id_conductor_actual = request.id_conductor_actual,
                    estado = "activo"
                )
            }

            println("✅ Bus creado: ${busCreado.placa} (ID: $nuevoId)")
            call.respond(HttpStatusCode.Created, busCreado)
        }

        // --- ELIMINAR BUS ---
        delete("/api/buses/{id}") {
            val id = call.parameters["id"] ?: ""
            val eliminado = transaction {
                Buses.deleteWhere { Buses.id eq id }
            }
            if (eliminado > 0) {
                println("🗑️ Bus eliminado: $id")
                call.respond(mapOf("status" to "ok", "mensaje" to "Bus eliminado"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("status" to "error", "mensaje" to "Bus no encontrado"))
            }
        }
        // --- ENDPOINTS DE PARADEROS ---
        get("/api/paraderos") {
            val lista = transaction {
                Paraderos.selectAll().map { fila ->
                    ParaderoRespuesta(
                        id = fila[Paraderos.id],
                        nombre = fila[Paraderos.nombre],
                        direccion = fila[Paraderos.direccion],
                        latitud = fila[Paraderos.latitud],
                        longitud = fila[Paraderos.longitud],
                        geohash = fila[Paraderos.geohash],
                        estado = fila[Paraderos.estado]
                    )
                }
            }
            call.respond(lista)
        }

        post("/api/paraderos") {
            val request = call.receive<ParaderoRespuesta>()
            val nuevoId = "par_${System.currentTimeMillis()}"

            transaction {
                Paraderos.insert {
                    it[id] = nuevoId
                    it[nombre] = request.nombre
                    it[direccion] = request.direccion
                    it[latitud] = request.latitud
                    it[longitud] = request.longitud
                    it[geohash] = request.geohash
                    it[estado] = "activo"
                }
            }
            println("📍 Paradero creado: ${request.nombre}")
            call.respond(HttpStatusCode.Created, mapOf("status" to "ok", "id" to nuevoId))
        }

        delete("/api/paraderos/{id}") {
            val id = call.parameters["id"] ?: ""
            val eliminado = transaction { Paraderos.deleteWhere { Paraderos.id eq id } }
            if (eliminado > 0) call.respond(mapOf("status" to "ok"))
            else call.respond(HttpStatusCode.NotFound, mapOf("status" to "error"))
        }
        // --- ENDPOINTS DE USUARIOS (GESTIÓN HUMANA) ---
        get("/api/usuarios") {
            val lista = transaction {
                Usuarios.selectAll().map { fila ->
                    UsuarioRespuesta(
                        uid = fila[Usuarios.uid],
                        correo = fila[Usuarios.correo],
                        nombre_completo = fila[Usuarios.nombreCompleto],
                        rol = fila[Usuarios.rol],
                        activo = fila[Usuarios.activo],
                        fecha_registro = fila[Usuarios.fechaRegistro].toEpochMilli()
                    )
                }
            }
            call.respond(lista)
        }

        put("/api/usuarios/{uid}/activo") {
            val uid = call.parameters["uid"] ?: ""
            val request = call.receive<Map<String, Boolean>>()
            val nuevoEstado = request["activo"] ?: true

            val actualizado = transaction {
                Usuarios.update({ Usuarios.uid eq uid }) {
                    it[activo] = nuevoEstado
                }
            }
            if (actualizado > 0) call.respond(mapOf("status" to "ok"))
            else call.respond(HttpStatusCode.NotFound, mapOf("status" to "error"))
        }

        put("/api/usuarios/{uid}/rol") {
            val uid = call.parameters["uid"] ?: ""
            val request = call.receive<Map<String, String>>()
            val nuevoRol = request["rol"] ?: "pasajero"

            val actualizado = transaction {
                Usuarios.update({ Usuarios.uid eq uid }) {
                    it[rol] = nuevoRol
                }
            }
            if (actualizado > 0) call.respond(mapOf("status" to "ok"))
            else call.respond(HttpStatusCode.NotFound, mapOf("status" to "error"))
        }
        // --- ENDPOINTS DE INCIDENCIAS ---
        get("/api/incidencias") {
            val lista = transaction {
                Incidencias.selectAll().map { fila ->
                    IncidenciaRespuesta(
                        id = fila[Incidencias.id],
                        id_usuario = fila[Incidencias.id_usuario],
                        descripcion = fila[Incidencias.descripcion],
                        estado = fila[Incidencias.estado],
                        fecha_reporte = fila[Incidencias.fecha_reporte],
                        latitud = fila[Incidencias.latitud],
                        longitud = fila[Incidencias.longitud]
                    )
                }.sortedByDescending { it.fecha_reporte }
            }
            call.respond(lista)
        }

        post("/api/incidencias") {
            val request = call.receive<IncidenciaRespuesta>()
            val nuevoId = "inc_${System.currentTimeMillis()}"

            transaction {
                Incidencias.insert {
                    it[id] = nuevoId
                    it[id_usuario] = request.id_usuario
                    it[descripcion] = request.descripcion
                    it[estado] = request.estado
                    it[fecha_reporte] = request.fecha_reporte
                    it[latitud] = request.latitud
                    it[longitud] = request.longitud
                }
            }
            println("⚠️ Incidencia creada: ${request.descripcion}")
            call.respond(HttpStatusCode.Created, mapOf("status" to "ok", "id" to nuevoId))
        }
        // --- ENDPOINTS DE RUTAS ---
        get("/api/rutas") {
            val lista = transaction {
                Rutas.selectAll().map { fila ->
                    RutaRespuesta(
                        id = fila[Rutas.id],
                        nombre_ruta = fila[Rutas.nombreRuta],
                        origen = fila[Rutas.origen],
                        destino = fila[Rutas.destino],
                        estado = fila[Rutas.estado],
                        geometria = fila[Rutas.geometria]
                    )
                }
            }
            call.respond(lista)
        }

        post("/api/rutas") {
            val request = call.receive<RutaRespuesta>()
            val nuevoId = if (request.id.isNotEmpty()) request.id else "ruta_${System.currentTimeMillis()}"

            transaction {
                // Si la ruta ya existe (migración), la actualizamos. Si no, la insertamos.
                val existe = Rutas.selectAll().where { Rutas.id eq nuevoId }.any()
                if (existe) {
                    Rutas.update({ Rutas.id eq nuevoId }) {
                        it[nombreRuta] = request.nombre_ruta
                        it[origen] = request.origen
                        it[destino] = request.destino
                        it[estado] = request.estado
                        it[geometria] = request.geometria
                    }
                } else {
                    Rutas.insert {
                        it[id] = nuevoId
                        it[nombreRuta] = request.nombre_ruta
                        it[origen] = request.origen
                        it[destino] = request.destino
                        it[estado] = request.estado
                        it[geometria] = request.geometria
                    }
                }
            }
            println("🗺️ Ruta migrada/creada: ${request.nombre_ruta}")
            call.respond(HttpStatusCode.Created, mapOf("status" to "ok", "id" to nuevoId))
        }

        delete("/api/rutas/{id}") {
            val id = call.parameters["id"] ?: ""
            val eliminado = transaction { Rutas.deleteWhere { Rutas.id eq id } }
            if (eliminado > 0) call.respond(mapOf("status" to "ok"))
            else call.respond(HttpStatusCode.NotFound, mapOf("status" to "error"))
        }
        // --- ENDPOINTS DE ACTIVIDADES (ABORDAJE) ---
        post("/api/actividades") {
            val request = call.receive<ActividadRespuesta>()
            val nuevoId = "act_${System.currentTimeMillis()}"

            transaction {
                Actividades.insert {
                    it[id] = nuevoId
                    it[id_usuario] = request.id_usuario
                    it[id_bus] = request.id_bus
                    it[id_ruta] = request.id_ruta
                    it[fecha_abordaje] = request.fecha_abordaje
                    it[metodo_registro] = request.metodo_registro
                }
            }
            println("✅ Abordaje registrado: ${request.id_bus}")
            call.respond(HttpStatusCode.Created, mapOf("status" to "ok", "id" to nuevoId))
        }

        get("/api/actividades/{uid}") {
            val uid = call.parameters["uid"] ?: ""
            val lista = transaction {
                Actividades.selectAll().where { Actividades.id_usuario eq uid }.map { fila ->
                    ActividadRespuesta(
                        id = fila[Actividades.id],
                        id_usuario = fila[Actividades.id_usuario],
                        id_bus = fila[Actividades.id_bus],
                        id_ruta = fila[Actividades.id_ruta],
                        fecha_abordaje = fila[Actividades.fecha_abordaje],
                        metodo_registro = fila[Actividades.metodo_registro]
                    )
                }.sortedByDescending { it.fecha_abordaje }
            }
            call.respond(lista)
        }
    }

}