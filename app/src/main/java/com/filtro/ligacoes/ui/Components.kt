package com.filtro.ligacoes.ui

import androidx.compose.ui.text.style.TextAlign
import com.filtro.ligacoes.BlockedCall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/** Fundo preto com manchas de cor (verde ativa / vermelha desativada) derivando suavemente. */
@Composable
fun AuroraBackground(
    active: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
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

    // Leve efeito da luz vermelha diminuindo (pulsando suavemente para baixo quando desativado)
    val redLightDecreasing by t.animateFloat(
        1.0f, 0.35f,
        infiniteRepeatable(tween(3800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "redLightDecreasing",
    )

    val bgMiddle by animateColorAsState(
        if (active) Color(0xFF041510) else Pal.DeepRed,
        tween(1800, easing = FastOutSlowInEasing),
        label = "bgMiddle"
    )
    val colorA by animateColorAsState(
        if (active) Pal.Green else Pal.Red,
        tween(1800, easing = FastOutSlowInEasing),
        label = "colorA"
    )
    val colorB by animateColorAsState(
        if (active) Pal.Mint else Pal.Crimson,
        tween(1800, easing = FastOutSlowInEasing),
        label = "colorB"
    )
    val colorC by animateColorAsState(
        if (active) Pal.Lime else Pal.Rose,
        tween(1800, easing = FastOutSlowInEasing),
        label = "colorC"
    )

    val lightMult = if (active) 1.0f else redLightDecreasing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pal.Black, bgMiddle, Pal.Black)))
            .drawBehind {
                val w = size.width
                val h = size.height
                drawRect(
                    Brush.radialGradient(
                        listOf(colorA.copy(alpha = 0.38f * lightMult), Color.Transparent),
                        center = Offset(w * (0.10f + (0.60f * a)), h * (0.08f + (0.20f * b))),
                        radius = w * 0.95f * lightMult,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(colorB.copy(alpha = 0.26f * lightMult), Color.Transparent),
                        center = Offset(w * (0.95f - (0.60f * b)), h * (0.45f + (0.18f * c))),
                        radius = w * 0.85f * lightMult,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(colorC.copy(alpha = 0.18f * lightMult), Color.Transparent),
                        center = Offset(w * (0.20f + (0.50f * c)), h * (0.95f - (0.25f * a))),
                        radius = w * 0.90f * lightMult,
                    )
                )
            },
        content = content
    )
}

/** Cartão "vidro fosco": preenchimento translúcido + borda com brilho ajustada ao tema. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    active: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val accentColor by animateColorAsState(
        if (active) Pal.Green else Pal.Red,
        tween(1800, easing = FastOutSlowInEasing),
        label = "cardAccent"
    )
    val bgAlphaTop by animateFloatAsState(
        targetValue = if (active) 0.13f else 0.08f,
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "cardBgAlphaTop"
    )
    val bgAlphaBottom by animateFloatAsState(
        targetValue = if (active) 0.04f else 0.02f,
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "cardBgAlphaBottom"
    )

    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = bgAlphaTop), Color.White.copy(alpha = bgAlphaBottom))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.04f),
                        accentColor.copy(alpha = 0.30f)
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
    active: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val thumbX by animateDpAsState(
        if (checked) 28.dp else 0.dp,
        spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "thumb"
    )
    val trackAlpha by animateFloatAsState(if (checked) 1f else 0f, tween(300), label = "track")
    val trackStart by animateColorAsState(if (active) Pal.Green else Pal.Red, tween(1800, easing = FastOutSlowInEasing), label = "switchStart")
    val trackEnd by animateColorAsState(if (active) Pal.Mint else Pal.Rose, tween(1800, easing = FastOutSlowInEasing), label = "switchEnd")

    Box(
        modifier = modifier
            .width(64.dp)
            .height(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .drawBehind {
                drawRect(
                    Brush.horizontalGradient(listOf(trackStart, trackEnd)),
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

/** Seletor com a "pílula" deslizando entre as opções. */
@Suppress("unused", "UnusedBoxWithConstraintsScope")
@Composable
fun GlassSegmented(
    selected: Int,
    labels: List<String>,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    onSelect: (Int) -> Unit,
) {
    val pos by animateFloatAsState(
        selected.toFloat(),
        spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "seg"
    )
    val segStart by animateColorAsState(if (active) Pal.Green else Pal.Red, tween(1800, easing = FastOutSlowInEasing), label = "segStart")
    val segEnd by animateColorAsState(if (active) Pal.Mint else Pal.Rose, tween(1800, easing = FastOutSlowInEasing), label = "segEnd")
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
                        listOf(segStart.copy(alpha = 0.95f), segEnd.copy(alpha = 0.80f))
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
fun PillButton(
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    onClick: () -> Unit
) {
    val colStart by animateColorAsState(if (active) Pal.Green else Pal.Red, tween(1800, easing = FastOutSlowInEasing), label = "btnStart")
    val colEnd by animateColorAsState(if (active) Pal.Mint else Pal.Rose, tween(1800, easing = FastOutSlowInEasing), label = "btnEnd")

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(colStart, colEnd)))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Pal.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

