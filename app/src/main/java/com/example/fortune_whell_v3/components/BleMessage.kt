sealed class BleMessage {
    object Ok : BleMessage()
    object SemPapel : BleMessage()
    object Erro : BleMessage()
    object Fim : BleMessage()
    data class Texto(val conteudo: String) : BleMessage()
    data class MoedaRecebida(val valor: Float) : BleMessage()
    data class NotaRecebida(val valor: Float) : BleMessage()
}



fun parseBleMessage(msg: String): BleMessage {
    val clean = msg.trim()
        .removePrefix("$")
        .substringBefore("@")
        .uppercase()

    return when (clean) {
        "OK" -> BleMessage.Ok
        "SEM_PAPEL" -> BleMessage.SemPapel
        "ERRO" -> BleMessage.Erro
        "FIM" -> BleMessage.Fim
        else -> BleMessage.Texto(msg) // ✅ devolve como texto genérico
    }
}


