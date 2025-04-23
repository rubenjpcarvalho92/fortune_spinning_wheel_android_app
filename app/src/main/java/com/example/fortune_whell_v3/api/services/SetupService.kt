package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Setup
import retrofit2.Call // Import correto para Retrofit
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SetupService {
    // GET: Obter informações
    @GET("setups/{id}")
    suspend fun getSetup(@Path("id") id: String): Response<Setup>

    // POST: Criar um novo recurso
    @POST(" setups/")
    suspend fun createSetup(@Body newSetups: Setup): Response<Setup>

    // PUT: Atualizar um recurso completo
    @PUT("setups/{id}")
    suspend fun updateSetup(@Path("id") id: String, @Body updateSetups: Setup): Response<Setup>
}