/** Escudo central: pulsa verde quando a proteção está ativa e exibe efeito de luz vermelha diminuindo quando desativada. */
@Composable
fun ShieldOrb(active: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseActive by transition.animateFloat(
        0.88f, 1.12f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pActive"
    )
    val redLightDecreasing by transition.animateFloat(
        1.0f, 0.40f,
        infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "redLightDecreasing"
    )

    val glowTarget = if (active) 1f else 0.80f
    val glow by animateFloatAsState(glowTarget, tween(1800, easing = FastOutSlowInEasing), label = "glow")

    val primaryColor by animateColorAsState(if (active) Pal.Green else Pal.Red, tween(1800, easing = FastOutSlowInEasing), label = "shieldPrimary")
    val secondaryColor by animateColorAsState(if (active) Pal.Mint else Pal.Rose, tween(1800, easing = FastOutSlowInEasing), label = "shieldSecondary")

    val activeProgress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "shieldIconProgress"
    )

    Canvas(modifier.size(150.dp)) {
        val w = size.width
        val h = size.height
        val r = size.minDimension / 2f

        val pulseScale = if (active) pulseActive else (0.88f + (0.12f * redLightDecreasing))
        val alphaMult = if (active) glow else (glow * redLightDecreasing)

        drawCircle(
            brush = Brush.radialGradient(
                listOf(primaryColor.copy(alpha = 0.55f * alphaMult), Color.Transparent),
                center = center,
                radius = r * pulseScale
            ),
            radius = r * pulseScale,
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
                    secondaryColor.copy(alpha = 0.20f + (0.55f * alphaMult)),
                    primaryColor.copy(alpha = 0.10f + (0.40f * alphaMult)),
                ),
                startY = h * 0.20f,
                endY = h * 0.85f,
            )
        )
        drawPath(
            shield,
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.35f + (0.5f * alphaMult)),
                    primaryColor.copy(alpha = 0.25f + (0.65f * alphaMult)),
                ),
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        val baseInk = Color.White.copy(alpha = 0.55f + (0.45f * alphaMult))
        val checkAlpha = activeProgress
        val xAlpha = 1f - activeProgress

        // Desenha o check (✓) quando a proteção estiver ativa
        if (checkAlpha > 0.01f) {
            val checkPath = Path().apply {
                moveTo(w * 0.40f, h * 0.52f)
                lineTo(w * 0.47f, h * 0.60f)
                lineTo(w * 0.61f, h * 0.43f)
            }
            drawPath(
                checkPath,
                color = baseInk.copy(alpha = baseInk.alpha * checkAlpha),
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Desenha o X (❌) quando a proteção estiver desativada
        if (xAlpha > 0.01f) {
            val xColor = baseInk.copy(alpha = baseInk.alpha * xAlpha)
            val strokeW = 5.dp.toPx()
            drawLine(
                color = xColor,
                start = Offset(w * 0.42f, h * 0.44f),
                end = Offset(w * 0.58f, h * 0.60f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = xColor,
                start = Offset(w * 0.58f, h * 0.44f),
                end = Offset(w * 0.42f, h * 0.60f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }
}

/** Botão de adição (+): abre o submenu em vidro fosco. */
@Composable
fun GlassPlusButton(
    modifier: Modifier = Modifier,
    active: Boolean = true,
    onClick: () -> Unit,
) {
    val accentColor by animateColorAsState(
        if (active) Pal.Green else Pal.Red,
        tween(1800, easing = FastOutSlowInEasing),
        label = "plusBtnAccent"
    )

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        accentColor.copy(alpha = 0.35f)
                    )
                ),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            val strokeW = 2.5.dp.toPx()
            drawLine(Color.White, Offset(w / 2f, 0f), Offset(w / 2f, h), strokeW, StrokeCap.Round)
            drawLine(Color.White, Offset(0f, h / 2f), Offset(w, h / 2f), strokeW, StrokeCap.Round)
        }
    }
}

