package com.example.fortune_whell_v3.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import java.lang.Math.toRadians
import kotlin.random.Random
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fortune_wheel_v3.components.AnimatedButtonWithBorder
import com.example.fortune_whell_v3.R
import com.example.fortune_whell_v3.Utils.DeviceUtils.Companion.getAndroidId
import com.example.fortune_whell_v3.api.models.Maquina
import com.example.fortune_whell_v3.api.models.Talao
import com.example.fortune_whell_v3.api.resources.APIResource
import kotlinx.coroutines.delay
import kotlin.math.*
import com.example.fortune_whell_v3.resources.RouletteResource
import com.example.fortune_whell_v3.viewmodel.BLEViewModel
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel

@Composable
fun RouletteScreen(navController: NavController, bleViewModel: BLEViewModel = viewModel(), maquinaViewModel: MaquinaViewModel = viewModel()) {
    val rotationAngle = remember { Animatable(0f) }
    var shouldSpin by remember { mutableStateOf(false) }
    var selectedSlice by remember { mutableStateOf(0) }
    var isCardVisible by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf(R.drawable.carta_bb) } // Carta padrão
    val resultList = remember { mutableStateListOf<Int>() } // Lista de resultados (IDs dos cromos)
    val slotLabels = remember { mutableStateListOf<String>() } // Rótulos dos slots
    var creditValue by remember { mutableStateOf(0) } // Valor inicial de crédito
    var slotCount by remember { mutableStateOf(10) } // Número fixo de slots, definido pelo pop-up
    var showPopupPayment1 by remember { mutableStateOf(false) } // Controla a exibição do pop-up final
    var showPopupPayment2 by remember { mutableStateOf(false) } // Controla a exibição do pop-up final
    var showPopupPayment by remember { mutableStateOf(true) } // Agora mostra diretamente o pop-up de pagamento
    var isManualSpin by remember { mutableStateOf(false) } // Indica se foi uma interação manual
    val numeroSerie = getAndroidId(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()
    val pointerAngle = remember { Animatable(0f) } // Posição do pino
    var showPopupPrizes by remember { mutableStateOf(false) }
    var isButtonExitEnabled by remember { mutableStateOf(false) }
    val prizeListToPrint = remember { mutableStateListOf<String>() }
    var isSpinning by remember { mutableStateOf(false) }
    var isSpinCompleted by remember { mutableStateOf(false) }
    val cardRotationAngle = remember { Animatable(0f) }
    var talaoCriado by remember { mutableStateOf<Talao?>(null) }
    var maquina by remember { mutableStateOf<Maquina?>(null) }
    var showPopupLevantamento by remember { mutableStateOf(false) }

    val levantamentoEmCurso = remember { mutableStateOf(false) }

    var esperandoConfirmacaoArduino = remember { mutableStateOf(false) }
    val bleMessage by bleViewModel.receivedMessage.collectAsState()

    // Mede a velocidade da roleta
    var lastRotationValue by remember { mutableStateOf(0f) }
    var roletaVelocidade by remember { mutableStateOf(0f) }




    // Função para gerir a permissão de rodar
    fun spinWheel() {
        if (creditValue > 0) {
            shouldSpin = true
            isManualSpin = true // Marca como giro manual
        }
    }

    fun handleExitWithBLE() {
        Log.d("BLE", "A sair ")
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Fundo da tela

        Image(
            painter = painterResource(id = R.drawable.fundo_jogo),
            contentDescription = "Fundo Roleta",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Roleta no centro


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
                    .offset(y = (-80).dp)
                    .offset(x = (-50).dp),
                    maquinaViewModel = maquinaViewModel
            )
        }

        // Slots de cromos no lado direito
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
                repeat(slotCount) { index ->
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(80.dp)
                    ) {
                        if (index < resultList.size) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Imagem do cromo
                                Image(
                                    painter = painterResource(id = resultList[index]),
                                    contentDescription = "Cromo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .border(2.dp, Color(0xFFDAA520))
                                )
                                // Texto centralizado
                                Text(
                                    text = slotLabels[index],
                                    color = Color.Yellow,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(4.dp)
                                        .align(Alignment.Center) // Centraliza no cromo
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Ajustado para descer todos os botões um pouco mais
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp), // Ajustado para empurrar os botões mais para baixo
                horizontalArrangement = Arrangement.SpaceBetween, // Distribui os botões igualmente nas extremidades
                verticalAlignment = Alignment.Bottom
            ) {
                // 🔹 Botão Lista de Prêmios (Maior)
                Button(
                    onClick = { showPopupPrizes = true },
                    modifier = Modifier.size(200.dp), // Aumentado o tamanho deste botão
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.premios_icon),
                        contentDescription = "Botão Lista Prêmios",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                // 🔹 Botão de Girar a Roleta (Centralizado)
                Box(
                    modifier = Modifier
                        .size(250.dp) // Tamanho ajustado para dar destaque
                        .padding(bottom = 20.dp) // Ajustado para descer mais
                        .offset(x = 20.dp)
                ) {
                    AnimatedButtonWithBorder(
                        text = "",
                        imageResId = R.drawable.soccer_ball_color,
                        isAnimated = false,
                        verticalOffset = (-80).dp,
                        horizontalOffset = (-50).dp,
                        isEnabled = !shouldSpin && !isSpinning && creditValue > 0 // 🔹 Só habilita quando não está girando
                    ) {
                        spinWheel()
                    }
                }

                // Imagem do credit_counter em baixo
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = -30.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.credit_counter_v5),
                        contentDescription = "Credit Counter",
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = creditValue.toString(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 42.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 32.dp)
                    )
                }
            }
        }

        //*****************************************************************//
        //******************************POP UPS****************************//
        // Exibição da carta girando no eixo vertical
        if (isCardVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .pointerInput(Unit) {}
                    .wrapContentSize(Alignment.Center)
            ) {
                VerticalRotatingCard(
                    cardImage = selectedCard,
                    onAnimationEnd = {
                        isCardVisible = false

                        if (creditValue == 0 && !levantamentoEmCurso.value && prizeListToPrint.isNotEmpty()) {
                            showPopupLevantamento = true
                            coroutineScope.launch {
                                RouletteResource.realizarLevantamentoFinal(
                                    bleViewModel = bleViewModel,
                                    numeroSerie = numeroSerie,
                                    prizeListToPrint = prizeListToPrint,
                                    levantamentoEmCurso = levantamentoEmCurso,
                                    esperandoConfirmacaoArduino = esperandoConfirmacaoArduino
                                ) {
                                    showPopupLevantamento = false
                                    navController.navigate("main") // 🔁 Volta ao ecrã principal após desaparecer popup
                                }
                            }
                        }
                    }
                )
            }
        }



        // pop up com os menus iniciais de pagamento
        if (showPopupPayment) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Método de Pagamento",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 🔹 Botão de Método 1
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Transparent)
                                .pointerInput(Unit) {
                                    detectTapGestures { showPopupPayment1 = true }
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.mbway_icon),
                                contentDescription = "Método 1",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // 🔹 Botão de Método 2
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Transparent)
                                .pointerInput(Unit) {
                                    detectTapGestures { showPopupPayment2 = true }
                                }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.dinheiro_icon),
                                contentDescription = "Método 2",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showPopupPayment = false
                            navController.navigate("main")

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color(0xFFDAA520)
                        )
                    ) {
                        Text("Sair", color = Color(0xFFDAA520))
                    }
                }
            }
        }
        // 🔹 Pop-up Método de Pagamento 1
        if (showPopupPayment1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Você escolheu Método 1",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // 🔹 Adicionando duas imagens no pop-up
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.nota_10euros),
                            contentDescription = "QR Code MB WAY",
                            modifier = Modifier.size(100.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.nota_5euros),
                            contentDescription = "Número MB WAY",
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔹 Botões "Fechar" e "Voltar"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { showPopupPayment1 = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color(0xFFDAA520)
                            )
                        ) {
                            Text("Voltar", color = Color(0xFFDAA520))
                        }
                    }
                }
            }
        }
        // 🔹 Pop-up Método de Pagamento 2
        if (showPopupPayment2) {
            var inputValue by remember { mutableStateOf("") }
            val isValidInput = inputValue.toIntOrNull()?.let { it in 1..10 } ?: false

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Você escolheu Método 2",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // 🔹 Caixa de texto para inserir um número entre 1 e 10
                    TextField(
                        value = inputValue,
                        onValueChange = { inputValue = it.filter { char -> char.isDigit() } },
                        singleLine = true,
                        placeholder = { Text("Digite um valor (1-10)") }
                    )

                    // 🔹 Botões "Jogar" e "Voltar" lado a lado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 🔹 Só exibe o botão "Jogar" se o valor for válido
                        if (isValidInput) {
                            Button(
                                onClick = {
                                    creditValue = inputValue.toIntOrNull() ?: 0 // Define o valor no contador
                                    showPopupPayment2 = false
                                    showPopupPayment = false
                                    slotCount=creditValue
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color(0xFFDAA520)
                                )
                            ) {
                                Text("Jogar", color = Color(0xFFDAA520))
                            }
                        }

                        // 🔹 Botão "Voltar" para retornar ao pop-up do método de pagamento
                        Button(
                            onClick = {
                                showPopupPayment2 = false
                                showPopupPayment = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color(0xFFDAA520)
                            )
                        ) {
                            Text("Voltar", color = Color(0xFFDAA520))
                        }
                    }
                }
            }
        }
        // Pop up com a grelha de prémios
        if (showPopupPrizes) {
            //chamada da funcão para grelha de premios no menu dos premios, deverá ser dinâmica
            // de futuro vai aqui entrar qual o tipo de roleta
            val prizeGrid = maquinaViewModel.setup?.let { setup ->
                maquinaViewModel.maquina?.let { maquina ->
                    RouletteResource.getPrizeGrid(setup, maquina)
                }
            } ?: emptyList()

            val testImage = "http://37.27.191.181/uploads/7913bf7a0fb5e5df/Brinde_AM.jpg"

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

                    // Grelha transparente de 6x4
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prizeGrid?.let { grid ->
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
                                                        val painter = rememberAsyncImagePainter(
                                                            model = cell,
                                                            onError = {
                                                                Log.e("Coil", "Erro ao carregar imagem: $cell", it.result.throwable)
                                                            }
                                                        )

                                                        Image(
                                                            painter = painter,
                                                            contentDescription = "Imagem de brinde",
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
                                                        contentDescription = "Imagem local",
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
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
        //Pop up fim de jogo
        if (showPopupLevantamento) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(Color.White, shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    // 🔄 Imagem de fundo DENTRO do popup
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.fundo_premios), // tua imagem de fundo
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "A levantar prémios...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }

    //*****************************************************************//
    //******************************THREADS****************************//
    // Lógica de controle do giro
    LaunchedEffect(shouldSpin) {
        if (shouldSpin && !isSpinning) {
            isSpinning = true
            isSpinCompleted = false
            isCardVisible = false

            // 🔹 Buscar dados da máquina
            val maquinaLocal = try {
                APIResource.buscarDadosMaquinaRolleta(numeroSerie)
            } catch (e: Exception) {
                Log.e("API", "❌ Erro ao buscar dados da máquina: ${e.message}")
                null
            }

            if (maquinaLocal == null) {
                isSpinning = false
                shouldSpin = false
                return@LaunchedEffect
            }

            maquina = maquinaLocal

            // 🔹 Criar talão (se ainda não criado)
            if (talaoCriado == null) {
                try {
                    talaoCriado = APIResource.registarTalao(maquinaLocal, creditValue)
                    if (talaoCriado == null) {
                        Log.e("TALAO", "❌ Talão não foi criado corretamente.")
                        isSpinning = false
                        shouldSpin = false
                        return@LaunchedEffect
                    }
                } catch (e: Exception) {
                    Log.e("TALAO", "❌ Erro ao criar talão: ${e.message}")
                    isSpinning = false
                    shouldSpin = false
                    return@LaunchedEffect
                }
            }

            if (isManualSpin) creditValue--

            if (talaoCriado != null) {
                lastRotationValue = rotationAngle.value

                // ✅ Garantir que setup está carregado
                maquinaViewModel.setup?.let { setup ->

                    // 🎯 Calcular índice do prémio com base no setup
                    val prizeIndex = RouletteResource.calculatePrizeIndex(maquinaLocal, maquinaViewModel.premios)

                    // 🔁 Giros adicionais
                    val indicesBola = listOf(3, 7, 11, 15)
                    val label = maquinaViewModel.labels.getOrNull(prizeIndex) ?: ""
                    val girosAdicionais = when (label) {
                        "CI" -> 1
                        "VR" -> 2
                        "AZ" -> 3
                        "LR" -> 4
                        "VD" -> 5
                        "AM" -> 6
                        "RX" -> 7
                        "ARC" -> 8
                        else -> if ((0..100).random() < 25) 1 else 0
                    }

                    repeat(girosAdicionais) {
                        val randomBolaIndex = indicesBola.random()
                        spinTheWhell(randomBolaIndex, rotationAngle)
                        delay(500)
                    }

                    spinTheWhell(prizeIndex, rotationAngle)
                    while (roletaVelocidade > 5f) {
                        pointerAngle.animateTo(-2f, animationSpec = tween(50))
                        pointerAngle.animateTo(2f, animationSpec = tween(50))
                        delay(50)
                    }

                    val valorPremio = setup.run {
                        listOf(
                            P0, P1, P2, P3, P4, P5, P6, P7,
                            P8, P9, P10, P11, P12, P13, P14, P15
                        ).getOrNull(prizeIndex) ?: 0f
                    }

                    val novoAtribuido= maquinaLocal.atribuidoTotal + valorPremio
                    val novoApostado=maquinaLocal.apostadoTotal + maquinaLocal.valorAposta

                    val maquinaAtualizada = maquinaLocal.copy(
                        atribuidoTotal =novoAtribuido,
                        apostadoTotal = novoApostado,
                        taxaGanhoActual = (novoApostado-novoAtribuido) / novoApostado,
                        taxaGanhoParcial = (novoApostado-novoAtribuido) / novoApostado,
                        apostadoParcial = novoApostado,
                        atribuidoParcial = novoAtribuido
                    )

                    try {
                        APIResource.actualizaMaquina(maquinaAtualizada)
                    } catch (e: Exception) {
                        Log.e("API", "❌ Erro ao atualizar máquina: ${e.message}")
                        // Opcional: marca o estado da máquina com erro, ou mostra popup
                    }

                    try {
                        APIResource.registarPremio(maquinaViewModel.labels, maquinaViewModel.premios, prizeIndex, talaoCriado!!.numeroSerie)
                    } catch (e: Exception) {
                        Log.e("API", "❌ Erro ao atualizar máquina: ${e.message}")
                        // Opcional: marca o estado da máquina com erro, ou mostra popup
                    }


                    selectedSlice = prizeIndex
                    isCardVisible = true
                    isSpinCompleted = true
                    isButtonExitEnabled = true
                    shouldSpin = false
                    isSpinning = false

                    prizeListToPrint.add(maquinaViewModel.labels[prizeIndex])

                } ?: run {
                    println("⚠️ Setup não carregado.")
                    isSpinning = false
                    shouldSpin = false
                }
            } else {
                println("❌ Erro ao criar talão.")
                isSpinning = false
                shouldSpin = false
            }
        }
    }

// 🔹 Exibe o cromo somente quando a roleta termina
    LaunchedEffect(isSpinCompleted) {
        if (isSpinCompleted) {

            maquinaViewModel.maquina?.let { maquina ->
                selectedCard = RouletteResource.getCardImageForSlice(selectedSlice, maquina)
            }
            isCardVisible = true
            cardRotationAngle.snapTo(0f) // Reinicia a animação da carta

            cardRotationAngle.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 2000)
            )

            delay(1000) // Pequeno delay para exibição do cromo
            isCardVisible = false

            // 🔹 Preenche o slot após a animação do cromo
            maquinaViewModel.maquina?.let { maquina ->
                addToSlotResults(RouletteResource, selectedSlice, resultList, slotLabels, slotCount, maquina, maquinaViewModel.labels)
            }
        }
    }

    LaunchedEffect(rotationAngle.value) {
        roletaVelocidade = abs(rotationAngle.value - lastRotationValue)
        lastRotationValue = rotationAngle.value
    }

    // 🔹 Animação do pino batendo nos pregos ao desacelerar
    LaunchedEffect(roletaVelocidade) {
        if (roletaVelocidade in 1f..2000f) { // Ajusta o intervalo para o efeito
            val job = coroutineScope.launch {
                while (roletaVelocidade > 1f) { // Mantém vibração enquanto desacelera
                    val intensidadeVibracao = (roletaVelocidade / 6).coerceIn(1f, 8f)

                    val deslocamento =
                        if (Random.nextBoolean()) intensidadeVibracao else -intensidadeVibracao

                    pointerAngle.animateTo(
                        targetValue = deslocamento,
                        animationSpec = tween(durationMillis = 30)
                    )

                    pointerAngle.animateTo(
                        targetValue = -deslocamento,
                        animationSpec = tween(durationMillis = 30)
                    )

                    delay(
                        20 + Random.nextLong(
                            10,
                            40
                        )
                    ) // Pequena variação no tempo para parecer mais natural
                }
            }

            job.join()

            // Suaviza o pino no final (evita que fique "tremendo" no fim)
            pointerAngle.animateTo(0f, animationSpec = tween(500))
        }
    }

    //Comunicação BLE
    LaunchedEffect(Unit) {
        Log.d("BLE", "🟢 Verificando estado BLE ao abrir a tela...")
        bleViewModel.updateConnectionState()
    }

    LaunchedEffect(esperandoConfirmacaoArduino) {
        if (esperandoConfirmacaoArduino.value) {
            val tempoLimite = 5000L // 5 segundos
            val startTime = System.currentTimeMillis()

            while (esperandoConfirmacaoArduino.value && System.currentTimeMillis() - startTime < tempoLimite) {
                delay(100)
            }

            if (esperandoConfirmacaoArduino.value) {
                // ⚠️ Timeout atingido sem resposta do Arduino
                Log.e("BLE", "⏱️ Timeout: Arduino não respondeu a tempo")
                esperandoConfirmacaoArduino.value = false
                // Mostrar popup de erro, ou tentar nova tentativa
            }
        }
    }

    LaunchedEffect(bleMessage) {
        when (bleMessage) {
            is BLEViewModel.BleMessage.Ok -> {
                esperandoConfirmacaoArduino.value = false
                Log.d("BLEResponse", "✅ Recebido OK do Arduino $bleMessage")
                val talaoSeguro = talaoCriado ?: return@LaunchedEffect
                try {
                    APIResource.confirmarImpressao(talaoSeguro)
                } catch (e: Exception) {
                    Log.e("API", "❌ Erro ao confirmar impressão: ${e.message}")
                }
            }

            is BLEViewModel.BleMessage.SemPapel -> {
                esperandoConfirmacaoArduino.value = false
                try {
                    maquina?.let {
                        APIResource.reportarEstadoImpressora(it, "SEM_PAPEL")
                    }
                } catch (e: Exception) {
                    Log.e("API", "❌ Erro ao reportar falta de papel: ${e.message}")
                }
            }

            is BLEViewModel.BleMessage.Erro -> {
                esperandoConfirmacaoArduino.value = false
                Log.e("BLEResponse", "❌ Erro geral do Arduino $bleMessage")
                // Se quiseres notificar o servidor de erro geral também:
                try {
                    maquina?.let {
                        APIResource.reportarEstadoImpressora(it, "ERRO")
                    }
                } catch (e: Exception) {
                    Log.e("API", "❌ Erro ao reportar erro geral: ${e.message}")
                }
            }

            is BLEViewModel.BleMessage.Fim -> {
                Log.d("BLEResponse", "🏁 Impressão finalizada $bleMessage")
            }

            else -> Unit
        }
    }

}

