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

// --- MODELOS BÁSICOS ---
@Serializable
data class MensajeRespuesta(
    val status: String,
    val mensaje: String,
    val autor: String
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
    }
}