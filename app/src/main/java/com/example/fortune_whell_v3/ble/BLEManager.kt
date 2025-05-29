package com.example.bleproject.ble

import BleMessage
import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import parseBleMessage
import java.util.*

class BLEManager(private val context: Context) {

    val SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    val RX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    val TX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")


    companion object {
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9B34FB"
    }



    var onBleMessage: ((BleMessage) -> Unit)? = null

    private var bluetoothGatt: BluetoothGatt? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null

    var onMessageReceived: ((String) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    fun connect(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLEManager", "❌ Permissão BLUETOOTH_CONNECT não concedida")
            return
        }

        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null

        Log.d("BLEManager", "➡️ Tentando conectar com ${device.address}")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }



    fun disconnect() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bluetoothGatt?.disconnect()
    }

    fun sendMessage(message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        Log.d("BLEManager", "✅ A enviar dados: $message")
        val data = message.toByteArray(Charsets.UTF_8)
        bluetoothGatt?.let { gatt ->
            rxCharacteristic?.let { characteristic ->
                gatt.writeCharacteristic(characteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BLEManager", "📡 Conexão alterada: status=$status, newState=$newState")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLEManager", "✅ Dispositivo conectado. Tentando descobrir serviços...")

                Handler(Looper.getMainLooper()).postDelayed({
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        val success = gatt.discoverServices()
                        Log.d("BLEManager", "🔍 discoverServices() chamado: $success")
                    } else {
                        Log.e("BLEManager", "❌ Permissão BLUETOOTH_CONNECT não concedida, não é possível chamar discoverServices()")
                    }
                }, 300)

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("BLEManager", "⚠️ Dispositivo desconectado")
                onDisconnected?.invoke()
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("BLEManager", "❌ Falha ao descobrir serviços")
                return
            }

            gatt.services.forEach { service ->
                Log.d("BLEManager", "🔍 Serviço: ${service.uuid}")
                service.characteristics.forEach { characteristic ->
                    Log.d("BLEManager", "    ↳ Característica: ${characteristic.uuid}")
                }
            }

            val service = gatt.getService(SERVICE_UUID) ?: run {
                Log.e("BLEManager", "❌ Serviço não encontrado!")
                return
            }

            txCharacteristic = service.getCharacteristic(TX_CHARACTERISTIC_UUID)
            rxCharacteristic = service.getCharacteristic(RX_CHARACTERISTIC_UUID)

            enableNotifications(gatt, rxCharacteristic!!)
            onConnected?.invoke()
        }




        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic == rxCharacteristic) {
                val message = value.toString(Charsets.UTF_8).trim()
                onMessageReceived?.invoke(message)

                val bleMessage = parseBleMessage(message)
                onBleMessage?.invoke(bleMessage)
            }
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG)) ?: return

        gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
    }


    private fun hasBluetoothConnectPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

}
