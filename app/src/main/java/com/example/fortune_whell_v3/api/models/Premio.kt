package com.example.fortune_whell_v3.api.models

data class Premio (
    val codigoRoleta: String,
    val data: String,
    val numeroSerie: String,
    val categoriaPremio: Float,
    val contabilizado : Int,
    val Taloes_numeroSerie: String
)