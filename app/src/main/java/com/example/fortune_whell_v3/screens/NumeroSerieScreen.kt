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
    var input by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Insira o número de série da máquina", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Número de Série") }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (input.isNotBlank()) {
                maquinaViewModel.definirNumeroSerie(input)
                navController.navigate("info") {
                    popUpTo("definirNumeroSerie") { inclusive = true }
                }
            }
        }) {
            Text("Continuar")
        }
    }
}
