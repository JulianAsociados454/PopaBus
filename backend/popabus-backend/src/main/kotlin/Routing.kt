package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class MensajeRespuesta(
    val status: String,
    val mensaje: String,
    val autor: String
)

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respond(
                MensajeRespuesta(
                    status = "ok",
                    mensaje = "PopaBus Backend funcionando",
                    autor = "Ranger Dev"
                )
            )
        }
    }
}