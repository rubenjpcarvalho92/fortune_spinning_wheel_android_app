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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fortune_whell_v3.R
import com.example.fortune_whell_v3.api.services.ApiServices
import com.example.fortune_whell_v3.resources.LoginResource
import com.example.fortune_whell_v3.Utils.DeviceUtils.Companion.getAndroidId
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(navController: NavController) {
    val clienteService = ApiServices.clienteService
    val adminService = ApiServices.adminService
    val funcionarioService = ApiServices.funcionarioService
    val loginService = ApiServices.loginService
    val maquinaService = ApiServices.maquinaService

    val numeroSerie = getAndroidId(LocalContext.current)

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
                    if (username.isNotEmpty() && codigoAcesso.isNotEmpty() && numeroSerie.isNotEmpty()) {
                        isLoading = true
                        coroutineScope.launch {
                            val nif = username.toIntOrNull()
                            if (nif == null) {
                                errorMessage = "NIF inválido"
                                isLoading = false
                                return@launch
                            }

                            try {
                                val clienteResponse = clienteService.getCliente(nif)
                                val cliente = clienteResponse.body()

                                if (clienteResponse.isSuccessful && cliente?.passwordCliente == codigoAcesso) {
                                    val maquinaResponse = maquinaService.getMaquina(numeroSerie)
                                    val maquina = maquinaResponse.body()

                                    if (maquinaResponse.isSuccessful && maquina?.status == "Activa") {
                                        val sucesso = LoginResource.registerLogin(nif, numeroSerie, "Sucesso", "Cliente", loginService)
                                        if (sucesso) {
                                            navController.navigate("main")
                                        } else {
                                            errorMessage = "Erro ao registar login"
                                        }
                                    } else {
                                        LoginResource.registerLogin(nif, numeroSerie, "Falha", "Cliente", loginService)
                                        errorMessage = "Máquina não operacional"
                                    }
                                    isLoading = false
                                    return@launch
                                }

                                val adminResponse = adminService.getAdmin(nif)
                                val admin = adminResponse.body()
                                if (adminResponse.isSuccessful && admin?.passwordAdmin == codigoAcesso) {
                                    LoginResource.registerLogin(nif, numeroSerie, "Sucesso", "Admin", loginService)
                                    navController.navigate("admin")
                                    isLoading = false
                                    return@launch
                                }

                                val funcionarioResponse = funcionarioService.getFuncionario(nif)
                                val funcionario = funcionarioResponse.body()
                                if (funcionarioResponse.isSuccessful && funcionario?.passwordFuncionario == codigoAcesso) {
                                    LoginResource.registerLogin(nif, numeroSerie, "Sucesso", "Funcionario", loginService)
                                    navController.navigate("config")
                                    isLoading = false
                                    return@launch
                                }

                                LoginResource.registerLogin(nif, numeroSerie, "Falha", "desconhecido", loginService)
                                errorMessage = "Credenciais inválidas!"

                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Erro no login: ${e.message}", e)
                                errorMessage = "Erro ao comunicar com o servidor."
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Preencha todos os campos e verifique o número de série!"
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(Color.White)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Blue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Login")
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}
