package com.filtro.ligacoes.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/** Fundo preto com manchas verdes que derivam devagar (a "aurora"). */
@Composable
fun AuroraBackground(content: @Composable BoxScope.() -> Unit) {
    val t = rememberInfiniteTransition(label = "aurora")
    val a by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse),
        label = "a",
    )
    val b by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(19000, easing = LinearEasing), RepeatMode.Reverse),
        label = "b",
    )
    val c by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(23000, easing = LinearEasing), RepeatMode.Reverse),
        label = "c",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pal.Black, Color(0xFF041510), Pal.Black)))
            .drawBehind {
                val w = size.width
                val h = size.height
                drawRect(
                    Brush.radialGradient(
                        listOf(Pal.Green.copy(alpha = 0.38f), Color.Transparent),
                        center = Offset(w * (0.10f + (0.60f * a)), h * (0.08f + (0.20f * b))),
                        radius = w * 0.95f,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(Pal.Mint.copy(alpha = 0.26f), Color.Transparent),
                        center = Offset(w * (0.95f - (0.60f * b)), h * (0.45f + (0.18f * c))),
                        radius = w * 0.85f,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(Pal.Lime.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * (0.20f + (0.50f * c)), h * (0.95f - (0.25f * a))),
                        radius = w * 0.90f,
                    )
                )
            },
        content = content
    )
}

/** Cartão "vidro fosco": preenchimento translúcido + borda com brilho. */
@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.13f), Color.White.copy(alpha = 0.04f))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.04f),
                        Pal.Green.copy(alpha = 0.30f)
                    )
                ),
                shape
            )
            .padding(20.dp),
        content = content
    )
}

/** Entrada suave: cada cartão sobe e aparece com um pequeno atraso. */
@Composable
fun Modifier.entrance(index: Int): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((90 * index).milliseconds)
        progress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 56.dp.toPx()
    }
}

@Composable
fun GlassSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    val thumbX by animateDpAsState(
        if (checked) 28.dp else 0.dp,
        spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "thumb"
    )
    val trackAlpha by animateFloatAsState(if (checked) 1f else 0f, tween(300), label = "track")

    Box(
        modifier = modifier
            .width(64.dp)
            .height(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .drawBehind {
                drawRect(
                    Brush.horizontalGradient(listOf(Pal.Green, Pal.Mint)),
                    alpha = trackAlpha
                )
            }
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch
            ) { onCheckedChange(!checked) }
            .padding(4.dp)
    ) {
        Box(
            Modifier
                .offset(x = thumbX)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/** Seletor com a "pílula" verde deslizando entre as opções. */
@Suppress("unused", "UnusedBoxWithConstraintsScope")
@Composable
fun GlassSegmented(
    selected: Int,
    labels: List<String>,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
) {
    val pos by animateFloatAsState(
        selected.toFloat(),
        spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "seg"
    )
    val outer = RoundedCornerShape(20.dp)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(outer)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), outer)
            .padding(4.dp)
    ) {
        val segW = maxWidth / labels.size
        Box(
            Modifier
                .offset(x = segW * pos)
                .width(segW)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Pal.Green.copy(alpha = 0.95f), Pal.Mint.copy(alpha = 0.80f))
                    )
                )
        )
        Row(Modifier.fillMaxSize()) {
            labels.forEachIndexed { i, label ->
                val textColor by animateColorAsState(
                    if (i == selected) Pal.Black else Pal.Text,
                    tween(250),
                    label = "segText"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PillButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(Pal.Green, Pal.Mint)))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Pal.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

/** Escudo central: pulsa e brilha quando a proteção está ativa. */
@Composable
fun ShieldOrb(active: Boolean, modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        0.88f, 1.12f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p"
    )
    val glow by animateFloatAsState(if (active) 1f else 0.12f, tween(800), label = "glow")

    Canvas(modifier.size(150.dp)) {
        val w = size.width
        val h = size.height
        val r = size.minDimension / 2f

        drawCircle(
            brush = Brush.radialGradient(
                listOf(Pal.Green.copy(alpha = 0.55f * glow), Color.Transparent),
                center = center,
                radius = r * pulse
            ),
            radius = r * pulse,
            center = center
        )

        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.22f)
            lineTo(w * 0.74f, h * 0.31f)
            lineTo(w * 0.74f, h * 0.50f)
            cubicTo(w * 0.74f, h * 0.66f, w * 0.63f, h * 0.76f, w * 0.50f, h * 0.82f)
            cubicTo(w * 0.37f, h * 0.76f, w * 0.26f, h * 0.66f, w * 0.26f, h * 0.50f)
            lineTo(w * 0.26f, h * 0.31f)
            close()
        }

        drawPath(
            shield,
            brush = Brush.verticalGradient(
                listOf(
                    Pal.Mint.copy(alpha = 0.20f + (0.55f * glow)),
                    Pal.Green.copy(alpha = 0.10f + (0.40f * glow)),
                ),
                startY = h * 0.20f,
                endY = h * 0.85f,
            )
        )
        drawPath(
            shield,
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.35f + (0.5f * glow)),
                    Pal.Green.copy(alpha = 0.25f + (0.65f * glow)),
                ),
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        val ink = Color.White.copy(alpha = 0.55f + (0.45f * glow))
        if (active) {
            val check = Path().apply {
                moveTo(w * 0.40f, h * 0.52f)
                lineTo(w * 0.47f, h * 0.60f)
                lineTo(w * 0.61f, h * 0.43f)
            }
            drawPath(
                check,
                color = ink,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        } else {
            drawLine(ink, Offset(w * 0.44f, h * 0.44f), Offset(w * 0.44f, h * 0.62f), 5.dp.toPx(), StrokeCap.Round)
            drawLine(ink, Offset(w * 0.56f, h * 0.44f), Offset(w * 0.56f, h * 0.62f), 5.dp.toPx(), StrokeCap.Round)
        }
    }
}
