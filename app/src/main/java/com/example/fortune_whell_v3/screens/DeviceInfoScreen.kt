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

@Composable
fun DeviceInfoScreen(
    navController: NavController,
    maquinaViewModel: MaquinaViewModel,
    bleViewModel: BLEViewModel
) {
    val serialNumber = maquinaViewModel.numeroSerie  // ← novo!
    val macAddress = "3C:8A:1F:B0:07:D2"

    val isConnected by bleViewModel.isConnected.collectAsState()
    val maquina = maquinaViewModel.maquina
    val setup = maquinaViewModel.setup

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

        if (maquina == null || setup == null || !isConnected  ) {
            Text("❌ Erro ao buscar dados da máquina ou setup", color = MaterialTheme.colorScheme.error)

            Button(onClick = {
                navController.navigate("info")
            }) {
                Text("Tentar novamente")
            }
        } else {
            Text("✅ Máquina encontrada:")
            Text("Número de Série: ${maquina.numeroSerie}")
            Text("Status: ${maquina.status}")

            Button(onClick = {
                navController.navigate("login") {
                    popUpTo("device_info") { inclusive = true }
                }
            }) {
                Text("Continuar")
            }
        }
    }
}

