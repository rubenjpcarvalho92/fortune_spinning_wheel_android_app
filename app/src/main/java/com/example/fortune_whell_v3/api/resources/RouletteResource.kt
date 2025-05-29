package com.example.fortune_whell_v3.resources

import android.util.Log
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.bleproject.viewmodel.BLEViewModel
import com.example.fortune_whell_v3.R
import com.example.fortune_whell_v3.api.models.Maquina
import com.example.fortune_whell_v3.api.models.Setup
import com.example.fortune_whell_v3.api.resources.APIResource
import kotlin.math.pow
import kotlin.random.Random


object RouletteResource {
    //Função de dinâmica de rotação da roleta
    fun cubicBezier(x1: Float, y1: Float, x2: Float, y2: Float): Easing {
        return Easing { fraction ->
            bezierInterpolate(fraction, x1, y1, x2, y2)
        }
    }
    fun bezierInterpolate(t: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val u = 1 - t
        return (3 * u.pow(2) * t * y1) + (3 * u * t.pow(2) * y2) + t.pow(3)
    }

    //Calculo das probabilidade segundo os valores dos premios e o RTP
    fun calcularProbabilidadesRTP(RTP_premio: Float, margemGanho: Float, premiosSetup: List<Float>): List<Float> {
        val premios = premiosSetup

        val indicesBola = setOf(3, 7, 11, 15)

        val probabilidadesNaoNormalizadas = premios.mapIndexed { index, premio ->
            when {
                index in indicesBola -> 0f
                premio > 0.2f -> RTP_premio / premio
                else -> margemGanho / 4
            }
        }

        val somaTotal = probabilidadesNaoNormalizadas.sum()

        return if (somaTotal > 0f) {
            probabilidadesNaoNormalizadas.map { it / somaTotal }
        } else {
            probabilidadesNaoNormalizadas
        }
    }


    // Função para selecionar um índice com base em probabilidades
    fun selecionarIndicePorProbabilidade(probabilidades: List<Float>): Int {

        val somaProbabilidades = probabilidades.sum()
        println("Soma das probabilidades: $somaProbabilidades")

        val randomValue = Random.nextFloat()
        var acumulado = 0f
        probabilidades.forEachIndexed { index, probabilidade ->
            acumulado += probabilidade
            if (randomValue <= acumulado) {
                return index
            }
        }
        return probabilidades.size - 1 // Caso extremo
    }

    fun getCardImageForSlice(sliceIndex: Int, maquina: Maquina): Int {
        return when (sliceIndex) {
            0 -> R.drawable.carta_vd // VD
            1 -> R.drawable.carta_pt // PT
            2 -> R.drawable.carta_ci // CI
            4 -> R.drawable.carta_am // AM
            5 -> R.drawable.carta_gm // GM
            6  -> R.drawable.carta_vr // VR
            8  -> R.drawable.carta_lr // LR
            9  -> R.drawable.carta_pc // PC
            10  -> R.drawable.carta_rx // RX
            12  -> R.drawable.carta_az // AZ
            13  -> R.drawable.carta_bb // BB
            14  -> if (maquina.valorAposta == 2) R.drawable.carta_arc else R.drawable.carta_ee
            else -> R.drawable.carta_vd // Carta padrão (VD)
        }
    }

    fun getPrizeGrid(setup: Setup, maquina: Maquina): List<List<Any>> {
        val labels = listOf(
            setup.L0, setup.L1, setup.L2, setup.L3,
            setup.L4, setup.L5, setup.L6, setup.L7,
            setup.L8, setup.L9, setup.L10, setup.L11,
            setup.L12, setup.L13, setup.L14, setup.L15
        )

        val brindes = listOf(
            setup.brinde_lr, setup.brinde_vr, setup.brinde_pc, setup.brinde_az,
            setup.brinde_pt, setup.brinde_rx, setup.brinde_vd, setup.brinde_bb,
            setup.brinde_ci, setup.brinde_gm, setup.brinde_am,
            if (maquina.valorAposta == 2) setup.brinde_arc else setup.brinde_ee
        )

        val orderedLabels = listOf(
            labels[6], labels[5], labels[8], labels[2],
            labels[4], labels[9], labels[10], labels[1],
            labels[13], labels[14], labels[12], labels[11]
        )

        val orderedImages = listOf(
            brindes[0], brindes[1], brindes[2], brindes[3],
            brindes[4], brindes[5], brindes[6], brindes[7],
            brindes[8], brindes[9], brindes[10], brindes[11]
        )

        return listOf(
            orderedLabels.subList(0, 4),
            orderedImages.subList(0, 4),
            orderedLabels.subList(4, 8),
            orderedImages.subList(4, 8),
            orderedLabels.subList(8, 12),
            orderedImages.subList(8, 12)
        )
    }

