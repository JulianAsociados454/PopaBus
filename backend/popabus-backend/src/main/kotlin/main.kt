package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    // ⬇️ Inicializa la base de datos ANTES de arrancar el servidor
    initDatabase()
    val puerto = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}