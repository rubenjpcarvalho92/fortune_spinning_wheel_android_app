package com.example.fortune_whell_v3.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.example.fortune_whell_v3.api.models.Maquina
import com.example.fortune_whell_v3.api.models.Setup
import com.example.fortune_whell_v3.api.resources.APIResource
import kotlinx.coroutines.launch

class MaquinaViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)

    var maquina by mutableStateOf<Maquina?>(null)
        private set

    var setup by mutableStateOf<Setup?>(null)
        private set

    var numeroSerie by mutableStateOf(prefs.getString("numero_serie", "") ?: "")
        private set

    var macESP32 by mutableStateOf(prefs.getString("mac_esp32", "") ?: "")
        private set

    // 🔹 Inicializa automaticamente se ambos os campos estiverem preenchidos
    init {
        if (numeroSerie.isNotBlank() && macESP32.isNotBlank()) {
            inicializar(numeroSerie)
        }
    }

    // 🔹 Define e guarda o número de série manualmente
    fun definirNumeroSerie(novo: String) {
        numeroSerie = novo
        prefs.edit().putString("numero_serie", novo).apply()
        if (macESP32.isNotBlank()) {
            inicializar(novo)
        }
    }

    // 🔹 Define e guarda o MAC do ESP32 manualmente
    fun definirMacESP32(novo: String) {
        macESP32 = novo
        prefs.edit().putString("mac_esp32", novo).apply()
        if (numeroSerie.isNotBlank()) {
            inicializar(numeroSerie)
        }
    }

    fun atualizarMaquina(maquinaAtualizada: Maquina) {
        maquina = maquinaAtualizada
    }

    // 🔹 Inicializa a máquina e o setup com base no número de série
    fun inicializar(numeroSerie: String = this.numeroSerie) {
        if (numeroSerie.isBlank()) return

        this.numeroSerie = numeroSerie

        viewModelScope.launch {
            maquina = APIResource.buscarDadosMaquinaRolleta(numeroSerie)
            setup = APIResource.buscarSetup(numeroSerie)
        }
    }

    // 🔹 Propriedades derivadas do setup
    val labels: List<String>
        get() = setup?.let {
            listOf(
                it.L0, it.L1, it.L2, it.L3,
                it.L4, it.L5, it.L6, it.L7,
                it.L8, it.L9, it.L10, it.L11,
                it.L12, it.L13, it.L14, it.L15
            )
        } ?: emptyList()

    val premios: List<Float>
        get() = setup?.let {
            listOf(
                it.P0, it.P1, it.P2, it.P3,
                it.P4, it.P5, it.P6, it.P7,
                it.P8, it.P9, it.P10, it.P11,
                it.P12, it.P13, it.P14, it.P15
            )
        } ?: emptyList()

    val cores: List<String>
        get() = setup?.let {
            listOf(
                it.C0, it.C1, it.C2, it.C3,
                it.C4, it.C5, it.C6, it.C7,
                it.C8, it.C9, it.C10, it.C11,
                it.C12, it.C13, it.C14, it.C15
            )
        } ?: emptyList()
}
