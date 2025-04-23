package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Premio
import retrofit2.Response
import retrofit2.http.*

interface PremioService {

    // 🔹 Obter prêmios contabilizados (todos=true) ou não contabilizados (todos=false)
    @GET("premios/")
    suspend fun getPremios(
        @Query("numeroSerie") numeroSerie: String,
        @Query("todos") todos: Boolean // <- EM STRING
    ): Response<List<Premio>>


    // 🔹 Criar um novo prêmio
    @POST("premios/")
    suspend fun createPremio(@Body newPremio: Premio): Response<Premio>

    // 🔹 Atualizar um prêmio
    @PUT("premios/{id}")
    suspend fun updatePremio(@Path("id") id: Int, @Body updatePremio: Premio): Response<Premio>

    // 🔹 Obter prêmio por ID
    @GET("premios/{id}")
    suspend fun getPremio(@Path("id") id: Int): Response<Premio>

    // 🔹 Contabilizar todos os prêmios não contabilizados da máquina
    @PATCH("premios/")
    suspend fun contabilizarPremios(
        @Query("numeroSerie") numeroSerie: String
    ): Response<Unit>
}
