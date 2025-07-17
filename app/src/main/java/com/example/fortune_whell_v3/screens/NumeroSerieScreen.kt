package com.example.fortune_whell_v3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel

@Composable
fun NumeroSerieScreen(
    navController: NavController,
    maquinaViewModel: MaquinaViewModel
) {
    var numeroSerieInput by remember { mutableStateOf(maquinaViewModel.numeroSerie) }
    var macInput by remember { mutableStateOf(maquinaViewModel.macESP32) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Insira os dados da máquina",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Campo para o número de série
        TextField(
            value = numeroSerieInput,
            onValueChange = { numeroSerieInput = it },
            label = { Text("Número de Série") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Campo para o MAC do ESP32
        TextField(
            value = macInput,
            onValueChange = { macInput = it },
            label = { Text("MAC do ESP32") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Botão continuar
        Button(
            onClick = {
                maquinaViewModel.definirNumeroSerie(numeroSerieInput)
                maquinaViewModel.definirMacESP32(macInput)
                navController.navigate("info") {
                    popUpTo("definirNumeroSerie") { inclusive = true }
                }
            },
            enabled = numeroSerieInput.isNotBlank() && macInput.isNotBlank()
        ) {
            Text("Continuar")
        }
    }
}
