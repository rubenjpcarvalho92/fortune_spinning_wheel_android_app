package com.example.fortune_whell_v3


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.lang.Math.toRadians
import kotlin.random.Random
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

class BackupMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FortuneWhell()
        }
    }
}

@Composable
fun FortuneWhell() {
    val navController = rememberNavController()

    // Configurando as rotas de navegação
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen(navController) }
        composable("roulette") { RouletteScreen() } // Tela da roleta
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(navController: NavController) {
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
            var codigoacesso by remember { mutableStateOf("") }

            TextField(
                value = codigoacesso,
                onValueChange = { codigoacesso = it },
                label = { Text("Código de acesso") },
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(color = Color.Blue, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 16.dp),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (codigoacesso.isNotEmpty()) {
                        navController.navigate("main")
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(Color.White)
            ) {
                Text("Login")
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(navController: NavController) {
    Scaffold {
        Image(
            painter = painterResource(id = R.drawable.fundo_mainscreen),
            contentDescription = "Menu Inicial",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedBorderAroundBall {
                navController.navigate("roulette") // Navega para a tela da roleta
            }
        }
    }
}

@Composable
fun AnimatedBorderAroundBall(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAnimation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(285.dp)
            .offset(y = 220.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val borderWidth = 13.dp.toPx()

            rotate(rotationAnimation.value) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Red,
                            Color(0xFFFFA500), // Orange
                            Color.Yellow,
                            Color.Green,
                            Color.Blue,
                            Color(0xFF4B0082), // Indigo
                            Color(0xFF8F00FF), // Violet
                            Color.Red // Fechando o loop
                        )
                    ),
                    radius = (size.minDimension / 2) - borderWidth / 2,
                    style = Stroke(width = borderWidth)
                )
            }
        }

        Button(
            onClick = { onClick() },
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                text = "JOGAR",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun RouletteScreen() {
    val rotationAngle = remember { Animatable(0f) }
    var shouldSpin by remember { mutableStateOf(false) }
    var selectedSlice by remember { mutableStateOf(-1) }
    var isCardVisible by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf(R.drawable.carta_bb) } // Carta padrão
    val resultList = remember { mutableStateListOf<String>() } // Lista de resultados
    val coroutineScope = rememberCoroutineScope()

    // Função para girar a roleta
    fun spinWheel() {
        shouldSpin = true
    }

    // Função para girar automaticamente
    fun triggerSpinAutomatically() {
        coroutineScope.launch {
            delay(500) // Pausa breve para parecer natural
            spinWheel() // Inicia o giro automaticamente
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem de fundo cobrindo toda a tela
        Image(
            painter = painterResource(id = R.drawable.fundo_jogo),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FortuneWheelWithRims(
                rotationAngle = rotationAngle.value % 360,
                selectedSlice = selectedSlice,
                modifier = Modifier.size(500.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { spinWheel() },
                enabled = !shouldSpin && !isCardVisible // Desabilita o botão durante animações
            ) {
                Text(text = "Girar")
            }

            // Exibe os resultados acumulados
            Text(
                text = "Resultados: ${resultList.joinToString(", ")}",
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Exibição da carta girando no eixo vertical
        if (isCardVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .wrapContentSize(Alignment.Center)
            ) {
                VerticalRotatingCard(
                    cardImage = selectedCard,
                    onAnimationEnd = { isCardVisible = false }
                )
            }
        }
    }

    LaunchedEffect(shouldSpin) {
        if (shouldSpin) {
            var randomEnd = rotationAngle.value + Random.nextInt(720, 1800)
            val correctionFactor = 0.014

            // Correção para evitar parar na borda
            if (randomEnd % 22.5 < correctionFactor) {
                randomEnd += correctionFactor.toFloat()
            }

            rotationAngle.animateTo(
                targetValue = randomEnd,
                animationSpec = tween(durationMillis = 3000)
            )

            // Após a rotação, determinar onde parou
            val sliceAngle = 360f / 16
            val adjustedAngle = (360 - ((rotationAngle.value + 90) % 360)) % 360
            val sliceIndex = (adjustedAngle / sliceAngle).toInt()

            selectedSlice = sliceIndex

            when {
                listOf(3, 7, 11, 15).contains(sliceIndex) -> {
                    // Índices com bola: gira automaticamente
                    triggerSpinAutomatically()
                }
                else -> {
                    // Escolhe a carta baseada no índice do slice
                    selectedCard = getCardImageForSlice(sliceIndex)
                    isCardVisible = true

                    // Adiciona o resultado à lista, exceto quando for "Bola"
                    val resultText = getSliceLabel(selectedSlice )
                    if (resultText.isNotEmpty()) {
                        resultList.add(resultText)
                    }
                }
            }

            shouldSpin = false // Libera o botão
        }
    }
}
// Função para obter o nome do slice baseado no índice
fun getSliceLabel(sliceIndex: Int): String {
    val slices = listOf(
        "AM", "GM", "VR", "", // Bola
        "LR", "PC", "RX", "", // Bola
        "AZ", "BB", "EE", "", // Bola
        "VD", "PT", "CI", ""  // Bola
    )
    return slices[sliceIndex]
}
// Representa uma fatia da roleta
data class WheelSlice(
    val color: Color,
    val label: String
)
fun getCardImageForSlice(sliceIndex: Int): Int {
    return when (sliceIndex) {
        0 -> R.drawable.carta_am // Carta para "AM"
        1 -> R.drawable.carta_gm // Carta para "GM"
        2 -> R.drawable.carta_vr // Carta para "VR"
        4 -> R.drawable.carta_lr // Carta para "LR"
        5 -> R.drawable.carta_pc // Carta para "PC"
        6 -> R.drawable.carta_rx // Carta para "RX"
        8 -> R.drawable.carta_az // Carta para "AZ"
        9 -> R.drawable.carta_bb // Carta para "BB"
        10 -> R.drawable.carta_ee // Carta para "EE"
        12 -> R.drawable.carta_vd // Carta para "VD"
        13 -> R.drawable.carta_pt // Carta para "PT"
        14 -> R.drawable.carta_ci // Carta para "CI"
        else -> R.drawable.carta_am // Carta padrão (caso o índice não seja mapeado)
    }
}
@Composable
fun FortuneWheelWithRims(rotationAngle: Float, selectedSlice: Int, modifier: Modifier = Modifier) {
    val slices = listOf(
        WheelSlice(Color(0xFFFFFF00), "AM"),
        WheelSlice(Color(0xFFFF4FA7), "GM"),
        WheelSlice(Color(0xFFFF0000), "VR"),
        WheelSlice(Color(0xFF000000), ""), // Bola
        WheelSlice(Color(0xFFFF6400), "LR"),
        WheelSlice(Color(0xFFFFFFFF), "PC"),
        WheelSlice(Color(0xFF800080), "RX"),
        WheelSlice(Color(0xFF000000), ""), // Bola
        WheelSlice(Color(0xFF0000FF), "AZ"),
        WheelSlice(Color(0xFFFF4FA7), "BB"),
        WheelSlice(Color(0xFFDAA520), "EE"),
        WheelSlice(Color(0xFF000000), ""), // Bola
        WheelSlice(Color(0xFF008000), "VD"),
        WheelSlice(Color(0xFFFFFFFF), "PT"),
        WheelSlice(Color(0xFF767676), "CI"),
        WheelSlice(Color(0xFF000000), "") // Bola
    )

    val imageBitmap = ImageBitmap.imageResource(id = R.drawable.bola)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = min(size.width, size.height) / 2
            val sliceAngle = 360f / slices.size

            drawGoldenRim(radius)
            drawBlackRim(radius - 5.dp.toPx())

            rotate(rotationAngle) {
                slices.forEachIndexed { index, slice ->
                    drawBrightSliceWithImageAndText(
                        index = index,
                        radius = radius - 10.dp.toPx(),
                        sliceAngle = sliceAngle,
                        slice = slice,
                        image = if (index in listOf(3, 7, 11, 15)) imageBitmap else null
                    )
                    drawVividBlackLineBetweenSlices(index, radius - 10.dp.toPx(), sliceAngle)
                }

                // Adiciona os pinos dourados entre as fatias
                slices.indices.forEach { index ->
                    drawGoldenPinBetweenSlices(index, radius - 10.dp.toPx(), sliceAngle)
                }
            }

            drawGoldenBlackCenter(radius / 5)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSmallerGoldenBlackPin(size.minDimension / 2 - 20.dp.toPx())
        }

        // Adiciona a caixa de texto no centro
        val selectedText = if (selectedSlice != -1 && slices[selectedSlice].label.isNotEmpty()) {
            slices[selectedSlice].label
        } else {
            ""
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.Center)
        ) {
            Text(
                text = selectedText, // Exibe a string do slice selecionado
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
// Componente para a carta rotativa no eixo vertical
@Composable
fun VerticalRotatingCard(cardImage: Int, onAnimationEnd: () -> Unit) {
    val rotationAngle = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotationAngle.animateTo(
            targetValue = 360f, // Gira uma vez completa no eixo vertical
            animationSpec = tween(durationMillis = 2000)
        )
        delay(500) // Pausa breve antes de desaparecer
        onAnimationEnd()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(300.dp)
            .graphicsLayer {
                rotationY = rotationAngle.value
                cameraDistance = 12 * density
            }
    ) {
        Image(
            painter = painterResource(id = cardImage), // Imagem baseada no índice
            contentDescription = "Carta",
            modifier = Modifier.fillMaxSize()
        )
    }
}
private fun DrawScope.drawGoldenPinBetweenSlices(index: Int, radius: Float, sliceAngle: Float) {
    val pinRadius = 5.dp.toPx() // Tamanho do pino dourado

    // Calcula a posição do pino com base no ângulo rotacional
    val pinPosition = Offset(
        x = center.x + radius * cos(toRadians(sliceAngle * index.toDouble())).toFloat(),
        y = center.y + radius * sin(toRadians(sliceAngle * index.toDouble())).toFloat()
    )

    // Desenha o pino dourado na posição calculada
    drawCircle(
        color = Color(0xFFFFD700), // Cor dourada
        radius = pinRadius,
        center = pinPosition
    )
}
private fun DrawScope.drawBrightSliceWithImageAndText(index: Int, radius: Float, sliceAngle: Float, slice: WheelSlice, image: ImageBitmap?) {
    // Desenha a fatia
    drawArc(
        color = slice.color,
        startAngle = sliceAngle * index,
        sweepAngle = sliceAngle,
        useCenter = true,
        topLeft = center.copy(x = center.x - radius, y = center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )

    // Calcula a posição do texto
    val textAngle = sliceAngle * index + sliceAngle / 2
    val textRadius = radius - 50.dp.toPx() // Ajuste da posição do texto
    val textCenter = Offset(
        x = center.x + textRadius * cos(toRadians(textAngle.toDouble())).toFloat(),
        y = center.y + textRadius * sin(toRadians(textAngle.toDouble())).toFloat()
    )

    // Desenha o texto alinhado com a fatia
    if (slice.label.isNotEmpty()) {
        drawContext.canvas.nativeCanvas.apply {
            save()
            translate(textCenter.x, textCenter.y)
            rotate((textAngle + 90).toFloat(), 0f, 0f) // Adiciona 180º (90º + 90º para ajustar)
            drawText(
                slice.label,
                0f,
                0f,
                android.graphics.Paint().apply {
                    color = if (index == 5 || index == 13 || index== 0)
                        android.graphics.Color.BLACK
                    else
                        android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 40f
                }
            )
            restore()
        }
    }

    // Desenha a imagem, se houver
    image?.let {
        val imageRadius = radius - 80.dp.toPx() // Ajusta a posição da imagem
        val imageCenter = Offset(
            x = center.x + imageRadius * cos(toRadians(textAngle.toDouble())).toFloat(),
            y = center.y + imageRadius * sin(toRadians(textAngle.toDouble())).toFloat()
        )

        drawImage(
            image = it,
            topLeft = Offset(
                x = imageCenter.x - it.width / 2f,
                y = imageCenter.y - it.height / 2f
            )
        )
    }
}
private fun DrawScope.drawVividBlackLineBetweenSlices(index: Int, radius: Float, sliceAngle: Float) {
    val lineWidth = 3.dp.toPx()

    rotate(sliceAngle * index) {
        drawLine(
            color = Color.Black,
            start = center,
            end = Offset(center.x, center.y - radius),
            strokeWidth = lineWidth
        )
    }
}
private fun DrawScope.drawGoldenRim(radius: Float) {
    val rimWidth = 35.dp.toPx()
    val goldenGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500)),
        center = center,
        radius = radius
    )

    drawCircle(
        brush = goldenGradient,
        radius = radius,
        style = Stroke(width = rimWidth)
    )
}
private fun DrawScope.drawBlackRim(radius: Float) {
    val rimWidth = 10.dp.toPx()

    drawCircle(
        color = Color.Black,
        radius = radius,
        style = Stroke(width = rimWidth)
    )
}
private fun DrawScope.drawSmallerGoldenBlackPin(radius: Float) {
    val pinHeight = radius / 6
    val pinWidth = radius / 12

    val goldenGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B)),
        center = center.copy(y = center.y - radius - pinHeight / 2 - 10.dp.toPx()), // Ajuste puxando o pino para cima
        radius = pinHeight
    )

    val path = Path().apply {
        moveTo(center.x, center.y - radius + pinHeight - 10.dp.toPx()) // Ajuste no ponto inferior do triângulo
        lineTo(center.x - pinWidth / 2, center.y - radius - pinHeight / 2 - 10.dp.toPx()) // Top left
        lineTo(center.x + pinWidth / 2, center.y - radius - pinHeight / 2 - 10.dp.toPx()) // Top right
        close()
    }

    drawPath(path = path, brush = goldenGradient)

    drawCircle(
        color = Color.Black,
        radius = pinWidth / 2,
        center = Offset(center.x, center.y - radius - pinHeight / 2 - 10.dp.toPx()) // Ajuste do círculo preto
    )
}
private fun DrawScope.drawGoldenBlackCenter(radius: Float) {
    val metallicGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFF424242)),
        center = center,
        radius = radius
    )

    drawCircle(
        brush = metallicGradient,
        radius = radius
    )
}
