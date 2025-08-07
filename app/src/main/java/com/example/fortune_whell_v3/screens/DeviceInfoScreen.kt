package com.example.fortune_whell_v3.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bleproject.viewmodel.BLEViewModel
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel
import kotlinx.coroutines.delay

@Composable
fun DeviceInfoScreen(
    navController: NavController,
    maquinaViewModel: MaquinaViewModel,
    bleViewModel: BLEViewModel
) {
    val serialNumber = maquinaViewModel.numeroSerie
    val macAddress = maquinaViewModel.macESP32

    val isConnected by bleViewModel.isConnected.collectAsState()
    val maquina = maquinaViewModel.maquina
    val setup = maquinaViewModel.setup

    var mostrarErro by remember { mutableStateOf(false) }

    fun tentarConectar() {
        val mac = maquina?.MACArduino ?: macAddress
        if (!isConnected && mac.isNotBlank()) {
            bleViewModel.connectToMac(mac)
            Log.d("BLE", "🔌 A tentar ligar ao MAC: $mac")
        }
    }

    var countdown by remember { mutableStateOf(30) }

    LaunchedEffect(isConnected, maquina, setup) {
        while (!isConnected || maquina == null || setup == null) {
            countdown = 10
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            tentarConectar()
        }
    }


    LaunchedEffect(maquina?.MACArduino, isConnected) {
        if (maquina != null && setup != null && isConnected) {
            if (maquina.status == "Activa") {
                navController.navigate("main") {
                    popUpTo("device_info") { inclusive = true }
                }
            } else {
                mostrarErro = true
            }
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

        if (mostrarErro || maquina == null || setup == null || !isConnected) {
            Text("❌ Erro ao buscar dados da máquina ou setup", color = MaterialTheme.colorScheme.error)

            Button(onClick = { tentarConectar() }) {
                Text("Tentar novamente")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("⏳ Tentando novamente em $countdown segundos...", fontSize = 14.sp)
        } else {
            Text("✅ Máquina encontrada:")
            Text("Número de Série: ${maquina.numeroSerie}")
            Text("Status: ${maquina.status}")
        }
    }
}
