package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Levantamento
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LevantamentoService {
    // GET: Buscar levantamento por número de série
    @GET("levantamentos/{id}")
    suspend fun getLevantamento(@Path("id") id: String): Response<Levantamento>

    // POST: Criar novo levantamento
    @POST("levantamentos/")
    suspend fun createLevantamento(@Body newLevantamento: Levantamento): Response<Levantamento>
}