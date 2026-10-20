package com.ranger.popabus.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

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
data class ParaderoRespuesta(
    val id: String = "",
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val geohash: String = "",
    val estado: String = "activo"
)
data class UsuarioRespuesta(
    val uid: String,
    val correo: String,
    val nombre_completo: String,
    val rol: String,
    val activo: Boolean,
    val fecha_registro: Long
)
data class IncidenciaRespuesta(
    val id: String = "",
    val id_usuario: String,
    val descripcion: String,
    val estado: String = "pendiente",
    val fecha_reporte: Long,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
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
    @GET("/api/buses")
    suspend fun listarBuses(): Response<List<BusRespuesta>>

    @POST("/api/buses")
    suspend fun crearBus(@Body bus: BusRespuesta): Response<BusRespuesta>

    @DELETE("/api/buses/{id}")
    suspend fun eliminarBus(@Path("id") id: String): Response<Map<String, String>>
    @GET("/api/paraderos")
    suspend fun listarParaderos(): Response<List<ParaderoRespuesta>>

    @POST("/api/paraderos")
    suspend fun crearParadero(@Body paradero: ParaderoRespuesta): Response<Map<String, String>>

    @DELETE("/api/paraderos/{id}")
    suspend fun eliminarParadero(@Path("id") id: String): Response<Map<String, String>>
    @GET("/api/usuarios")
    suspend fun listarUsuarios(): Response<List<UsuarioRespuesta>>

    @PUT("/api/usuarios/{uid}/activo")
    suspend fun cambiarEstadoUsuario(@Path("uid") uid: String, @Body request: Map<String, Boolean>): Response<Map<String, String>>

    @PUT("/api/usuarios/{uid}/rol")
    suspend fun cambiarRolUsuario(@Path("uid") uid: String, @Body request: Map<String, String>): Response<Map<String, String>>

    @GET("/api/incidencias")
    suspend fun listarIncidencias(): Response<List<IncidenciaRespuesta>>

    @POST("/api/incidencias")
    suspend fun crearIncidencia(@Body incidencia: IncidenciaRespuesta): Response<Map<String, String>>
}