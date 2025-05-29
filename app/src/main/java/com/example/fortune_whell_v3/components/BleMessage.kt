sealed class BleMessage {
    object Ok : BleMessage()
    object SemPapel : BleMessage()
    object Erro : BleMessage()
    object Fim : BleMessage()
    data class Desconhecida(val conteudo: String) : BleMessage()
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
        else -> BleMessage.Desconhecida(msg)
    }
}