@Composable
fun GearIcon(tint: Color) {
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.38f
        drawCircle(
            color = tint,
            radius = r * 0.70f,
            center = Offset(cx, cy),
            style = Stroke(width = 2f.dp.toPx(), cap = StrokeCap.Round)
        )
        val teeth = 8
        for (i in 0 until teeth) {
            val angle = Math.toRadians((i * (360.0 / teeth)))
            val x1 = cx + (r * 0.60f) * kotlin.math.cos(angle).toFloat()
            val y1 = cy + (r * 0.60f) * kotlin.math.sin(angle).toFloat()
            val x2 = cx + r * 1.05f * kotlin.math.cos(angle).toFloat()
            val y2 = cy + r * 1.05f * kotlin.math.sin(angle).toFloat()
            drawLine(
                color = tint,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun ShieldCallIcon(tint: Color) {
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.28f)
            lineTo(w * 0.85f, h * 0.55f)
            cubicTo(w * 0.85f, h * 0.75f, w * 0.70f, h * 0.88f, w * 0.50f, h * 0.95f)
            cubicTo(w * 0.30f, h * 0.88f, w * 0.15f, h * 0.75f, w * 0.15f, h * 0.55f)
            lineTo(w * 0.15f, h * 0.28f)
            close()
        }
        drawPath(shield, color = tint, style = Stroke(width = 2f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun SubmenuItem(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(12.dp))
        Text(text, color = Pal.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** Submenu flutuante em vidro fosco acionado pelo botão +. */
@Composable
fun ActionSubmenu(
    isOpen: Boolean,
    active: Boolean,
    settingsLabel: String,
    blockedCallsLabel: String,
    onOpenSettings: () -> Unit,
    onOpenBlockedCalls: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (isOpen) {
        val accentColor by animateColorAsState(
            if (active) Pal.Green else Pal.Red,
            tween(1800, easing = FastOutSlowInEasing),
            label = "submenuAccent"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.TopEnd
        ) {
            val menuShape = RoundedCornerShape(20.dp)
            Column(
                modifier = Modifier
                    .padding(top = 70.dp, end = 20.dp)
                    .width(230.dp)
                    .clip(menuShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF14201A).copy(alpha = 0.96f),
                                Color(0xFF090E0C).copy(alpha = 0.98f),
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.40f),
                                Color.White.copy(alpha = 0.08f),
                                accentColor.copy(alpha = 0.40f)
                            )
                        ),
                        menuShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent dismiss */ }
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SubmenuItem(
                    icon = { GearIcon(accentColor) },
                    text = settingsLabel,
                    onClick = {
                        onDismiss()
                        onOpenSettings()
                    }
                )
                SubmenuItem(
                    icon = { ShieldCallIcon(accentColor) },
                    text = blockedCallsLabel,
                    onClick = {
                        onDismiss()
                        onOpenBlockedCalls()
                    }
                )
            }
        }
    }
}

/** Gaveta lateral em vidro fosco para exibir o histórico de ligações bloqueadas. */
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun BlockedCallsDrawer(
    isOpen: Boolean,
    active: Boolean,
    title: String,
    clearActionText: String,
    emptyText: String,
    rejectedText: String,
    hiddenNumberText: String,
    log: List<BlockedCall>,
    primaryColor: Color,
    onClearLog: () -> Unit,
    onClose: () -> Unit,
) {
    val backdropAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.60f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "blockedBackdrop"
    )

    val drawerOffsetFraction by animateFloatAsState(
        targetValue = if (isOpen) 0f else 1.0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "blockedSlide"
    )

    if (isOpen || backdropAlpha > 0.01f) {
        val accentColor by animateColorAsState(
            if (active) Pal.Green else Pal.Red,
            tween(1800, easing = FastOutSlowInEasing),
            label = "blockedDrawerAccent"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = backdropAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                val drawerWidth = maxWidth * 0.88f
                val panelShape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)

                Column(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = drawerWidth.toPx() * drawerOffsetFraction
                        }
                        .clip(panelShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF121B17).copy(alpha = 0.95f),
                                    Color(0xFF090D0B).copy(alpha = 0.98f),
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.40f),
                                    Color.White.copy(alpha = 0.08f),
                                    accentColor.copy(alpha = 0.35f)
                                )
                            ),
                            panelShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* prevent click-through */ }
                        .systemBarsPadding()
                        .padding(top = 16.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
                ) {
                    // Cabeçalho
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Pal.Text,
                            modifier = Modifier.weight(1f)
                        )
                        if (log.isNotEmpty()) {
                            PillButton(clearActionText, active = active, onClick = onClearLog)
                            Spacer(Modifier.width(8.dp))
                        }
                        // Botão fechar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onClose
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(Modifier.size(16.dp)) {
                                val w = size.width
                                val h = size.height
                                val strokeW = 2.2.dp.toPx()
                                drawLine(Color.White, Offset(0f, 0f), Offset(w, h), strokeW, StrokeCap.Round)
                                drawLine(Color.White, Offset(w, 0f), Offset(0f, h), strokeW, StrokeCap.Round)
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Lista de bloqueadas
                    if (log.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emptyText, fontSize = 14.sp, color = Pal.TextDim, textAlign = TextAlign.Center)
                        }
                    } else {
                        val fmt = remember { SimpleDateFormat("dd/MM  HH:mm", Locale.forLanguageTag("pt-BR")) }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            log.forEach { (time, number) ->
                                val itemShape = RoundedCornerShape(16.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(itemShape)
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .border(1.dp, Color.White.copy(alpha = 0.10f), itemShape)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(number.ifBlank { hiddenNumberText }, color = Pal.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text(fmt.format(Date(time)), color = Pal.TextDim, fontSize = 12.sp)
                                    }
                                    Text(
                                        rejectedText,
                                        color = primaryColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Botão de salvar alterações com ícone de check num recipiente de vidro fosco. */
@Composable
fun GlassSaveButton(
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    onClick: () -> Unit
) {
    val colStart by animateColorAsState(
        if (active) Pal.Green else Pal.Red,
        tween(1800, easing = FastOutSlowInEasing),
        label = "saveBtnStart"
    )
    val colEnd by animateColorAsState(
        if (active) Pal.Mint else Pal.Rose,
        tween(1800, easing = FastOutSlowInEasing),
        label = "saveBtnEnd"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(colStart, colEnd)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Canvas(Modifier.size(20.dp)) {
                val w = size.width
                val h = size.height
                val strokeW = 2.2.dp.toPx()
                val check = Path().apply {
                    moveTo(w * 0.22f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.72f)
                    lineTo(w * 0.78f, h * 0.30f)
                }
                drawPath(
                    check,
                    color = Pal.Black,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                color = Pal.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/** Painel lateral de configurações: desliza da direita para a esquerda em vidro fosco (frosty blur). */
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun SettingsDrawer(
    isOpen: Boolean,
    active: Boolean,
    currentLanguageCode: String,
    onSaveLanguage: (String) -> Unit,
    onClose: () -> Unit,
) {
    val backdropAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.60f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "drawerBackdrop"
    )

    val drawerOffsetFraction by animateFloatAsState(
        targetValue = if (isOpen) 0f else 1.0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "drawerSlide"
    )

    if (isOpen || backdropAlpha > 0.01f) {
        var tempSelectedLang by remember(isOpen) { mutableStateOf(currentLanguageCode) }
        val strings = remember(tempSelectedLang) { L10n.strings(tempSelectedLang) }
        val accentColor by animateColorAsState(
            if (active) Pal.Green else Pal.Red,
            tween(1800, easing = FastOutSlowInEasing),
            label = "drawerAccent"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = backdropAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                val drawerWidth = maxWidth * 0.88f
                val panelShape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)

                Column(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = drawerWidth.toPx() * drawerOffsetFraction
                        }
                        .clip(panelShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF121B17).copy(alpha = 0.95f),
                                    Color(0xFF090D0B).copy(alpha = 0.98f),
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.40f),
                                    Color.White.copy(alpha = 0.08f),
                                    accentColor.copy(alpha = 0.35f)
                                )
                            ),
                            panelShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Previne clique no fundo */ }
                        .systemBarsPadding()
                        .padding(top = 16.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
                ) {
                    // Cabeçalho
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                strings.settingsTitle,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Pal.Text
                            )
                            Text(
                                strings.settingsSubtitle,
                                fontSize = 13.sp,
                                color = Pal.TextDim
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        // Botão fechar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onClose
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(Modifier.size(16.dp)) {
                                val w = size.width
                                val h = size.height
                                val strokeW = 2.2.dp.toPx()
                                drawLine(Color.White, Offset(0f, 0f), Offset(w, h), strokeW, StrokeCap.Round)
                                drawLine(Color.White, Offset(w, 0f), Offset(0f, h), strokeW, StrokeCap.Round)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Seção de idioma
                    Text(
                        strings.languageSectionTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Pal.Text
                    )
                    Text(
                        strings.languageSectionSubtitle,
                        fontSize = 13.sp,
                        color = Pal.TextDim
                    )

                    Spacer(Modifier.height(14.dp))

                    // Lista de idiomas menor e mais elegante (sem descrições)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = (lang.code == tempSelectedLang)
                            val itemBorderColor by animateColorAsState(
                                if (isSelected) accentColor else Color.White.copy(alpha = 0.12f),
                                tween(400),
                                label = "langBorder"
                            )
                            val itemBgAlpha by animateFloatAsState(
                                if (isSelected) 0.16f else 0.06f,
                                tween(400),
                                label = "langBg"
                            )

                            val itemShape = RoundedCornerShape(14.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(itemShape)
                                    .background(Color.White.copy(alpha = itemBgAlpha))
                                    .border(1.dp, itemBorderColor, itemShape)
                                    .clickable { tempSelectedLang = lang.code }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lang.flag, fontSize = 18.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    lang.nativeName,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = Pal.Text,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Spacer(Modifier.width(8.dp))
                                    Canvas(Modifier.size(18.dp)) {
                                        drawCircle(accentColor)
                                        val check = Path().apply {
                                            moveTo(size.width * 0.28f, size.height * 0.52f)
                                            lineTo(size.width * 0.44f, size.height * 0.68f)
                                            lineTo(size.width * 0.74f, size.height * 0.36f)
                                        }
                                        drawPath(
                                            check,
                                            color = Pal.Black,
                                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botão Salvar alterações na parte inferior
                    GlassSaveButton(
                        text = strings.saveChanges,
                        active = active,
                        onClick = {
                            onSaveLanguage(tempSelectedLang)
                            onClose()
                        }
                    )
                }
            }
        }
    }
}
