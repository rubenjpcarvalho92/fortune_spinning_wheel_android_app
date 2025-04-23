package com.example.fortune_whell_v3.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fortune_whell_v3.viewmodel.BLEViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BLETestScreen(bleViewModel: BLEViewModel = viewModel()) {
    var macAddress by remember { mutableStateOf("78:21:84:7A:7A:4E") }
    val connectionState by bleViewModel.connectionState.collectAsState()
    val receivedData by bleViewModel.receivedMessage.collectAsState()
    var permissionGranted by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // 🔹 Atualiza o estado da conexão ao abrir a tela
    LaunchedEffect(Unit) {
        bleViewModel.updateConnectionState()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Estado da Conexão: $connectionState")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = macAddress,
            onValueChange = { macAddress = it },
            label = { Text("MAC Address do Arduino") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Botão para abrir o nRF Connect e depois verificar o estado
        Button(onClick = {
            openNRFConnect(context)
            bleViewModel.updateConnectionState() // 🔹 Verifica se já está conectado
        }) {
            Text("Abrir nRF Connect")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Botão para abrir as configurações do app
        Button(onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:" + context.packageName)
            }
            context.startActivity(intent)
        }) {
            Text("Abrir Configurações do App")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Botão para enviar dados "Teste" para o Arduino
        Button(
            onClick = {
                bleViewModel.sendData("AA:BB:CC") }
            ,
            enabled = connectionState == "Conectado" // 🔹 Agora fica ativo se já estiver conectado
        ) {
            Text("Enviar Dados")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Dados Recebidos:")

    }
}


@Composable
fun BluetoothPermissionRequester(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    Log.d("BluetoothPermission", "🔍 Iniciando verificação de permissões...")

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(Manifest.permission.BLUETOOTH)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        result.entries.forEach { (permission, isGranted) ->
            if (isGranted) {
                Log.d("BluetoothPermission", "✅ Permissão concedida: $permission")
            } else {
                Log.e("BluetoothPermission", "❌ Permissão negada: $permission")
            }
        }

        val allGranted = result.values.all { it }
        onPermissionResult(allGranted)
    }

    LaunchedEffect(Unit) {
        Log.d("BluetoothPermission", "🟢 LaunchedEffect ativado!")

        if (permissions.any {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }) {
            Log.d("BluetoothPermission", "📢 Solicitando permissões BLE...")
            permissionLauncher.launch(permissions)
        } else {
            Log.d("BluetoothPermission", "🔵 Todas as permissões já foram concedidas!")
            onPermissionResult(true)
        }
    }
}

@Composable
fun BluetoothPermissionWarning() {
    Text(
        text = "⚠️ Permissões Bluetooth não concedidas! Vá para as configurações do App para concedê-las.",
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    )
}

fun openNRFConnect(context: Context) {
    val packageName = "no.nordicsemi.android.mcp" // Pacote do nRF Connect

    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        try {
            val playStoreIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            )
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            Log.e("BLE", "Erro ao abrir a Play Store", e)
        }
    }
}
