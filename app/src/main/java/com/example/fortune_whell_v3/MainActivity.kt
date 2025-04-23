package com.example.fortune_whell_v3

import com.example.fortune_whell_v3.navigation.AppNavigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation() // Gerencia as telas
        }
    }
}


