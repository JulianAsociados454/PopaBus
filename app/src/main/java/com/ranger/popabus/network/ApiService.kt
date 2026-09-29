package com.ranger.popabus.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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

interface ApiService {
    @GET("/")
    suspend fun probarConexion(): Response<MensajeRespuesta>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/auth/register")
    suspend fun registrar(@Body request: RegisterRequest): Response<RegisterResponse>
}