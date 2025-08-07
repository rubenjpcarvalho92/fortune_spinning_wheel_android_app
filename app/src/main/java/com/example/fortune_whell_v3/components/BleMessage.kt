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

    return when {
        clean.equals("OK", ignoreCase = true) -> BleMessage.Ok
        clean.equals("SEM_PAPEL", ignoreCase = true) -> BleMessage.SemPapel
        clean.equals("ERRO", ignoreCase = true) -> BleMessage.Erro
        clean.equals("FIM", ignoreCase = true) -> BleMessage.Fim

        clean.startsWith("MOEDA|", ignoreCase = true) -> {
            val valor = clean.substringAfter("MOEDA|").toFloatOrNull() ?: 0f
            BleMessage.MoedaRecebida(valor)
        }

        clean.startsWith("NOTA|", ignoreCase = true) -> {
            val valor = clean.substringAfter("NOTA|").toFloatOrNull() ?: 0f
            BleMessage.NotaRecebida(valor)
        }

        else -> BleMessage.Texto(msg) // genérico
    }
}



