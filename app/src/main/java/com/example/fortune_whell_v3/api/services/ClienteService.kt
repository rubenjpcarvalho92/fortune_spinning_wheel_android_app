package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Cliente
import retrofit2.Call // Import correto para Retrofit
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ClienteService {
    // GET: Obter informações
    @GET("clientes/{id}")
    suspend fun getCliente(@Path("id") id: Int): Response<Cliente>

    // POST: Criar um novo recurso
    @POST("clientes")
    suspend fun createCliente(@Body newCliente: Cliente): Response<Cliente>

    // PUT: Atualizar um recurso completo
    @PUT("clientes/{id}")
    suspend fun updateCliente(@Path("id") id: Int, @Body updateCliente: Cliente): Response<Cliente>
}
