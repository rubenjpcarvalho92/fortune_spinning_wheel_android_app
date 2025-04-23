package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Stock
import retrofit2.Call // Import correto para Retrofit
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StockService {
    // GET: Obter informações
    @GET("stocks/{id}")
    suspend fun getStock(@Path("id") id: String): Response<Stock>

    // POST: Criar um novo recurso
    @POST("stocks")
    suspend fun createStock(@Body newStock: Stock): Response<Stock>

    // PUT: Atualizar um recurso completo
    @PUT("stocks/{id}")
    suspend fun updateStock(@Path("id") id: String, @Body stock: Stock): Response<Unit>
}