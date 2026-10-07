package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

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

// --- DATOS EN MEMORIA ---
data class UsuarioEnMemoria(
    val correo: String,
    val contrasena: String,
    val rol: String,
    val nombre: String,
    val uid: String
)

val usuariosDePrueba = listOf(
    UsuarioEnMemoria("admin@popabus.com",      "admin123",     "administrador", "Administrador Principal", "uid_admin_001"),
    UsuarioEnMemoria("conductor@popabus.com",  "conductor123", "conductor",     "Juan Conductor",          "uid_conductor_001"),
    UsuarioEnMemoria("pasajero@popabus.com",   "pasajero123",  "pasajero",      "Pedro Pasajero",          "uid_pasajero_001")
)

val busesDePrueba = listOf(
    BusRespuesta(
        id = "bus_test_001",
        placa = "ABC-123",
        modelo = "2024",
        id_ruta = "ruta_test_001",
        id_conductor_actual = "uid_conductor_001",
        estado = "activo"
    )
)

// ⚠️ Guarda la última ubicación de cada bus (a nivel global del archivo)
val ubicacionesActivas = mutableMapOf<String, UbicacionRequest>()

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

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

            val usuario = usuariosDePrueba.find {
                it.correo == request.correo && it.contrasena == request.contrasena
            }

            if (usuario != null) {
                call.respond(
                    LoginResponse(
                        status = "ok",
                        mensaje = "Login exitoso",
                        rol = usuario.rol,
                        uid = usuario.uid,
                        nombre_completo = usuario.nombre
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

            val yaExiste = usuariosDePrueba.any { it.correo == request.correo }
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
            val nuevoUsuario = UsuarioEnMemoria(
                correo = request.correo,
                contrasena = request.contrasena,
                rol = request.rol,
                nombre = request.nombre_completo,
                uid = nuevoUid
            )

            println("✅ Nuevo usuario registrado: ${nuevoUsuario.nombre} (${nuevoUsuario.correo})")

            call.respond(
                RegisterResponse(
                    status = "ok",
                    mensaje = "Usuario registrado exitosamente",
                    uid = nuevoUid,
                    rol = nuevoUsuario.rol,
                    nombre_completo = nuevoUsuario.nombre
                )
            )
        }

        // --- Recibir ubicación (conductor) ---
        post("/api/ubicacion") {
            val request = call.receive<UbicacionRequest>()

            // ⬇️ Guarda en el Map global
            ubicacionesActivas[request.id_bus] = request

            println(" Ubicación recibida:")
            println("   Bus: ${request.id_bus}")
            println("   Recorrido: ${request.id_recorrido}")
            println("   Lat: ${request.latitud}, Lng: ${request.longitud}")
            println("   Geohash: ${request.geohash}")
            println("   Velocidad: ${request.velocidad} m/s")
            println("    Total buses activos: ${ubicacionesActivas.size}")

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

            val busEncontrado = busesDePrueba.find { it.id_conductor_actual == uid }

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

        // --- NUEVO: Devuelve todos los buses activos (para el pasajero) ---
        get("/api/buses/activos") {
            val listaBuses = ubicacionesActivas.values.map { ubicacion ->
                BusActivo(
                    id_bus = ubicacion.id_bus,
                    latitud = ubicacion.latitud,
                    longitud = ubicacion.longitud,
                    geohash = ubicacion.geohash,
                    velocidad = ubicacion.velocidad,
                    timestamp = ubicacion.timestamp
                )
            }

            println(" Pasajero consultó buses activos: ${listaBuses.size} encontrados")

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