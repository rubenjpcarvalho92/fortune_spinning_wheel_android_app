package com.example.fortune_whell_v3.viewmodel

import android.Manifest
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import android.app.Application
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class BLEViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private var gatt: BluetoothGatt? = null

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    // 🔹 Estados de conexão BLE
    private val _connectionState = MutableStateFlow("Desconectado")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    private val _receivedMessage = MutableStateFlow<BleMessage>(BleMessage.Unknown(""))
    val receivedMessage: StateFlow<BleMessage> = _receivedMessage.asStateFlow()

    // 🔹 UUIDs para comunicação BLE
    private val SERVICE_UUID = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214")
    private val CHARACTERISTIC_UUID = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214")

    // 🔹 Conectar ao dispositivo já pareado pelo nRF Connect
    fun connectToExistingDevice(macAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("BLEViewModel", "🚀 A tentar conectar ao MAC: $macAddress") // ← AQUI
            if (!hasBluetoothPermission()) {
                Log.e("BLEViewModel", "❌ Permissão BLUETOOTH_CONNECT não concedida!")
                _connectionState.value = "Erro: Permissão negada"
                return@launch
            }

            bluetoothAdapter?.let { adapter ->
                try {
                    val device = adapter.getRemoteDevice(macAddress)
                    _connectionState.value = "Conectando..."

                    gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)

                } catch (e: SecurityException) {
                    _connectionState.value = "Erro de permissão Bluetooth"
                    Log.e("BLEViewModel", "❌ Erro ao conectar ao BLE", e)
                } catch (e: IllegalArgumentException) {
                    _connectionState.value = "Erro: Dispositivo inválido"
                    Log.e("BLEViewModel", "❌ MAC Address inválido!", e)
                }
            } ?: run {
                _connectionState.value = "Bluetooth não suportado"
            }
        }
    }
    // 🔹 Método para enviar dados após a conexão
    fun sendData(data: String) {
        gatt?.let { gatt ->
            val service = gatt.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

            if (characteristic == null) {
                Log.e("BLEViewModel", "❌ Característica BLE não encontrada! Verifica os UUIDs e a descoberta de serviços.")
                return
            }

            if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
                Log.e("BLEViewModel", "❌ Característica não suporta escrita!")
                return
            }

            val dataToSend = data.toByteArray(Charsets.UTF_8)

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e("BLEViewModel", "⚠️ Permissão BLUETOOTH_CONNECT não concedida!")
                return
            }

            try {
                gatt.writeCharacteristic(characteristic, dataToSend, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                Log.d("BLEViewModel", "📡 Dados enviados: $data")
            } catch (e: SecurityException) {
                Log.e("BLEViewModel", "❌ Erro de permissão ao escrever característica BLE", e)
            } catch (e: Exception) {
                Log.e("BLEViewModel", "❌ Erro inesperado ao enviar dados", e)
            }
        } ?: Log.e("BLEViewModel", "❌ GATT é null! Dispositivo pode não estar conectado.")
    }

    // 🔹 Ativar notificações para receber dados do dispositivo BLE
    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        try {
            gatt.setCharacteristicNotification(characteristic, true)

            val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))

            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            Log.d("BLEViewModel", "✅ Notificações habilitadas para ${characteristic.uuid}")

        } catch (e: SecurityException) {
            Log.e("BLEViewModel", "Erro ao ativar notificações BLE", e)
        } catch (e: IllegalArgumentException) {
            Log.e("BLEViewModel", "Erro: Descriptor inválido!", e)
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = "Conectado"
                    Log.d("BLEViewModel", "✅ Conectado! Descobrindo serviços...")

                    if (hasBluetoothPermission()) {
                        try {
                            gatt?.discoverServices()
                        } catch (e: SecurityException) {
                            Log.e("BLEViewModel", "Erro de permissão ao descobrir serviços", e)
                        }
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = "Desconectado"
                    Log.d("BLEViewModel", "❌ Desconectado.")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLEViewModel", "🔍 Serviços descobertos:")
                gatt?.services?.forEach { service ->
                    Log.d("BLEViewModel", "  🧩 Serviço: ${service.uuid}")
                    service.characteristics.forEach { char ->
                        Log.d("BLEViewModel", "     ↳ Característica: ${char.uuid} - propriedades: ${char.properties}")
                    }
                }

                val service = gatt?.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

                if (characteristic != null) {
                    Log.d("BLEViewModel", "✅ Característica encontrada! Ativando notificações...")
                    enableNotifications(gatt, characteristic)
                } else {
                    Log.e("BLEViewModel", "❌ Característica BLE não encontrada! Verifica os UUIDs e a descoberta de serviços.")
                }
            } else {
                Log.e("BLEViewModel", "⚠️ Falha ao descobrir serviços BLE!")
            }
        }



        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            val raw = characteristic?.value?.toString(Charsets.UTF_8)?.trim() ?: return
            val parsed = parseBleMessage(raw)

            Log.d("BLEViewModel", "📩 Mensagem interpretada: $parsed")

            viewModelScope.launch {
                _receivedMessage.emit(parsed)
            }
        }
    }

    /** 🔹 **Apenas usa o GATT de um dispositivo já conectado pelo nRF Connect** */
    fun useExistingGattConnection(device: BluetoothDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!hasBluetoothPermission()) {
                    Log.e("BLEViewModel", "⚠️ Permissão BLUETOOTH_CONNECT não concedida!")
                    return@launch
                }

                gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
                Log.d("BLEViewModel", "✅ Usando conexão BLE existente.")
            } catch (e: SecurityException) {
                Log.e("BLEViewModel", "Erro de permissão ao usar conexão existente.", e)
            }
        }
    }

    fun updateConnectionState() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        try {
            // Apenas verifica permissão no Android 12+
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                _connectionState.value = "Permissão negada"
                return
            }

            val connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)

            if (connectedDevices.isNotEmpty()) {
                val device = connectedDevices[0]
                _connectionState.value = "Conectado"

                gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
                gatt?.discoverServices()
            } else {
                _connectionState.value = "Desconectado"
            }

        } catch (e: SecurityException) {
            Log.e("Bluetooth", "❌ Permissão de Bluetooth negada em tempo de execução", e)
            _connectionState.value = "Permissão negada"
        } catch (e: Exception) {
            Log.e("Bluetooth", "❌ Erro ao verificar dispositivos conectados", e)
            _connectionState.value = "Erro ao conectar"
        }
    }

    sealed class BleMessage {
        object Ok : BleMessage()
        object Fim : BleMessage()
        object Erro : BleMessage()
        object SemPapel : BleMessage()
        data class Unknown(val raw: String) : BleMessage()
    }

    fun parseBleMessage(raw: String): BleMessage {
        val trimmed = raw.trim()
        val upper = trimmed.uppercase()

        return when (upper) {
            "OK" -> {
                Log.d("BLERecebido", "🧾 BLE recebido: '$trimmed' (${trimmed.length} chars)")
                BleMessage.Ok
            }

            "TAMPA ABERTA" ->{
                Log.d("BLERecebido", "🧾 BLE recebido: '$trimmed' (${trimmed.length} chars)")
                BleMessage.Fim
            }

            "ERRO" ->{
                Log.d("BLERecebido", "🧾 BLE recebido: '$trimmed' (${trimmed.length} chars)")
                BleMessage.Erro
            }

            "SEM_PAPEL" ->{
                Log.d("BLERecebido", "🧾 BLE recebido: '$trimmed' (${trimmed.length} chars)")
                BleMessage.SemPapel
            }

            else -> {
                Log.w("BLE", "🔸 Ignorado: '$trimmed' (${trimmed.length} chars)")
                BleMessage.Unknown(trimmed)
            }
        }
    }

    suspend fun awaitResposta(timeout: Long = 5000L): String? {
        return withTimeoutOrNull(timeout) {
            receivedMessage.first {
                when (it) {
                    is BleMessage.Ok,
                    is BleMessage.Fim,
                    is BleMessage.Erro,
                    is BleMessage.SemPapel -> true

                    else -> false
                }
            }.let { msg ->
                when (msg) {
                    is BleMessage.Ok -> "OK"
                    is BleMessage.Fim -> "FIM"
                    is BleMessage.Erro -> "ERRO"
                    is BleMessage.SemPapel -> "SEM_PAPEL"
                    else -> null
                }
            }
        }
    }

}
