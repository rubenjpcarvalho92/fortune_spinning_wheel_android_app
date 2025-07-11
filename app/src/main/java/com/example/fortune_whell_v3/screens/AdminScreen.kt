@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fortune_whell_v3.screens

import android.bluetooth.BluetoothManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fortune_whell_v3.api.models.Maquina
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bleproject.viewmodel.BLEViewModel
import com.example.fortune_whell_v3.api.models.Stock
import com.example.fortune_whell_v3.Utils.DeviceUtils.Companion.getAndroidId
import com.example.fortune_whell_v3.api.resources.APIResource
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(navController: NavController, bluetoothViewModel: BLEViewModel = viewModel(), maquinaViewModel: MaquinaViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informações do Equipamento") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { contentPadding ->
        TabsContent(
            contentPadding,
            navController,
            bluetoothViewModel,
            maquinaViewModel
        )
    }
}



@Composable
fun TabsContent(
    contentPadding: PaddingValues,
    navController: NavController,
    bluetoothViewModel : BLEViewModel,
    maquinaViewModel: MaquinaViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Dados do Equipamento", "Estado", "Stock")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTabIndex) {
            0 -> TabelaInfo(navController, bluetoothViewModel)
            1 -> {
                val numeroSerie = getAndroidId(LocalContext.current)
                TabelaEstado(numeroSerie)
            }
            2 -> {
                val numeroSerie = getAndroidId(LocalContext.current)
                TabelaStock(numeroSerie, maquinaViewModel)
            }
        }
    }
}

