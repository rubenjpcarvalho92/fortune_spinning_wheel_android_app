package com.example.fortune_whell_v3.api.services

import com.example.fortune_whell_v3.api.models.Admin
import retrofit2.Call // Import correto para Retrofit
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminService {
    // GET: Obter informações
    @GET("admins/{id}")
    suspend fun getAdmin(@Path("id") id: Int): Response<Admin>

    // POST: Criar um novo recurso
    @POST("admins")
    suspend fun createAdmin(@Body newAdmin: Admin): Response<Admin>

    // PUT: Atualizar um recurso completo
    @PUT("admins/{id}")
    suspend fun updateAdmin(@Path("id") id: Int, @Body updateAdmin: Admin): Response<Admin>
}