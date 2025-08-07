package com.example.fortune_whell_v3.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fortune_whell_v3.R
import com.example.fortune_wheel_v3.components.AnimatedButtonWithBorder
import kotlinx.coroutines.delay
import com.example.fortune_whell_v3.resources.RouletteResource.getPrizeGrid
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.fortune_whell_v3.api.resources.APIResource
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(navController: NavController, maquinaViewModel: MaquinaViewModel = viewModel()) {
    val coroutineScope = rememberCoroutineScope()
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isInactive by remember { mutableStateOf(false) }
    var showPopupPrizes by remember { mutableStateOf(false) }
    var showPopupMaquinaInativa by remember { mutableStateOf(false) }

    var touchCount by remember { mutableStateOf(0) }
    var lastTouchTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            if (System.currentTimeMillis() - lastInteractionTime > 1 * 300 * 1_000L && !isInactive) {
                isInactive = true
                navController.navigate("demo")
            }
        }
    }

    Scaffold {
        Image(
            painter = painterResource(id = R.drawable.fundo_mainscreen),
            contentDescription = "Menu Inicial",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        lastInteractionTime = System.currentTimeMillis()
                        isInactive = false
                    }
                }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            // Botão PLAY
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 20.dp)
            ) {
                AnimatedButtonWithBorder(
                    text = "",
                    imageResId = R.drawable.play_button,
                    isAnimated = true,
                    verticalOffset = 220.dp
                ) {
                    coroutineScope.launch {
                        try {
                            val maquinaAtualizada = APIResource.buscarDadosMaquinaRolleta(maquinaViewModel.numeroSerie)

                            if (maquinaAtualizada?.status?.lowercase() == "activa") {
                                maquinaViewModel.atualizarMaquina(maquinaAtualizada)
                                navController.navigate("roulette")
                            } else {
                                showPopupMaquinaInativa = true
                            }
                        } catch (e: Exception) {
                            println("❌ Erro ao verificar estado da máquina: ${e.message}")
                        }
                    }
                }
            }

            // Botão "Ver Prêmios"
            Button(
                onClick = { showPopupPrizes = true },
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 30.dp),
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.premios_icon),
                    contentDescription = "Botão Lista Prêmios",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            // Pop-up de prémios
            if (showPopupPrizes) {
                val grid = maquinaViewModel.setup?.let { setup ->
                    maquinaViewModel.maquina?.let { maquina ->
                        getPrizeGrid(setup, maquina)
                    }
                } ?: emptyList()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.White, shape = MaterialTheme.shapes.medium)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Lista de Prêmios", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            grid.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    row.forEach { cell ->
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .background(Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (cell) {
                                                is String -> {
                                                    if (cell.startsWith("http")) {
                                                        val painter = rememberAsyncImagePainter(model = cell)
                                                        Image(
                                                            painter = painter,
                                                            contentDescription = null,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Fit
                                                        )
                                                    } else {
                                                        Text(
                                                            text = cell,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = Color(0xFFDAA520)
                                                        )
                                                    }
                                                }

                                                is Int -> {
                                                    Image(
                                                        painter = painterResource(id = cell),
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPopupPrizes = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color(0xFFDAA520)
                            )
                        ) {
                            Text("Fechar", color = Color(0xFFDAA520))
                        }
                    }
                }
            }

            // Pop-up de máquina inativa
            if (showPopupMaquinaInativa) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.White, shape = MaterialTheme.shapes.medium)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ Máquina indisponível",
                            color = Color.Red,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Esta máquina está inativa no momento.\nContacte o operador.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPopupMaquinaInativa = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color(0xFFDAA520)
                            )
                        ) {
                            Text("Fechar")
                        }
                    }
                }
            }

            // 🔒 Atalho secreto visível
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 30.dp)
                    .background(Color.Red.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            val now = System.currentTimeMillis()
                            if (now - lastTouchTime < 1500L) {
                                touchCount++
                            } else {
                                touchCount = 1
                            }
                            lastTouchTime = now

                            if (touchCount >= 10) {
                                touchCount = 0
                                navController.navigate("login")
                            }
                        }
                    }
            )
        }
    }
}
