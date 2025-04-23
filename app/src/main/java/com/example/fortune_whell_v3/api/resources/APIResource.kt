package com.example.fortune_whell_v3.api.resources

import android.util.Log
import com.example.fortune_whell_v3.Utils.executeApiCall
import com.example.fortune_whell_v3.api.models.Levantamento
import com.example.fortune_whell_v3.api.models.Maquina
import com.example.fortune_whell_v3.api.models.Premio
import com.example.fortune_whell_v3.api.models.Setup
import com.example.fortune_whell_v3.api.models.Stock
import com.example.fortune_whell_v3.api.models.Talao
import com.example.fortune_whell_v3.api.services.ApiServices
import com.google.gson.Gson
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object APIResource {

    suspend fun registarPremio(labels: List<String>, premios: List<Float>,sliceIndex: Int, talaoSN: String): Premio? {
        val categoriaPremio = premios[sliceIndex]

        val numeroSerie = generateSerialNumberPremio(talaoSN)

        val premio = Premio(
            codigoRoleta = labels[sliceIndex],
            data = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            numeroSerie = numeroSerie,
            categoriaPremio = categoriaPremio,
            contabilizado = 0,
            Taloes_numeroSerie = talaoSN
        )

        Log.d("APITalao", "📤 Enviando requisição para criar talão: $premio")

        return executeApiCall("APIPremio") {
            ApiServices.premioService.createPremio(premio)
        }
    }

    suspend fun actualizaMaquina(maquinaAtualizada: Maquina): Boolean {

        // Log JSON para debug
        val jsonBody = Gson().toJson(maquinaAtualizada)
        Log.d("API Request", "JSON enviado: $jsonBody")

        return try {
            val response: Response<Maquina> =
                ApiServices.maquinaService.updateMaquina(maquinaAtualizada.numeroSerie, maquinaAtualizada)

            Log.d("HTTP Request", "URL: ${response.raw().request.url}")
            Log.d("HTTP Request", "Código: ${response.code()}")
            Log.d("HTTP Request", "Body enviado: $maquinaAtualizada")

            if (response.isSuccessful) {
                Log.d("API Response", "Máquina atualizada com sucesso!")
                true
            } else {
                Log.e("API Response", "Erro ao atualizar máquina: ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("API Request", "Erro de conexão ou exceção: ${e.message}")
            false
        }
    }

    suspend fun confirmarImpressao(talao: Talao){

        Log.d("DEBUG", "🔥 Entrou na função confirmarImpressao")

        val talaoActualizado = try {
            talao.copy(
                dataImpressao = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                impressaoOK = true
            )
        } catch (e: Exception) {
            Log.e("DEBUG", "Erro ao fazer copy do talão: ${e.message}")
            return
        }

        // JSON apenas para debug (opcional)
        val gson = com.google.gson.Gson()
        val jsonBody = gson.toJson(talaoActualizado)
        Log.d("DEBUG", "JSON enviado: $jsonBody")

        try {
            val response = ApiServices.talaoService.updateTalao(talao.numeroSerie, talaoActualizado)
            Log.d("HTTP Request", "Response Code: ${response.code()}")
            if (response.isSuccessful) {
                Log.d("API Response Talao", "Talão atualizado com sucesso!")
            } else {
                Log.e("API Response Talao", "Erro ao atualizar talão: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("API Response Talao", "Erro de conexão: ${e.message}")
        }
    }

    suspend fun resetGanhosMaquina(maquina: Maquina, stock: Stock): Levantamento? {
        val numeroSerie = generateSerialNumberTalaoELevatamento(maquina.numeroSerie)
        val dataAtual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val apostado = maquina.apostadoParcial ?: 0
        val atribuido = maquina.atribuidoParcial
        val taxaGanho = maquina.taxaGanhoParcial?.toDouble() ?: 0.0

        val levantamento = Levantamento(
            numeroSerie = numeroSerie,
            data = dataAtual,
            apostadoParcial = apostado,
            taxaGanhoParcial = taxaGanho,
            atribuidoParcial = atribuido,
            Maquinas_numeroSerie = maquina.numeroSerie,
            VD = stock.VD, PT = stock.PT, CI = stock.CI, AM = stock.AM,
            GM = stock.GM, VR = stock.VR, LR = stock.LR, PC = stock.PC,
            RX = stock.RX, AZ = stock.AZ, BB = stock.BB, EE = stock.EE,
            ARC = stock.ARC
        )

        // Enviar levantamento para o servidor
        val response = executeApiCall("APILevantamento") {
            ApiServices.levantamentoService.createLevantamento(levantamento)
        }

        // Reset da máquina na API (valores parciais a zero)
        val maquinaResetada = maquina.copy(
            apostadoParcial = 0,
            atribuidoParcial = 0f,
            taxaGanhoParcial = 0f
        )
        actualizaMaquina(maquinaResetada)

        return response
    }

    suspend fun registarTalao(maquina: Maquina, numeroJogadas: Int): Talao? {
        val numeroSerie = generateSerialNumberTalaoELevatamento(maquina.numeroSerie)

        val talao = Talao(
            numeroSerie = numeroSerie,
            numeroJogadas = numeroJogadas,
            valorJogadas = maquina.valorAposta,
            dataImpressao = null,
            impressaoOK = false,
            Maquinas_numeroSerie = maquina.numeroSerie
        )

        Log.d("APITalao", "📤 Enviando requisição para criar talão: $talao")

        return executeApiCall("APITalao") {
            ApiServices.talaoService.createTalao(talao)
        }
    }

    fun generateSerialNumberPremio(SN_primaryKey: String): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        val secondsOfDay = now.toLocalTime().toSecondOfDay()
        val segundosFormatados = String.format("%06d", secondsOfDay) // ✅ Aqui usas o valor

        return "$SN_primaryKey$segundosFormatados"
    }

    fun generateSerialNumberTalaoELevatamento(SN_primaryKey: String): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        val secondsOfDay = now.toLocalTime().toSecondOfDay()
        val segundosFormatados = String.format("%06d", secondsOfDay) // ✅ Aqui usas o valor

        return "$SN_primaryKey${now.format(formatter)}$segundosFormatados"
    }

    suspend fun buscarDadosMaquinaRolleta(numeroSerie: String): Maquina? {
        return try {
            val response = ApiServices.maquinaService.getMaquina(numeroSerie)
            if (response.isSuccessful) {
                response.body()
            } else {
                println("Erro ao buscar máquina: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            println("Erro de conexão: ${e.message}")
            null
        }
    }

    suspend fun buscarStock(numeroSerie: String): Stock? {
        return try {
            val response = ApiServices.stockService.getStock(numeroSerie)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun atualizarStock(stock: Stock): Boolean {
        return try {
            val response = ApiServices.stockService.updateStock(stock.Maquinas_numeroSerie, stock)
            if (response.isSuccessful) {
                Log.d("TabelaStock", "Estoque atualizado com sucesso!")
                true
            } else {
                Log.e("TabelaStock", "Erro ao atualizar estoque: ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("TabelaStock", "Erro de conexão ao atualizar estoque: ${e.message}")
            false
        }
    }

    suspend fun buscarSetup(numeroSerie: String): Setup? {
        return try {
            val response = ApiServices.setupService.getSetup(numeroSerie)

            Log.d("SETUP_API", "Código: ${response.code()} | Body: ${response.body()} | Erro: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SETUP_API", "Exceção: ${e.message}")
            null
        }
    }

    suspend fun atualizarSetup(setup: Setup): Boolean {
        return try {
            val response = ApiServices.setupService.updateSetup(setup.Maquinas_numeroSerie, setup)
            if (response.isSuccessful) {
                Log.d("TabelaSetup", "Setup atualizado com sucesso!")
                true
            } else {
                Log.e("TabelaSetup", "Erro ao atualizar setup: ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("TabelaSetup", "Erro de conexão ao atualizar setup: ${e.message}")
            false
        }
    }

    suspend fun descontarPremiosDoStock(numeroSerie: String, mapaPremios: Map<String, Int>, stockAtual: Stock) {
        var novoStock = stockAtual

        mapaPremios.forEach { (label, quantidade) ->
            novoStock = when (label) {
                "VD" -> novoStock.copy(VD = novoStock.VD - quantidade)
                "PT" -> novoStock.copy(PT = novoStock.PT - quantidade)
                "CI" -> novoStock.copy(CI = novoStock.CI - quantidade)
                "AM" -> novoStock.copy(AM = novoStock.AM - quantidade)
                "GM" -> novoStock.copy(GM = novoStock.GM - quantidade)
                "VR" -> novoStock.copy(VR = novoStock.VR - quantidade)
                "LR" -> novoStock.copy(LR = novoStock.LR - quantidade)
                "PC" -> novoStock.copy(PC = novoStock.PC - quantidade)
                "RX" -> novoStock.copy(RX = novoStock.RX - quantidade)
                "AZ" -> novoStock.copy(AZ = novoStock.AZ - quantidade)
                "BB" -> novoStock.copy(BB = novoStock.BB - quantidade)
                "EE" -> novoStock.copy(EE = novoStock.EE - quantidade)
                "ARC" -> novoStock.copy(ARC = novoStock.ARC - quantidade)
                else -> {
                    Log.w("Stock", "❓ Campo desconhecido: $label")
                    novoStock
                }
            }

            Log.d("Stock", "📦 Descontado $quantidade unidade(s) de $label")
        }

        atualizarStock(novoStock)
    }

    suspend fun getPremios(numeroSerie: String, todos: Boolean): List<Premio> {
        return try {
            Log.d("PremioService", "➡️ getPremios(numeroSerie=$numeroSerie, todos=$todos)")
            val response = ApiServices.premioService.getPremios(numeroSerie, todos)
            Log.d("PremioService", "⬅️ HTTP ${response.code()} | isSuccessful=${response.isSuccessful}")
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("API Premios", "Erro ao obter prêmios: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API Premios", "Exceção: ${e.message}")
            emptyList()
        }
    }

    // 🔹 Atualizar todos os prêmios não contabilizados para contabilizado = 1
    suspend fun contabilizarPremios(numeroSerie: String): Boolean {
        return try {
            val response = ApiServices.premioService.contabilizarPremios(numeroSerie)
            if (response.isSuccessful) {
                Log.d("API Premios", "✅ Todos os prêmios foram contabilizados.")
                true
            } else {
                Log.e("API Premios", "❌ Falha ao contabilizar prêmios: ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("API Premios", "❌ Erro de conexão: ${e.message}")
            false
        }
    }

    suspend fun reportarEstadoImpressora(maquina: Maquina, novoEstado: String) {
        val atualizada = maquina.copy(roloPapelOK = novoEstado)
        try {
            APIResource.actualizaMaquina(atualizada)
            Log.i("BLE", "📡 Estado da impressora atualizado para $novoEstado")
        } catch (e: Exception) {
            Log.e("BLE", "❌ Erro ao atualizar estado da impressora: ${e.message}")
        }
    }
}