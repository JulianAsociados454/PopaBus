package com.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.timestamp

// --- DEFINICIÓN DE TABLAS ---

object Usuarios : Table("usuarios") {
    val uid = varchar("uid", 100)
    val correo = varchar("correo", 200).uniqueIndex()
    val contrasena = varchar("contrasena", 200)
    val nombreCompleto = varchar("nombre_completo", 200)
    val rol = varchar("rol", 50)
    val activo = bool("activo").default(true)
    val fechaRegistro = timestamp("fecha_registro").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(uid)
}

object Buses : Table("buses") {
    val id = varchar("id", 100)
    val placa = varchar("placa", 50).uniqueIndex()
    val modelo = varchar("modelo", 100)
    val idRuta = varchar("id_ruta", 100).default("")
    val idConductorActual = varchar("id_conductor_actual", 100).default("")
    val estado = varchar("estado", 50).default("activo")

    override val primaryKey = PrimaryKey(id)
}

object Ubicaciones : Table("ubicaciones") {
    val idBus = varchar("id_bus", 100)
    val idRecorrido = varchar("id_recorrido", 100).default("")
    val latitud = double("latitud")
    val longitud = double("longitud")
    val geohash = varchar("geohash", 20)
    val velocidad = double("velocidad").default(0.0)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(idBus)
}

// --- CONEXIÓN E INICIALIZACIÓN ---

fun initDatabase() {
    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/popabus"
    val dbUser = System.getenv("DATABASE_USER") ?: "popabus_user"
    val dbPass = System.getenv("DATABASE_PASSWORD") ?: "popabus_pass"

    println("🗄️  Conectando a la base de datos: $dbUrl")

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPass
    )

    transaction {
        SchemaUtils.create(Usuarios, Buses, Ubicaciones)
    }

    println("✅ Base de datos inicializada correctamente")
}