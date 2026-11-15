package com.filtro.ligacoes.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
}

@Composable
fun FiltroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Pal.Green,
            background = Pal.Black,
            surface = Pal.Black,
            onSurface = Pal.Text,
            onBackground = Pal.Text,
        ),
        content = content,
    )
}
