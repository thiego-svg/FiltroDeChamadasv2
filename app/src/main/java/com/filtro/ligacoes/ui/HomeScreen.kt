package com.filtro.ligacoes.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.filtro.ligacoes.BlockedCall
import com.filtro.ligacoes.Health
import com.filtro.ligacoes.Prefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FiltroApp() {
    FiltroTheme {
        AuroraBackground {
            HomeScreen()
        }
    }
}

@Composable
private fun HomeScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }

    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }

    var enabled by remember { mutableStateOf(prefs.enabled) }
    var blockHidden by remember { mutableStateOf(prefs.blockHidden) }
    var roleHeld by remember { mutableStateOf(value = false) }
    var contactsOk by remember { mutableStateOf(value = false) }
    var notifOk by remember { mutableStateOf(value = true) }
    var batteryOk by remember { mutableStateOf(value = false) }
    var total by remember { mutableIntStateOf(0) }
    var log by remember { mutableStateOf(emptyList<BlockedCall>()) }

    LaunchedEffect(tick) {
        enabled = prefs.enabled
        blockHidden = prefs.blockHidden
        total = prefs.totalBlocked
        log = prefs.log()
        roleHeld = Health.roleHeld(ctx)
        contactsOk = Health.contactsGranted(ctx)
        notifOk = Health.notificationsGranted(ctx)
        batteryOk = ctx.getSystemService(PowerManager::class.java)
            ?.isIgnoringBatteryOptimizations(ctx.packageName) == true
    }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { tick++ }
    val contactsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { tick++ }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { tick++ }

    val ready = roleHeld && contactsOk
    val protecting = enabled && ready
    val shownTotal by animateIntAsState(total, tween(800), label = "total")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.entrance(0).padding(top = 8.dp, start = 4.dp)) {
            Text("Filtro de Chamadas", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Pal.Text)
            Text("Só toca quem está nos seus contatos.", fontSize = 14.sp, color = Pal.TextDim)
        }

        // Cartão principal
        GlassCard(Modifier.entrance(1)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ShieldOrb(protecting)
                Spacer(Modifier.height(6.dp))
                Text(
                    when {
                        !ready -> "Falta configurar"
                        protecting -> "Proteção ativa"
                        else -> "Proteção pausada"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Pal.Text,
                )
                Text(
                    when {
                        !ready -> "Conclua os passos mais abaixo para começar."
                        protecting -> "Ligações de números fora da agenda são barradas."
                        else -> "Todas as ligações tocam normalmente."
                    },
                    fontSize = 14.sp,
                    color = Pal.TextDim,
                )
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (enabled) "Ligado" else "Desligado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Pal.Text,
                    )
                    Spacer(Modifier.width(14.dp))
                    GlassSwitch(checked = enabled) {
                        enabled = it
                        prefs.enabled = it
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(shownTotal.toString(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Pal.Green)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "chamadas barradas",
                        fontSize = 14.sp,
                        color = Pal.TextDim,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
        }

        // Modo de bloqueio
        GlassCard(Modifier.entrance(2)) {
            SectionTitle("Quando um desconhecido ligar")
            Spacer(Modifier.height(10.dp))
            Text(
                "O telefone não toca. A ligação é recusada na hora, como se você tivesse " +
                    "tocado em recusar. O que quem ligou ouve (ocupado ou caixa postal) " +
                    "depende da operadora.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Pal.TextDim,
            )
            Spacer(Modifier.height(18.dp))
            SwitchRow(
                title = "Bloquear números ocultos",
                subtitle = "Chamadas sem identificação (número privado)",
                checked = blockHidden,
            ) {
                blockHidden = it
                prefs.blockHidden = it
            }
        }

        // Configuração
        GlassCard(Modifier.entrance(3)) {
            SectionTitle("Configuração")
            Spacer(Modifier.height(6.dp))
            SetupRow(
                done = contactsOk,
                title = "Acesso aos contatos",
                subtitle = "Para saber quem está na sua agenda",
                action = "Permitir",
            ) { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }
            SetupRow(
                done = roleHeld,
                title = "App de filtro de chamadas",
                subtitle = "O Android pergunta uma vez. Fica valendo após reiniciar.",
                action = "Ativar",
            ) {
                val rm = ctx.getSystemService(RoleManager::class.java)
                if ((rm != null) && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                    roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                }
            }
            if (Build.VERSION.SDK_INT >= 33) {
                SetupRow(
                    done = notifOk,
                    title = "Avisos",
                    subtitle = "Para avisar se a proteção for desfeita",
                    action = "Permitir",
                ) { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            }
            SetupRow(
                done = batteryOk,
                title = "Sem restrição de bateria",
                subtitle = "Recomendado: escolha este app e marque Sem restrições",
                action = "Abrir",
            ) {
                ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }

        // Histórico
        GlassCard(Modifier.entrance(4)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { SectionTitle("Últimas barradas") }
                if (log.isNotEmpty()) {
                    PillButton("Limpar") {
                        prefs.clearLog()
                        log = emptyList()
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            if (log.isEmpty()) {
                Text("Nenhuma chamada barrada ainda.", fontSize = 14.sp, color = Pal.TextDim)
            } else {
                val fmt = remember { SimpleDateFormat("dd/MM  HH:mm", Locale.forLanguageTag("pt-BR")) }
                log.take(8).forEach { (time, number) ->
                    LogRow(
                        number = number.ifBlank { "Número oculto" },
                        whenText = fmt.format(Date(time)),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Pal.Text)
}

@Composable
private fun SwitchRow(
    @Suppress("SameParameterValue") title: String,
    @Suppress("SameParameterValue") subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Pal.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Pal.TextDim, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        GlassSwitch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SetupRow(
    done: Boolean,
    title: String,
    subtitle: String,
    action: String,
    onAction: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(done)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Pal.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Pal.TextDim, fontSize = 12.sp, lineHeight = 16.sp)
        }
        if (!done) {
            Spacer(Modifier.width(10.dp))
            PillButton(action, onClick = onAction)
        }
    }
}

@Composable
private fun StatusDot(done: Boolean) {
    val fill by animateFloatAsState(if (done) 1f else 0f, tween(400), label = "dot")
    Canvas(Modifier.size(26.dp)) {
        val r = size.minDimension / 2f
        drawCircle(
            color = Color.White.copy(alpha = 0.20f),
            radius = r - 1.dp.toPx(),
            style = Stroke(width = 1.5.dp.toPx()),
        )
        drawCircle(
            brush = Brush.linearGradient(listOf(Pal.Green, Pal.Mint)),
            radius = r * fill,
            center = center,
        )
        if (fill > 0.6f) {
            val w = size.width
            val h = size.height
            val check = Path().apply {
                moveTo(w * 0.28f, h * 0.52f)
                lineTo(w * 0.44f, h * 0.67f)
                lineTo(w * 0.72f, h * 0.36f)
            }
            drawPath(
                check,
                color = Pal.Black,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

@Composable
private fun LogRow(number: String, whenText: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(number, color = Pal.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(whenText, color = Pal.TextDim, fontSize = 12.sp)
        }
        Text(
            "recusada",
            color = Pal.Green,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
