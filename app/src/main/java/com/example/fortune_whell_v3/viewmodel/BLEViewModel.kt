package com.example.bleproject.viewmodel

import BleMessage
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bleproject.ble.BLEManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class BLEViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleManager = BLEManager(context)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _lastMessage = MutableStateFlow("")
    val lastMessage: StateFlow<String> = _lastMessage

    private val _mensagensRecebidas = MutableSharedFlow<String>()

    private val _bleMessage = MutableStateFlow<BleMessage?>(null)
    val bleMessage: StateFlow<BleMessage?> = _bleMessage

    init {
        bleManager.onConnected = {
            _isConnected.value = true
        }
        bleManager.onDisconnected = {
            _isConnected.value = false
        }
        bleManager.onBleMessage = { msg ->
            viewModelScope.launch {
                val novoMsg = when {
                    msg is BleMessage.Texto && msg.conteudo.startsWith("MOEDA|") -> {
                        val valor = msg.conteudo.removePrefix("MOEDA|").toFloatOrNull()
                        if (valor != null) BleMessage.MoedaRecebida(valor) else msg
                    }
                    msg is BleMessage.Texto && msg.conteudo.startsWith("NOTA|") -> {
                        val valor = msg.conteudo.removePrefix("NOTA|").toFloatOrNull()
                        if (valor != null) BleMessage.NotaRecebida(valor) else msg
                    }
                    else -> msg
                }

                _bleMessage.value = novoMsg

                val texto = when (novoMsg) {
                    is BleMessage.Ok -> "OK"
                    is BleMessage.SemPapel -> "SEM_PAPEL"
                    is BleMessage.Erro -> "ERRO"
                    is BleMessage.Fim -> "FIM"
                    is BleMessage.Texto -> novoMsg.conteudo
                    is BleMessage.MoedaRecebida -> "MOEDA|${novoMsg.valor}"
                    is BleMessage.NotaRecebida -> "NOTA|${novoMsg.valor}"
                    else -> "DESCONHECIDO"
                }
                _lastMessage.value = texto
                _mensagensRecebidas.emit(texto)
            }
        }
    }

    fun connectToMac(macAddress: String) {
        if (adapter != null && BluetoothAdapter.checkBluetoothAddress(macAddress)) {
            val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)
            bleManager.connect(device)
        } else {
            Log.e("BLEViewModel", "❌ Adapter nulo ou MAC inválido")
        }
    }

    fun sendMessage(message: String) {
        bleManager.sendMessage(message)
    }

    suspend fun awaitResposta(timeout: Long = 5000): String? {
        return withTimeoutOrNull(timeout) {
            _mensagensRecebidas.first()
        }
    }

    fun sendLongMessage(
        mensagem: String,
        esperandoConfirmacao: MutableState<Boolean>,
        onRespostaRecebida: (String?) -> Unit,
        maxLength: Int = 18
    ) {
        viewModelScope.launch {
            val chunks = mensagem.chunked(maxLength)
            for ((index, chunk) in chunks.withIndex()) {
                bleManager.sendMessage(
                    if (index == chunks.lastIndex) chunk + "!" else chunk
                )
                delay(200)
            }
            esperandoConfirmacao.value = true
            val resposta = awaitResposta()
            esperandoConfirmacao.value = false
            onRespostaRecebida(resposta)
        }
    }

    fun disconnect() {
        bleManager.disconnect()
    }
}