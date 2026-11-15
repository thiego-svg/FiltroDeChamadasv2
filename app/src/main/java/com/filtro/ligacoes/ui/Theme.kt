package com.filtro.ligacoes.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

object Pal {
    val Black = Color(0xFF030506)
    @Suppress("unused")
    val DeepGreen = Color(0xFF07251A)
    val Green = Color(0xFF00E676)
    val Mint = Color(0xFF1DE9B6)
    val Lime = Color(0xFF69F0AE)
    val Text = Color(0xFFEAFBF2)
    val TextDim = Color(0xFF9DBBAA)

    // Cores do tema vermelho (quando a proteção estiver desativada)
    val DeepRed = Color(0xFF25070A)
    val Red = Color(0xFFFF334B)
    val Crimson = Color(0xFFFF1744)
    val Rose = Color(0xFFFF5252)
    @Suppress("unused")
    val CoralRed = Color(0xFFFF6E40)
}

@Composable
fun FiltroTheme(active: Boolean = true, content: @Composable () -> Unit) {
    val primaryColor by animateColorAsState(
        if (active) Pal.Green else Pal.Red,
        tween(1800, easing = FastOutSlowInEasing),
        label = "themePrimary"
    )
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = primaryColor,
            background = Pal.Black,
            surface = Pal.Black,
            onSurface = Pal.Text,
            onBackground = Pal.Text,
        ),
        content = content,
    )
}
