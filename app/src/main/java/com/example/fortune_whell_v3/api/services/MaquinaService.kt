package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Maquina
import retrofit2.Call // Import correto para Retrofit
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MaquinaService {
    // GET: Obter informações
    @GET("maquinas/{id}")
    suspend fun getMaquina(@Path("id") id: String): Response<Maquina>

    // POST: Criar um novo recurso
    @POST("maquinas/")
    suspend fun createMaquina(@Body newMaquina: Maquina): Response<Maquina>

    // PUT: Atualizar um recurso completo
    @PUT("maquinas/{id}")
    suspend fun updateMaquina(@Path("id") id: String, @Body updateMaquina: Maquina): Response<Maquina>
}