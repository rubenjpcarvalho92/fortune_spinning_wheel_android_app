package com.example.fortune_whell_v3.screens

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.bleproject.viewmodel.BLEViewModel
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel

@Composable
fun DeviceInfoScreen(
    navController: NavController,
    maquinaViewModel: MaquinaViewModel,
    bleViewModel: BLEViewModel
) {
    val context = LocalContext.current
    val serialNumber = getTabletSerialNumber(context)
    val macAddress = "3C:8A:1F:B0:07:D2"

    val isConnected by bleViewModel.isConnected.collectAsState()
    val maquina = maquinaViewModel.maquina
    val setup = maquinaViewModel.setup

    // ✅ Só tenta ligar se ainda não estiver ligado (independentemente de maquina/setup)
    LaunchedEffect(maquina?.MACArduino, isConnected) {
        val mac = maquina?.MACArduino
        if (mac != null && !isConnected) {
            bleViewModel.connectToMac(mac)
            Log.d("BLE", "🔌 A tentar ligar ao MAC: $mac")
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Status BLE: ${if (isConnected) "✅ Conectado" else "❌ Não conectado"}")

        Text("SN do Tablet", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(serialNumber, fontSize = 16.sp)

        Text("MAC do Arduino", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(macAddress, fontSize = 16.sp)

        if (maquina == null || setup == null) {
            Text("❌ Erro ao buscar dados da máquina ou setup", color = MaterialTheme.colorScheme.error)

            Button(onClick = {
                navController.navigate("info") // ou força recarregar dados
            }) {
                Text("Tentar novamente")
            }
        } else {
            Text("✅ Máquina encontrada:")
            Text("Número de Série: ${maquina.numeroSerie}")
            Text("Status: ${maquina.status}")

            Button(onClick = {
                navController.navigate("main") {
                    popUpTo("device_info") { inclusive = true }
                }
            }) {
                Text("Continuar")
            }
        }
    }
}


fun getTabletSerialNumber(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}

fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    return bluetoothManager?.adapter
}

fun hasBluetoothPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.BLUETOOTH_CONNECT
    ) == PackageManager.PERMISSION_GRANTED
}
