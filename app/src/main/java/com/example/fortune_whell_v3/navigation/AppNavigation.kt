package com.example.fortune_whell_v3.navigation

// Screens
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bleproject.viewmodel.BLEViewModel
import com.example.fortune_whell_v3.screens.*
import com.example.fortune_whell_v3.viewmodel.MaquinaViewModel
import com.example.fortune_whell_v3.R // ← necessário para aceder a R.raw

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bluetoothViewModel: BLEViewModel = viewModel()
    val maquinaViewModel: MaquinaViewModel = viewModel()
    val context = LocalContext.current

    // Música de fundo (MediaPlayer)
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.stadium_crowd_noise).apply {
                isLooping = true
                setVolume(1f, 1f)
                start()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Rota inicial dinâmica com base no número de série
    val startDestination = remember {
        val prefs = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
        if (prefs.getString("numero_serie", null).isNullOrBlank()) "definirNumeroSerie" else "info"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("definirNumeroSerie") {
            NumeroSerieScreen(navController, maquinaViewModel)
        }
        composable("info") {
            DeviceInfoScreen(navController, maquinaViewModel, bluetoothViewModel)
        }
        composable("demo") {
            GhostModeScreen(navController, maquinaViewModel)
        }
        composable("login") {
            LoginScreen(navController, maquinaViewModel)
        }
        composable("main") {
            MainScreen(navController, maquinaViewModel)
        }
        composable("roulette") {
            RouletteScreen(navController, bluetoothViewModel, maquinaViewModel)
        }
        composable("admin") {
            AdminScreen(navController, bluetoothViewModel, maquinaViewModel)
        }
        composable("teste") {
            BLETestScreen(bluetoothViewModel)
        }
    }
}