    fun calculatePositionByIndex(sliceIndex: Int, roulettePosition: Float): Float {
        var deslocamento: Float
        var correcaoFatia:Float
        var anguloFinal: Float
        var novaPosicaoFatia: Float

        // 🔹 Obtém ângulo atual da roleta (mantém entre 0° e 360°)
        val anguloNormalizado = (roulettePosition % 360 + 360) % 360
        val anguloDentroFatia = ((anguloNormalizado % 22.5f) + 22.5f) % 22.5f

        // 🔹 Determina em qual fatia a roleta está atualmente
        val indice_actual =calcularIndiceRoleta(anguloNormalizado)

        if (sliceIndex >= indice_actual) {
            deslocamento = 360f-((sliceIndex-indice_actual)*22.5f)
        } else {
            deslocamento = ((indice_actual-sliceIndex)*22.5f)
        }

        // 🔹 Garante que o giro seja sempre no sentido horário
        val voltas = Random.nextInt(10, 30) * 360.0f

        novaPosicaoFatia = Random.nextFloat() * (20f - 2.5f) + 2.5f

        if(anguloDentroFatia>=novaPosicaoFatia){
            anguloFinal=-(anguloDentroFatia-novaPosicaoFatia)
        }else{
            anguloFinal=(novaPosicaoFatia-anguloDentroFatia)
        }

        return roulettePosition + deslocamento+voltas+anguloFinal
    }

    fun calculatePrizeIndex(maquina: Maquina, premios: List<Float>): Int {
        val RTP_actual = 1 - (maquina.taxaGanhoActual ?: 0f)
        val RTP_alvo = 1 - (maquina.taxaGanhoDefinida ?: 0f)
        var RTP_novo = if (RTP_actual > RTP_alvo) {
            RTP_alvo - (RTP_actual - RTP_alvo) * RTP_alvo
        } else {
            RTP_alvo + (RTP_actual - RTP_alvo) * RTP_alvo
        }

        //ter probabilidade snegtivas dava asneira
        //Fiaca copm valores negativos a dar premio
        //A soma das probabilidades deixaav de ser 1
        RTP_novo = RTP_novo.coerceIn(0f, 1f)

        val RTP_premio = RTP_novo / 8
        val margemGanho = 1 - RTP_novo

        val probabilidades = calcularProbabilidadesRTP(RTP_premio, margemGanho, premios)
        val sliceIndex = selecionarIndicePorProbabilidade(probabilidades)

        return sliceIndex
    }

    fun calcularIndiceRoleta(angulo: Float): Int {
        val a = ((angulo % 360f) + 360f) % 360f

        return when {
            a >= 0f && a < 22.5f -> 15
            a >= 22.5f && a < 45f -> 14
            a >= 45f && a < 67.5f -> 13
            a >= 67.5f && a < 90f -> 12
            a >= 90f && a < 112.5f -> 11
            a >= 112.5f && a < 135f -> 10
            a >= 135f && a < 157.5f -> 9
            a >= 157.5f && a < 180f -> 8
            a >= 180f && a < 202.5f -> 7
            a >= 202.5f && a < 225f -> 6
            a >= 225f && a < 247.5f -> 5
            a >= 247.5f && a < 270f -> 4
            a >= 270f && a < 292.5f -> 3
            a >= 292.5f && a < 315f -> 2
            a >= 315f && a < 337.5f -> 1
            else -> 0  // de 337.5f a 360f
        }
    }
    suspend fun realizarLevantamentoFinal(
        bleViewModel: BLEViewModel,
        numeroSerie: String,
        prizeListToPrint: SnapshotStateList<String>,
        levantamentoEmCurso: MutableState<Boolean>,
        esperandoConfirmacaoArduino: MutableState<Boolean>,
        onLevantamentoTerminado: () -> Unit,
        numeroTalao: String,
    ) {
        if (levantamentoEmCurso.value || prizeListToPrint.isEmpty()) return
        levantamentoEmCurso.value = true

        try {
            val dados = prizeListToPrint.joinToString(":")
            val comando = "TALAO|PRINT|$numeroTalao|$dados"
            Log.d("LEVANTAMENTO", "📦 Enviando comando BLE: $comando")

            bleViewModel.sendMessage(comando)
            esperandoConfirmacaoArduino.value = true

            val resposta = try {
                bleViewModel.awaitResposta(timeout = 5000)
            } catch (e: Exception) {
                Log.e("LEVANTAMENTO", "❌ Erro ao aguardar resposta BLE: ${e.message}")
                null
            }

            Log.d("LEVANTAMENTO", "🖨️ Resposta da impressora: $resposta")

            if (resposta == "OK") {
                try {
                    val mapaPremios = prizeListToPrint.groupingBy { it }.eachCount()

                    val stockAtual = try {
                        APIResource.buscarStock(numeroSerie)
                    } catch (e: Exception) {
                        Log.e("LEVANTAMENTO", "❌ Erro ao buscar stock: ${e.message}")
                        null
                    }

                    stockAtual?.let {
                        try {
                            APIResource.descontarPremiosDoStock(numeroSerie, mapaPremios, it)
                        } catch (e: Exception) {
                            Log.e("LEVANTAMENTO", "❌ Erro ao descontar stock: ${e.message}")
                        }
                    }

                    onLevantamentoTerminado()
                    Log.i("LEVANTAMENTO", "✅ Premios descontados e levantamento concluído.")
                } catch (e: Exception) {
                    Log.e("LEVANTAMENTO", "❌ Erro durante o processamento de levantamento: ${e.message}")
                }
            } else {
                Log.e("LEVANTAMENTO", "❌ Impressão falhou. Resposta: $resposta")
            }

        } catch (e: Exception) {
            Log.e("LEVANTAMENTO", "❌ Erro inesperado no levantamento: ${e.message}")
        } finally {
            esperandoConfirmacaoArduino.value = false
            levantamentoEmCurso.value = false
        }
    }



}

