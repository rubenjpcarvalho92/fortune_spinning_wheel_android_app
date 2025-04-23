package com.example.fortune_whell_v3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.example.fortune_whell_v3.api.models.Maquina
import com.example.fortune_whell_v3.api.models.Setup
import com.example.fortune_whell_v3.api.resources.APIResource
import kotlinx.coroutines.launch

class MaquinaViewModel : ViewModel() {

    var maquina by mutableStateOf<Maquina?>(null)
        private set

    var setup by mutableStateOf<Setup?>(null)
        private set

    var numeroSerie by mutableStateOf("")
        private set

    // 🔹 Inicializa a máquina e o setup com base no número de série
    fun inicializar(numeroSerie: String) {
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

    val brindes: List<String>
        get() = setup?.let {
            listOf(
                it.brinde_am, it.brinde_az, it.brinde_bb, it.brinde_ci,
                it.brinde_ee, it.brinde_gm, it.brinde_lr, it.brinde_pc,
                it.brinde_pt, it.brinde_rx, it.brinde_vd, it.brinde_vr,
                it.brinde_arc
            )
        } ?: emptyList()
}
