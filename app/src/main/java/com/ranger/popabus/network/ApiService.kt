package com.ranger.popabus.network

import retrofit2.Response
import retrofit2.http.GET

data class MensajeRespuesta(
    val status: String,
    val mensaje: String,
    val autor: String
)

interface ApiService {
    @GET("/")
    suspend fun probarConexion(): Response<MensajeRespuesta>
}
