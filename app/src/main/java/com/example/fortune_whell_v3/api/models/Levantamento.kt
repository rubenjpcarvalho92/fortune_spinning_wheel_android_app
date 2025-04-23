package com.example.fortune_whell_v3.api.models

data class Levantamento(
    val numeroSerie: String,
    val data: String,
    val apostadoParcial: Int,
    val taxaGanhoParcial: Double,
    val atribuidoParcial: Float,
    val Maquinas_numeroSerie: String,
    val VD: Int,
    val PT: Int,
    val CI: Int,
    val AM: Int,
    val GM: Int,
    val VR: Int,
    val LR: Int,
    val PC: Int,
    val RX: Int,
    val AZ: Int,
    val BB: Int,
    val EE: Int,
    val ARC: Int
)
