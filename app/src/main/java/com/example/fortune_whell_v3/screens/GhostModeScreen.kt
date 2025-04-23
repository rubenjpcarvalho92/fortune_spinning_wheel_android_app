package com.example.fortune_whell_v3.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fortune_wheel_v3.components.AnimatedButtonWithBorder
import com.example.fortune_whell_v3.R
import com.example.fortune_whell_v3.resources.RouletteResource
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun GhostModeScreen(navController: NavController, maquinaViewModel: MaquinaViewModel) {
    val rotationAngle = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val resultSlices = remember { mutableStateListOf<Int>() }
    val totalSlots = remember { Random.nextInt(5, 11) }
    var spinsRemaining by remember { mutableStateOf(totalSlots) }
    var isCardVisible by remember { mutableStateOf(false) }
    val pointerAngle = remember { Animatable(0f) }
    var sairParaMain by remember { mutableStateOf(false) }

    val indicesBola = listOf(3, 7, 11, 15)
    val labels = maquinaViewModel.labels

    fun getSliceLabel(index: Int): String = labels.getOrNull(index) ?: ""

    fun getCardImageForSlice(index: Int): Int {
        return RouletteResource.getCardImageForSlice(index, maquinaViewModel.maquina ?: return R.drawable.carta_bb)
    }

    fun simulateSpin() {
        coroutineScope.launch {
            delay(1000)
            if (sairParaMain) return@launch

            val sliceIndex = Random.nextInt(0, 16)
            val targetAngle = RouletteResource.calculatePositionByIndex(sliceIndex, rotationAngle.value)

            rotationAngle.animateTo(
                targetValue = targetAngle,
                animationSpec = tween(5000, easing = RouletteResource.cubicBezier(0.25f, 1f, 0.36f, 1f))
            )

            if (sairParaMain) return@launch

            if (sliceIndex !in indicesBola) {
                spinsRemaining--
                resultSlices.add(sliceIndex)
                if (resultSlices.size > totalSlots) resultSlices.removeAt(0)
            }

            isCardVisible = true
            delay(3000)
            if (sairParaMain) return@launch
            isCardVisible = false

            if (spinsRemaining == 0) {
                delay(3000)
                if (!sairParaMain) navController.navigate("main")
            } else {
                simulateSpin()
            }
        }
    }

    LaunchedEffect(Unit) {
        simulateSpin()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fundo
        Image(
            painter = painterResource(id = R.drawable.fundo_jogo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contador de créditos
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = -30.dp)
                .align(Alignment.BottomEnd)
        ) {
            Image(
                painter = painterResource(id = R.drawable.credit_counter_v5),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = spinsRemaining.toString(),
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 42.sp,
                modifier = Modifier.align(Alignment.Center).offset(y = 32.dp)
            )
        }

        // Roleta
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            FortuneWheelWithRims(
                rotationAngle = rotationAngle.value % 360,
                pointerAngle = pointerAngle.value,
                modifier = Modifier
                    .size(500.dp)
                    .offset(y = (-80).dp, x = (-50).dp),
                maquinaViewModel = maquinaViewModel
            )
        }

        // Botão dos prémios
        Button(
            onClick = { },
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 30.dp),
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.premios_icon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        // Botão central (visual)
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 20.dp)
                .offset(x = 20.dp)
                .align(Alignment.BottomCenter)
        ) {
            AnimatedButtonWithBorder(
                text = "",
                imageResId = R.drawable.soccer_ball_color,
                isAnimated = false,
                verticalOffset = (-80).dp,
                horizontalOffset = (-50).dp,
                isEnabled = false
            ) {}
        }

        // Coluna de cromos
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp, top = 20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(totalSlots) { index ->
                    if (index < resultSlices.size) {
                        val sliceIndex = resultSlices[index]
                        val label = getSliceLabel(sliceIndex)
                        val imageRes = getCardImageForSlice(sliceIndex)

                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(80.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = "Cromo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .border(2.dp, Color(0xFFDAA520))
                                )
                                Text(
                                    text = label,
                                    color = Color.Yellow,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(4.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Carta giratória
        if (isCardVisible && resultSlices.isNotEmpty()) {
            val image = getCardImageForSlice(resultSlices.last())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .wrapContentSize(Alignment.Center)
            ) {
                VerticalRotatingCard(
                    cardImage = image,
                    onAnimationEnd = { isCardVisible = false }
                )
            }
        }

        // ⚠️ Overlay transparente para saída imediata
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures {
                        sairParaMain = true
                        navController.navigate("main")
                    }
                }
        )
    }
}
