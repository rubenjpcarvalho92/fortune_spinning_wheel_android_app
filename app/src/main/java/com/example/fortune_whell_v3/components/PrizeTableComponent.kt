package com.example.fortune_whell_v3.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class PrizeSlot(
    val label: String = "",
    val imageResId: Int? = null,
    val isEmpty: Boolean = true
)

@Composable
fun PrizeTable(
    prizeSlots: List<PrizeSlot>,
    onAnimationEnd: (Int) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(150.dp) // Largura fixa para o componente lateral
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            prizeSlots.forEachIndexed { index, slot ->
                PrizeSlotView(slot, index, onAnimationEnd)
            }
        }
    }
}

@Composable
fun PrizeSlotView(slot: PrizeSlot, index: Int, onAnimationEnd: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(
                color = if (slot.isEmpty) Color.LightGray else Color.Transparent,
                shape = CircleShape
            )
            .border(2.dp, Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        slot.imageResId?.let { resId ->
            Image(
                painter = painterResource(id = resId),
                contentDescription = slot.label,
                modifier = Modifier.size(50.dp)
            )
        }

        if (slot.isEmpty) {
            Text(
                text = "?",
                color = Color.DarkGray
            )
        }
    }
}
