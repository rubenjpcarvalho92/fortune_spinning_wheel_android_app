package com.example.fortune_whell_v3.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bleproject.viewmodel.BLEViewModel
import kotlinx.coroutines.launch


@Composable
fun BLETestScreen(bleViewModel: BLEViewModel = viewModel()) {
    var messageToSend by remember { mutableStateOf("") }
    val isConnected by bleViewModel.isConnected.collectAsState()
    var statusMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isConnected) "Estado: Conectado" else "Estado: Desconectado",
            color = if (isConnected) Color.Green else Color.Red,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = messageToSend,
            onValueChange = { messageToSend = it },
            label = { Text("Mensagem BLE") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (isConnected) {
                    bleViewModel.sendMessage(messageToSend)
                    statusMessage = "Mensagem enviada."
                } else {
                    statusMessage = "Não está conectado via BLE."
                }
            },
            enabled = messageToSend.isNotBlank()
        ) {
            Text("Enviar")
        }

        if (statusMessage.isNotBlank()) {
            Text(statusMessage)
        }

        Button(
            onClick = {
                if (isConnected) {
                    bleViewModel.sendMessage("TESTE|PRINT")
                    statusMessage = "Comando de teste enviado. A aguardar resposta..."

                    coroutineScope.launch {
                        val resposta = bleViewModel.awaitResposta(timeout = 10000)
                        Log.w("BLETestScreen", "resposta do ESP32 $resposta")
                        statusMessage = resposta ?: "⏱ Sem resposta (timeout)"
                    }
                } else {
                    statusMessage = "Não está conectado via BLE."
                }
            }
        ) {
            Text("Enviar Comando de Teste")
        }
    }
}
