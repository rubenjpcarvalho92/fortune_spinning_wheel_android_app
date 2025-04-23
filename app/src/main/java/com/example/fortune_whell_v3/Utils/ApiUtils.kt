package com.example.fortune_whell_v3.Utils

import android.util.Log
import retrofit2.Response

suspend fun <T> executeApiCall(
    tag: String = "API",
    call: suspend () -> Response<T>
): T? {
    return try {
        val response = call()
        if (response.isSuccessful) {
            Log.d(tag, "✅ Sucesso: ${response.body()}")
            response.body()
        } else {
            Log.e(tag, "❌ Erro ${response.code()}: ${response.errorBody()?.string()}")
            null
        }
    } catch (e: Exception) {
        Log.e(tag, "🚨 Exceção na API: ${e.message}")
        null
    }
}