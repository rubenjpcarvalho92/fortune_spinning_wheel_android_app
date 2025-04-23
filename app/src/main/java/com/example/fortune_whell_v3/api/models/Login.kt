package com.example.fortune_whell_v3.api.models

data class Login(
    val dataLogin: String,
    val nivelAcesso: String,
    val NIF: Int,
    val Maquinas_numeroSerie: String,
    val resultado: String
)
