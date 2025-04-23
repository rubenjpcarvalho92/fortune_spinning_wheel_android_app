package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Funcionario
import retrofit2.Call // Import correto para Retrofit
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FuncionarioService {
    // GET: Obter informações
    @GET("funcionarios/{id}")
    suspend fun getFuncionario(@Path("id") id: Int): Response<Funcionario>

    // POST: Criar um novo recurso
    @POST("funcionarios")
    suspend fun createFuncionario(@Body newFuncionario: Funcionario): Response<Funcionario>

    // PUT: Atualizar um recurso completo
    @PUT("funcionarios/{id}")
    suspend fun updateFuncionario(@Path("id") id: Int, @Body updateFuncionario: Funcionario): Response<Funcionario>
}
