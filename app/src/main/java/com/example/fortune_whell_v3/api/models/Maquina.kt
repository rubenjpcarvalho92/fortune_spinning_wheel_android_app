package com.example.fortune_whell_v3.api.models

data class Maquina(
    val numeroSerie: String,
    val valorAposta: Int,
    val atribuidoTotal: Float,
    val apostadoTotal: Int,
    val taxaGanhoDefinida: Float,
    val taxaGanhoActual: Float,
    val taxaGanhoParcial: Float,
    val apostadoParcial: Int,
    val atribuidoParcial: Float,
    val status: String,
    val roloPapelOK: String,
    val stockOK: String,
    val Admins_NIF: Int,
    val Funcionarios_NIF: Int,
    val Clientes_NIF: Int,
    val MACArduino: String,
    val apostadoParcialDinheiro: Int
)