package com.example.fortune_whell_v3.api.models

data class Talao (
    val numeroSerie: String,      // Opcional para Request
    val numeroJogadas: Int,
    val valorJogadas: Int,
    val dataImpressao: String? = null, // Opcional para Response
    val impressaoOK: Boolean,
    val Maquinas_numeroSerie: String
)