@Composable
fun TabelaInfo(navController: NavController, bleViewModel: BLEViewModel) {
    val context = LocalContext.current
    var numeroSerie by remember { mutableStateOf<String?>(null) }
    var dadosMaquina by remember { mutableStateOf<Maquina?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val androidId = getAndroidId(context)
        numeroSerie = androidId

        val numeroSegura = numeroSerie ?: run {
            errorMessage = "Número de série não disponível."
            return@LaunchedEffect
        }

        val resultado = APIResource.buscarDadosMaquinaRolleta(numeroSegura)
        if (resultado != null) {
            dadosMaquina = resultado
        } else {
            errorMessage = "Erro ao buscar dados da máquina."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SnackbarHost(hostState = snackbarHostState)

        Column {
            Text(
                text = "Dados do Equipamento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else if (dadosMaquina != null) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        CampoFixo("Número de Série", dadosMaquina?.numeroSerie ?: "Desconhecido")
                        CampoFixo("Valor da Aposta", "${dadosMaquina?.valorAposta ?: "N/A"} €")
                        CampoFixo("Atribuído Total", "${dadosMaquina?.atribuidoTotal ?: "N/A"} €")
                        CampoFixo("Apostado Total", "${dadosMaquina?.apostadoTotal ?: "N/A"} €")
                        CampoFixo("Taxa de Ganho Definida", "${dadosMaquina?.taxaGanhoDefinida ?: "N/A"} %")
                        CampoFixo("Taxa de Ganho Actual", "${dadosMaquina?.taxaGanhoActual ?: "N/A"} %")
                        CampoFixo("Taxa de Ganho Parcial", "${dadosMaquina?.taxaGanhoParcial ?: "N/A"} %")
                        CampoFixo("Valor Apostado Parcial", "${dadosMaquina?.apostadoParcial ?: "N/A"} €")
                        CampoFixo("Valor Atribuído Parcial", "${dadosMaquina?.atribuidoParcial ?: "N/A"} €")
                        CampoFixo("Estado", dadosMaquina?.status ?: "Desconhecido")
                        CampoFixo("Rolo Papel", dadosMaquina?.roloPapelOK ?: "Sem Papel")
                        CampoFixo("Stock", dadosMaquina?.stockOK ?: "Em Falta")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val maquinaAtual = dadosMaquina ?: return@launch
                            val premios = APIResource.getPremios(maquinaAtual.numeroSerie, false)
                            if (premios.isEmpty()) {
                                snackbarHostState.showSnackbar("Nenhum prêmio por contabilizar.")
                                return@launch
                            }

                            val stockAtual = APIResource.buscarStock(maquinaAtual.numeroSerie)
                            if (stockAtual == null) {
                                snackbarHostState.showSnackbar("Erro ao buscar stock.")
                                return@launch
                            }

                            val dataHora = getDataHoraAtual()
                            val comando = buildLevantamentoCommand(maquinaAtual, stockAtual, dataHora)
                            bleViewModel.sendMessage(comando)

                            val resposta = bleViewModel.awaitResposta(timeout = 7000)
                            if (resposta != "OK") {
                                snackbarHostState.showSnackbar("Erro na impressão: $resposta")
                                return@launch
                            }

                            val sucesso = APIResource.contabilizarPremios(maquinaAtual.numeroSerie)
                            if (!sucesso) {
                                snackbarHostState.showSnackbar("Falha ao contabilizar os prêmios.")
                                return@launch
                            }

                            val levantamento = APIResource.resetGanhosMaquina(maquinaAtual, stockAtual)
                            dadosMaquina = maquinaAtual.copy(
                                apostadoParcial = 0,
                                atribuidoParcial = 0f,
                                taxaGanhoParcial = 0f,
                                apostadoParcialDinheiro = 0,
                            )

                            snackbarHostState.showSnackbar("✅ Levantamento realizado com sucesso.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Levantar Máquina")
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Voltar ao Menu Inicial", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

fun getDataHoraAtual(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

fun buildLevantamentoCommand(maquina: Maquina, stock: Stock, dataHora: String): String {
    return buildString {
        append("LEVANTAMENTO|PRINT|")
        append(maquina.numeroSerie).append("|")
        append(maquina.valorAposta).append(":")
        append(maquina.atribuidoTotal).append(":")
        append(maquina.apostadoTotal).append(":")
        append(maquina.taxaGanhoDefinida).append(":")
        append(maquina.taxaGanhoActual).append(":")
        append(maquina.taxaGanhoParcial).append(":")
        append(maquina.apostadoParcial).append(":")
        append(maquina.atribuidoParcial).append(":")
        append(maquina.status).append(":")
        append(maquina.roloPapelOK).append(":")
        append(maquina.stockOK).append(":")
        append(maquina.Admins_NIF).append(":")
        append(maquina.Funcionarios_NIF).append(":")
        append(maquina.Clientes_NIF).append(":")
        append(maquina.MACArduino).append(":")
        append(maquina.apostadoParcialDinheiro).append("|")
        append(stock.VD).append(":")
        append(stock.PT).append(":")
        append(stock.CI).append(":")
        append(stock.AM).append(":")
        append(stock.GM).append(":")
        append(stock.VR).append(":")
        append(stock.LR).append(":")
        append(stock.PC).append(":")
        append(stock.RX).append(":")
        append(stock.AZ).append(":")
        append(stock.BB).append(":")
        append(stock.EE).append(":")
        append(stock.ARC).append("|")
        append(dataHora)
    }
}


@Composable
fun TabelaEstado(numeroSerie: String) {
    var dadosMaquina by remember { mutableStateOf<Maquina?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val estadoOptions = listOf("Activa", "Desactiva", "Manutenção", "Registada")
    val papelOptions = listOf("Sem Papel", "Com Papel", "Manutenção")
    val stockOptions = listOf("OK", "Em Falta")

    var estadoSelecionado by remember { mutableStateOf(estadoOptions.first()) }
    var roloPapelSelecionado by remember { mutableStateOf(papelOptions.first()) }
    var stockSelecionado by remember { mutableStateOf(stockOptions.first()) }

    // Buscar os dados da máquina ao carregar o componente
    LaunchedEffect(numeroSerie) {
        isLoading = true

        val resultado = APIResource.buscarDadosMaquinaRolleta(numeroSerie)

        if (resultado != null) {
            dadosMaquina = resultado
            estadoSelecionado = resultado.status ?: estadoOptions.first()
            roloPapelSelecionado = resultado.roloPapelOK ?: papelOptions.first()
            stockSelecionado = resultado.stockOK ?: stockOptions.first()
            mensagemErro = null
        } else {
            mensagemErro = "Erro ao buscar dados da máquina."
        }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Alterar Estado",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (mensagemErro != null) {
            Text(
                text = mensagemErro!!,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            return@Column
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        // Dropdowns para alterar estado
        EstadoDropdown(label = "Estado", options = estadoOptions, selectedOption = estadoSelecionado) {
            estadoSelecionado = it
        }
        EstadoDropdown(label = "Rolo de Papel", options = papelOptions, selectedOption = roloPapelSelecionado) {
            roloPapelSelecionado = it
        }
        EstadoDropdown(label = "Stock", options = stockOptions, selectedOption = stockSelecionado) {
            stockSelecionado = it
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (dadosMaquina == null) {
                    mensagemErro = "Erro: Máquina não carregada corretamente."
                    return@Button
                }

                isUpdating = true
                mensagemErro = null
                mensagemSucesso = null

                val maquinaAtualizada = dadosMaquina!!.copy(
                    status = estadoSelecionado,
                    roloPapelOK = roloPapelSelecionado,
                    stockOK = stockSelecionado
                )

                // Lançar coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    val sucesso = APIResource.actualizaMaquina(maquinaAtualizada)

                    withContext(Dispatchers.Main) {
                        isUpdating = false
                        if (sucesso) {
                            mensagemSucesso = "Estado atualizado com sucesso!"
                        } else {
                            mensagemErro = "Erro ao atualizar máquina."
                        }
                    }
                }
            },
            enabled = !isUpdating,
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text(text = "Atualizar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exibir mensagens de erro ou sucesso
        mensagemErro?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        mensagemSucesso?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TabelaStock(numeroSerie: String, maquinaViewModel: MaquinaViewModel = viewModel()) {
    var stock by remember { mutableStateOf<Stock?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val updatedStockValues = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(numeroSerie) {
        if (numeroSerie.isEmpty()) {
            errorMessage = "Número de série inválido ou não encontrado."
            isLoading = false
            return@LaunchedEffect
        }

        val result = APIResource.buscarStock(numeroSerie)

        if (result != null) {
            stock = result
            errorMessage = null

            val campos = listOf("EE", "VD", "PT", "CI", "AM", "GM", "VR", "LR", "PC", "RX", "AZ", "BB", "ARC")

            campos.forEach { campo ->
                try {
                    val field = result.javaClass.getDeclaredField(campo).apply { isAccessible = true }
                    val valor = field.get(result) as? Int
                    updatedStockValues[campo] = valor?.toString() ?: ""
                } catch (e: Exception) {
                    // Se o campo não existir ou outro erro ocorrer, apenas ignora
                    updatedStockValues[campo] = ""
                }
            }
        } else {
            errorMessage = "Erro ao carregar dados do estoque."
        }

        isLoading = false
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Parâmetros de Stock",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            stock != null -> {
                val labelsSemBola = maquinaViewModel.labels.filter { it != "BOLA" }
                StockCard(updatedStockValues, labelsSemBola)

                Spacer(modifier = Modifier.height(16.dp))

                ButtonAtualizarStock(numeroSerie, updatedStockValues, maquinaViewModel) { sucesso ->
                    errorMessage = if (sucesso) null else "Erro ao atualizar stock."
                }
            }

            else -> {
                Text("Stock não encontrado.", color = MaterialTheme.colorScheme.error)
            }
        }
    }

}

@Composable
fun EstadoDropdown(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = selectedOption)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        text = { Text(option) }
                    )
                }
            }
        }
    }
}

@Composable
fun CampoFixo(nome: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = nome, fontWeight = FontWeight.Bold)
        Text(text = valor)
    }
}

@Composable
fun CampoEditavel(nome: String, updatedStockValues: MutableMap<String, String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nome,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = updatedStockValues[nome] ?: "",
            onValueChange = { updatedStockValues[nome] = it },
            singleLine = true,
            modifier = Modifier
                .width(100.dp)
                .height(48.dp)
        )
    }
}

