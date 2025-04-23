package com.example.fortune_whell_v3.api.services


import com.example.fortune_whell_v3.api.models.Login
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface LoginService {
    // GET: Obter informações
    @GET("logins/{id}")
    suspend fun getLogin(@Path("id") id: Int): Response<Login>

    // POST: Criar um novo recurso
    @POST("logins/")
    suspend fun createLogin(@Body newLogin: Login): Response<Login>

    // PUT: Atualizar um recurso completo
    @PUT("logins/{id}")
    suspend fun updateLogin(@Path("id") id: Int, @Body updatedLogin: Login): Response<Login>
}