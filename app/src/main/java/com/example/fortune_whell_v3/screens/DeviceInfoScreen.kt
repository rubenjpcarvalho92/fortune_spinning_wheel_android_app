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
import com.example.fortune_whell_v3.api.models.Maquina
import com.example.fortune_whell_v3.api.resources.APIResource
import kotlinx.coroutines.launch

@Composable
fun DeviceInfoScreen(
    navController: NavController,
) {
    val context = LocalContext.current

    var serialNumber by remember { mutableStateOf("") }
    var macAddress by remember { mutableStateOf("") }

    var maquina by remember { mutableStateOf<Maquina?>(null) }
    var erro by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun atualizarMaquina() {
        coroutineScope.launch {
            isLoading = true
            erro = false

            serialNumber = getTabletSerialNumber(context)
            macAddress = getArduinoMacAddress(context) ?: "Não encontrado"

            Log.d("DeviceInfoScreen", "Buscando máquina com SN: $serialNumber")

            val resultado = APIResource.buscarDadosMaquinaRolleta(serialNumber)

            if (resultado != null) {
                Log.d("DeviceInfoScreen", "Máquina encontrada: ${resultado.numeroSerie}, Status: ${resultado.status}")
                maquina = resultado

                // 👉 Navega para login automaticamente
                navController.navigate("login") {
                    popUpTo("device_info") { inclusive = true }
                }
            } else {
                Log.e("DeviceInfoScreen", "Erro: máquina não encontrada ou falha de conexão")
                erro = true
            }

            isLoading = false
        }
    }

    // Primeira chamada
    LaunchedEffect(Unit) {
        atualizarMaquina()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Só mostra SN e MAC se a máquina NÃO foi encontrada
        if (maquina == null && !isLoading) {
            Text("SN do Tablet", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(serialNumber, fontSize = 16.sp)

            Text("MAC do Arduino", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(macAddress, fontSize = 16.sp)
        }

        when {
            isLoading -> CircularProgressIndicator()
            erro -> Text("❌ Erro ao buscar máquina!", color = MaterialTheme.colorScheme.error)
        }

        if (maquina == null && !isLoading) {
            Button(
                onClick = { atualizarMaquina() },
                enabled = true
            ) {
                Text("Atualizar")
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

fun getArduinoMacAddress(context: Context): String? {
    if (!hasBluetoothPermission(context)) {
        return "Permissão não concedida"
    }

    return try {
        val bluetoothAdapter = getBluetoothAdapter(context)
        val pairedDevices = bluetoothAdapter?.bondedDevices

        pairedDevices?.forEach { device ->
            if (device.name.contains("Arduino", ignoreCase = true)) {
                return device.address
            }
        }
        "Arduino não encontrado"
    } catch (e: SecurityException) {
        "Erro de permissão"
    }
}