@Composable
fun StockCard(updatedStockValues: MutableMap<String, String>, labels: List<String>) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            labels.forEach { CampoEditavel(it, updatedStockValues) }
        }
    }
}

@Composable
fun ButtonAtualizarStock(
    numeroSerie: String,
    updatedStockValues: Map<String, String>,
    maquinaViewModel: MaquinaViewModel = viewModel(),
    onResult: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val labels = maquinaViewModel.labels

    Button(
        onClick = {
            // Gera um map com os valores inteiros por label
            val valores = labels.associateWith { updatedStockValues[it]?.toIntOrNull() ?: 0 }

            // Cria uma instância de Stock com reflexão
            val stock = Stock(
                Maquinas_numeroSerie = numeroSerie,
                EE = valores["EE"] ?: 0,
                VD = valores["VD"] ?: 0,
                PT = valores["PT"] ?: 0,
                CI = valores["CI"] ?: 0,
                AM = valores["AM"] ?: 0,
                GM = valores["GM"] ?: 0,
                VR = valores["VR"] ?: 0,
                LR = valores["LR"] ?: 0,
                PC = valores["PC"] ?: 0,
                RX = valores["RX"] ?: 0,
                AZ = valores["AZ"] ?: 0,
                BB = valores["BB"] ?: 0,
                ARC = valores["ARC"] ?: 0
            )

            coroutineScope.launch {
                val sucesso = APIResource.atualizarStock(stock)
                onResult(sucesso)
            }
        },
        modifier = Modifier.fillMaxWidth(0.5f)
    ) {
        Text("Atualizar Stock")
    }
}