@Composable
fun FortuneWheelWithRims(rotationAngle: Float, pointerAngle: Float, modifier: Modifier = Modifier, maquinaViewModel: MaquinaViewModel) {
    val labels = maquinaViewModel.labels
    val cores = maquinaViewModel.cores

    val bolaImages = mapOf(
        3 to ImageBitmap.imageResource(id = R.drawable.spin_ball_jpeg_1),
        7 to ImageBitmap.imageResource(id = R.drawable.spin_ball_jpeg_2),
        11 to ImageBitmap.imageResource(id = R.drawable.spin_ball_jpeg_3),
        15 to ImageBitmap.imageResource(id = R.drawable.spin_ball_jpeg_4)
    )

    // Criação das fatias
    val slices = labels.mapIndexed { index, label ->
        val corHex = cores.getOrNull(index) ?: "#000000"
        val cor = try {
            Color(android.graphics.Color.parseColor(corHex))
        } catch (e: Exception) {
            Color.Gray
        }
        if (label == "BOLA") {
            WheelSlice(Color.Black, "")
        } else {
            WheelSlice(cor, label)
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = min(size.width, size.height) / 2
            val sliceAngle = 360f / slices.size

            drawGoldenRim(radius)
            drawBlackRim(radius - 5.dp.toPx())

            // Corrige o ângulo inicial para alinhar corretamente a fatia 0 com o topo
            rotate(rotationAngle - 90f) {
                slices.forEachIndexed { index, slice ->
                    drawBrightSliceWithImageAndText(
                        index = index,
                        radius = radius - 10.dp.toPx(),
                        sliceAngle = sliceAngle,
                        slice = slice,
                        image = bolaImages[index] // imagem da bola específica por índice
                    )
                    drawVividBlackLineBetweenSlices(index, radius - 10.dp.toPx(), sliceAngle)
                }

                slices.indices.forEach { index ->
                    drawGoldenPinBetweenSlices(index, radius - 10.dp.toPx(), sliceAngle)
                }
            }

            drawGoldenBlackCenter(radius / 5)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSmallerGoldenBlackPin(size.minDimension / 2 - 20.dp.toPx(), pointerAngle)
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
            animationSpec = tween(durationMillis = 3000)
        )
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

data class WheelSlice(
    val color: Color,
    val label: String
)
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
            rotate((textAngle + 90).toFloat(), 0f, 0f)
            drawText(
                slice.label,
                0f,
                0f,
                android.graphics.Paint().apply {
                    color = when (slice.label) {
                        "PT", "PC", "AM" -> android.graphics.Color.BLACK
                        else -> android.graphics.Color.WHITE
                    }
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
private fun DrawScope.drawSmallerGoldenBlackPin(radius: Float, pointerAngle: Float) {
    val pinHeight = radius / 6
    val pinWidth = radius / 12
    val pivotY = center.y - radius - pinHeight / 2 - 10.dp.toPx() // Ponto de rotação

    val goldenGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B)),
        center = Offset(center.x, pivotY),
        radius = pinHeight
    )

    rotate(pointerAngle, pivot = Offset(center.x, pivotY)) { // 🔹 ROTACIONANDO O PINO
        val path = Path().apply {
            moveTo(center.x, pivotY + pinHeight)
            lineTo(center.x - pinWidth / 2, pivotY)
            lineTo(center.x + pinWidth / 2, pivotY)
            close()
        }

        drawPath(path = path, brush = goldenGradient)

        drawCircle(
            color = Color.Black,
            radius = pinWidth / 2,
            center = Offset(center.x, pivotY)
        )
    }
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

// Função para adicionar resultados aos slots
fun addToSlotResults(rouletteResource: RouletteResource, sliceIndex: Int, resultList: MutableList<Int>, slotLabels: MutableList<String>, slotCount: Int, maquina: Maquina, labels: List<String>) {
    if (labels[sliceIndex] != "BOLA") {
        resultList.add(rouletteResource.getCardImageForSlice(sliceIndex,maquina)) // Adiciona cromo ao slot
        slotLabels.add(labels[sliceIndex]) // Adiciona o rótulo fixo ao slot
        if (resultList.size > slotCount) {
            resultList.removeAt(0) // Remove o primeiro elemento se ultrapassar o limite
            slotLabels.removeAt(0) // Remove o primeiro rótulo também
        }
    }
}

suspend fun spinTheWhell(rouletteIndex: Int, rotationAngle: Animatable<Float, AnimationVector1D>){
    val customEasing = RouletteResource.cubicBezier(0.25f, 1.0f, 0.36f, 1f)

    val bolaPosition = RouletteResource.calculatePositionByIndex(rouletteIndex, rotationAngle.value)
    rotationAngle.animateTo(
        targetValue = bolaPosition,
        animationSpec = tween(
            durationMillis = 5000,
            easing = customEasing
        )
    )
}



