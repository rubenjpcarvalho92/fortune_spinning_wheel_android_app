package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Talao
import retrofit2.Call // Import correto para Retrofit
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TalaoService {
    // GET: Obter informações
    @GET("taloes/{id}")
    suspend fun getTalao(@Path("id") id: String): Response<Talao>

    // POST: Criar um novo recurso
    @POST("taloes/")
    suspend fun createTalao(@Body newTalao: Talao): Response<Talao>

    // PUT: Atualizar um recurso completo
    @PUT("taloes/{id}")
    suspend fun updateTalao(@Path("id") id: String, @Body updateTalao: Talao): Response<Talao>
}