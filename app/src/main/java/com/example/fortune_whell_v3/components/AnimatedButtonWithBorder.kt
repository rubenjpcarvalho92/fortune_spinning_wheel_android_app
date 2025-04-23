package com.example.fortune_wheel_v3.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun AnimatedButtonWithBorder(
    text: String,
    imageResId: Int,
    canvasSize: Dp = 285.dp,
    isAnimated: Boolean = true,
    verticalOffset: Dp = 0.dp,
    horizontalOffset: Dp = 0.dp,
    borderPadding: Dp = 10.dp, // Espaço entre a borda giratória e a imagem central
    isEnabled: Boolean = true, // Controle para habilitar/desabilitar o botão
    onClick: () -> Unit
) {
    val infiniteTransition = if (isAnimated) rememberInfiniteTransition() else null
    val rotationAnimation = infiniteTransition?.animateFloat(
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
            .size(canvasSize)
            .offset(y = verticalOffset)
            .offset(x = horizontalOffset)
    ) {
        if (isAnimated) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val borderWidth = 15.dp.toPx()
                val paddingPx = borderPadding.toPx()
                rotate(rotationAnimation?.value ?: 0f) {
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
                        radius = (size.minDimension / 2f) - borderWidth / 2 - paddingPx, // Ajusta o raio com base no padding
                        style = Stroke(width = borderWidth)
                    )
                }
            }
        }

        Button(
            onClick = { if (isEnabled) onClick() }, // Ação do botão respeitando `isEnabled`
            modifier = Modifier
                .size(canvasSize - (borderPadding * 2)) // Ajusta o tamanho para acomodar o padding
                .align(Alignment.Center),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) Color.Transparent else Color.Gray.copy(alpha = 0.6f) // Indica visualmente que está desabilitado
            ),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(0.dp),
            enabled = isEnabled // Desabilita interações
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Imagem de fundo
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "Imagem do Botão",
                    modifier = Modifier.fillMaxSize()
                )

                // Texto sobreposto
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isEnabled) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
