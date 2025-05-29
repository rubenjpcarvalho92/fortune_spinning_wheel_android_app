package com.example.fortune_whell_v3.navigation

//Screens
import com.example.fortune_whell_v3.screens.MainScreen
import com.example.fortune_whell_v3.screens.RouletteScreen
import com.example.fortune_whell_v3.screens.LoginScreen
import com.example.fortune_whell_v3.screens.AdminScreen
import com.example.fortune_whell_v3.screens.GhostModeScreen

//Nativo
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bleproject.viewmodel.BLEViewModel
import com.example.fortune_whell_v3.Utils.DeviceUtils.Companion.getAndroidId
import com.example.fortune_whell_v3.screens.BLETestScreen
import com.example.fortune_whell_v3.screens.DeviceInfoScreen
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Criando a instância do BLEViewModel
    val bluetoothViewModel: BLEViewModel = viewModel()

    //Criando uma instância para o ViewModel da maquina
    val maquinaViewModel: MaquinaViewModel = viewModel()

    val context = LocalContext.current

    // 🔹 Inicializa apenas uma vez com LaunchedEffect
    LaunchedEffect(Unit) {
        val numeroSerie = getAndroidId(context)
        maquinaViewModel.inicializar(numeroSerie)
    }

    // Configurando as rotas de navegação
    NavHost(navController = navController, startDestination = "info") {
        composable("info"){ DeviceInfoScreen (navController,maquinaViewModel,bluetoothViewModel
        ) }
        composable("demo") { GhostModeScreen(navController, maquinaViewModel) }
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen(navController, maquinaViewModel) }
        composable("roulette") { RouletteScreen(navController, bluetoothViewModel, maquinaViewModel) }
        composable("admin") { AdminScreen(navController,bluetoothViewModel,maquinaViewModel)} // Tela da roleta
        composable("teste") { BLETestScreen(bluetoothViewModel) } // Tela da roleta
    }
}