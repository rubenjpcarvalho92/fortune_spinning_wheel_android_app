package com.example.fortune_whell_v3.resources

import android.util.Log
import com.example.fortune_whell_v3.api.models.Login
import com.example.fortune_whell_v3.api.services.LoginService
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object LoginResource {

    suspend fun registerLogin(
        nif: Int,
        numeroSerie: String,
        resultado: String,
        nivelAcesso: String = "desconhecido",
        loginService: LoginService
    ): Boolean {
        val login = Login(
            NIF = nif,
            Maquinas_numeroSerie = numeroSerie,
            resultado = resultado,
            dataLogin = getCurrentDateTime(),
            nivelAcesso = nivelAcesso
        )

        Log.d("LoginResource", "📤 Enviando login: $login")

        return try {
            val response: Response<Login> = loginService.createLogin(login)
            if (response.isSuccessful) {
                Log.d("LoginResource", "✅ Login registrado: ${response.body()}")
                true
            } else {
                Log.e("LoginResource", "❌ Erro ao registrar login: ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("LoginResource", "🚫 Falha de conexão ao registrar login: ${e.message}", e)
            false
        }
    }

    private fun getCurrentDateTime(): String {
        val now = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return formatter.format(now)
    }
}
