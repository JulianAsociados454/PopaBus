package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class MensajeRespuesta(
    val status: String,
    val mensaje: String,
    val autor: String
)
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

// --- MODELOS PARA LOGIN ---
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

// Usuarios de prueba (mientras conectamos Firebase Admin)
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

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        // Endpoint de prueba original
        get("/") {
            call.respond(
                MensajeRespuesta(
                    status = "ok",
                    mensaje = "PopaBus Backend funcionando",
                    autor = "Ranger Dev"
                )
            )
        }

        // --- NUEVO: Login ---
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
        // --- NUEVO: Registro ---
        post("/api/auth/register") {
            val request = call.receive<RegisterRequest>()

            // Verificar si el correo ya existe
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

            // Crear el nuevo usuario (en memoria por ahora)
            val nuevoUid = "uid_${System.currentTimeMillis()}"
            val nuevoUsuario = UsuarioEnMemoria(
                correo = request.correo,
                contrasena = request.contrasena,
                rol = request.rol,
                nombre = request.nombre_completo,
                uid = nuevoUid
            )

            // ⚠️ Esto no persiste entre reinicios, es solo para probar el flujo
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
    }
}