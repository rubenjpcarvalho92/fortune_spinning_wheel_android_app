package com.example.fortune_whell_v3.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fortune_whell_v3.R
import com.example.fortune_whell_v3.api.services.ApiServices
import com.example.fortune_whell_v3.resources.LoginResource
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(navController: NavController, maquinaViewModel: MaquinaViewModel) {
    val clienteService = ApiServices.clienteService
    val adminService = ApiServices.adminService
    val funcionarioService = ApiServices.funcionarioService
    val loginService = ApiServices.loginService
    val maquinaService = ApiServices.maquinaService

    val numeroSerie = maquinaViewModel.numeroSerie

    var username by remember { mutableStateOf("") }
    var codigoAcesso by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold {
        Image(
            painter = painterResource(id = R.drawable.fundo_inicial),
            contentDescription = "Initial Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
        ) {
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(color = Color.Blue, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(0.5f),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = codigoAcesso,
                onValueChange = { codigoAcesso = it },
                label = { Text("Código de acesso") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(color = Color.Blue, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(0.5f),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // A tua lógica de login aqui
                },
                enabled = true, // <--- Garante que está sempre ativo
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Blue
                ),
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.5f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Blue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Login", fontWeight = FontWeight.Bold)
                }
            }


            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            IconButton(
                onClick = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                enabled = true,
                modifier = Modifier.size(100.dp)
            )  {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Login",
                        tint = Color.Unspecified,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
