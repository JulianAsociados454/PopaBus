package com.ranger.popabus.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class MensajeRespuesta(
    val status: String,
    val mensaje: String,
    val autor: String
)

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class LoginResponse(
    val status: String,
    val mensaje: String,
    val rol: String = "",
    val uid: String = "",
    val nombre_completo: String = ""
)
data class RegisterRequest(
    val correo: String,
    val contrasena: String,
    val nombre_completo: String,
    val rol: String
)

data class RegisterResponse(
    val status: String,
    val mensaje: String,
    val uid: String = "",
    val rol: String = "",
    val nombre_completo: String = ""
)
data class UbicacionRequest(
    val id_bus: String,
    val id_recorrido: String,
    val latitud: Double,
    val longitud: Double,
    val geohash: String,
    val velocidad: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class UbicacionResponse(
    val status: String,
    val mensaje: String
)
data class BusRespuesta(
    val id: String,
    val placa: String,
    val modelo: String,
    val id_ruta: String,
    val id_conductor_actual: String,
    val estado: String = "activo"
)

data class BusConductorResponse(
    val status: String,
    val mensaje: String,
    val bus: BusRespuesta? = null
)
data class BusActivo(
    val id_bus: String,
    val latitud: Double,
    val longitud: Double,
    val geohash: String,
    val velocidad: Double = 0.0,
    val timestamp: Long = 0L
)

data class BusesActivosResponse(
    val status: String,
    val mensaje: String,
    val buses: List<BusActivo>
)

interface ApiService {
    @GET("/")
    suspend fun probarConexion(): Response<MensajeRespuesta>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun registrar(@Body request: RegisterRequest): Response<RegisterResponse>
    @POST("/api/ubicacion")
    suspend fun enviarUbicacion(@Body request: UbicacionRequest): Response<UbicacionResponse>
    @GET("/api/buses/conductor/{uid}")
    suspend fun obtenerBusConductor(@Path("uid") uid: String): Response<BusConductorResponse>
    @GET("/api/buses/activos")
    suspend fun obtenerBusesActivos(): Response<BusesActivosResponse>

}