package com.filtro.ligacoes.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.filtro.ligacoes.BlockedCall
import com.filtro.ligacoes.Health
import com.filtro.ligacoes.Prefs
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class StartupData(
    val total: Int,
    val log: List<BlockedCall>,
    val roleHeld: Boolean,
    val contactsOk: Boolean,
    val notifOk: Boolean,
    val batteryOk: Boolean,
    val blockUnknown: Boolean,
    val blockVoip: Boolean,
)

@Composable
fun FiltroApp() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }

    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }

    var enabled by remember { mutableStateOf(prefs.enabled) }
    var roleHeld by remember { mutableStateOf(false) }
    var contactsOk by remember { mutableStateOf(false) }

    LaunchedEffect(tick) {
        enabled = prefs.enabled
        val (held, contacts) = withContext(Dispatchers.IO) {
            Pair(Health.roleHeld(ctx), Health.contactsGranted(ctx))
        }
        roleHeld = held
        contactsOk = contacts
    }

    val ready = roleHeld && contactsOk
    val protecting = enabled && ready

    FiltroTheme(active = protecting) {
        AuroraBackground(active = protecting) {
            HomeScreen(
                protecting = protecting,
                tick = tick,
                onTick = { tick++ },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    protecting: Boolean,
    tick: Int,
    onTick: () -> Unit,
) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }

    var enabled by remember { mutableStateOf(prefs.enabled) }
    var blockUnknown by remember { mutableStateOf(prefs.blockUnknown) }
    var blockVoip by remember { mutableStateOf(prefs.blockVoip) }
    var languageCode by remember { mutableStateOf(prefs.languageCode) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }
    var isBlockedCallsOpen by remember { mutableStateOf(false) }

    var roleHeld by remember { mutableStateOf(value = false) }
    var contactsOk by remember { mutableStateOf(value = false) }
    var notifOk by remember { mutableStateOf(value = true) }
    var batteryOk by remember { mutableStateOf(value = false) }
    var total by remember { mutableIntStateOf(0) }
    var log by remember { mutableStateOf(emptyList<BlockedCall>()) }

    LaunchedEffect(tick) {
        enabled = prefs.enabled
        blockUnknown = prefs.blockUnknown
        blockVoip = prefs.blockVoip
        languageCode = prefs.languageCode
        val data = withContext(Dispatchers.IO) {
            StartupData(
                total = prefs.totalBlocked,
                log = prefs.log(),
                roleHeld = Health.roleHeld(ctx),
                contactsOk = Health.contactsGranted(ctx),
                notifOk = Health.notificationsGranted(ctx),
                batteryOk = ctx.getSystemService(PowerManager::class.java)
                    ?.isIgnoringBatteryOptimizations(ctx.packageName) == true,
                blockUnknown = prefs.blockUnknown,
                blockVoip = prefs.blockVoip,
            )
        }
        total = data.total
        log = data.log
        roleHeld = data.roleHeld
        contactsOk = data.contactsOk
        notifOk = data.notifOk
        batteryOk = data.batteryOk
        blockUnknown = data.blockUnknown
        blockVoip = data.blockVoip
    }

    val str = remember(languageCode) { L10n.strings(languageCode) }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { onTick() }
    val contactsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onTick() }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onTick() }

    val ready = roleHeld && contactsOk
    val shownTotal by animateIntAsState(total, tween(800), label = "total")
    val primaryColor by animateColorAsState(if (protecting) Pal.Green else Pal.Red, tween(1800, easing = FastOutSlowInEasing), label = "primaryColor")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Cabeçalho (título)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(0)
                    .padding(top = 8.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(str.appTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Pal.Text)
                }
            }

            // Cartão principal com o botão de adição (+) sobreposto na primeira caixa de seleção
            Box(modifier = Modifier.entrance(1).fillMaxWidth()) {
                GlassCard(active = protecting) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        ShieldOrb(protecting)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            when {
                                !ready -> str.statusNeedsSetup
                                protecting -> str.statusActive
                                else -> str.statusDisabled
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Pal.Text,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            when {
                                !ready -> str.descNeedsSetup
                                protecting -> str.descActive
                                else -> str.descDisabled
                            },
                            fontSize = 14.sp,
                            color = Pal.TextDim,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (enabled) str.switchOn else str.switchOff,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Pal.Text,
                            )
                            Spacer(Modifier.width(14.dp))
                            GlassSwitch(checked = enabled, active = protecting) {
                                enabled = it
                                prefs.enabled = it
                                if (it && !blockUnknown && !blockVoip) {
                                    blockUnknown = true
                                    prefs.blockUnknown = true
                                }
                                onTick()
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(shownTotal.toString(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                str.callsBlockedLabel,
                                fontSize = 14.sp,
                                color = Pal.TextDim,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                    }
                }
                // Botão de adição (+) posicionado exatamente sobre a sobreposição da primeira caixa de seleção
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp),
                ) {
                    GlassPlusButton(active = protecting) {
                        isMenuOpen = true
                    }
                }
            }

            // Modo de bloqueio
            GlassCard(modifier = Modifier.entrance(2), active = protecting) {
                SectionTitle(str.blockModeTitle)
                Spacer(Modifier.height(10.dp))
                Text(
                    str.blockModeDesc,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Pal.TextDim,
                )
                Spacer(Modifier.height(18.dp))
                SwitchRow(
                    title = str.blockUnknownTitle,
                    subtitle = str.blockUnknownSubtitle,
                    checked = blockUnknown,
                    active = protecting,
                ) {
                    blockUnknown = it
                    prefs.blockUnknown = it
                    if (!it && !blockVoip) {
                        enabled = false
                        prefs.enabled = false
                    }
                    onTick()
                }
                Spacer(Modifier.height(14.dp))
                SwitchRow(
                    title = str.blockVoipTitle,
                    subtitle = str.blockVoipSubtitle,
                    checked = blockVoip,
                    active = protecting,
                ) {
                    blockVoip = it
                    prefs.blockVoip = it
                    if (!it && !blockUnknown) {
                        enabled = false
                        prefs.enabled = false
                    }
                    onTick()
                }
            }

            // Configuração
            GlassCard(modifier = Modifier.entrance(3), active = protecting) {
                SectionTitle(str.setupTitle)
                Spacer(Modifier.height(6.dp))
                SetupRow(
                    done = contactsOk,
                    title = str.setupContactsTitle,
                    subtitle = str.setupContactsSubtitle,
                    action = str.setupContactsAction,
                    active = protecting,
                ) { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }
                SetupRow(
                    done = roleHeld,
                    title = str.setupRoleTitle,
                    subtitle = str.setupRoleSubtitle,
                    action = str.setupRoleAction,
                    active = protecting,
                ) {
                    val rm = ctx.getSystemService(RoleManager::class.java)
                    if ((rm != null) && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                        roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                    }
                }
                if (Build.VERSION.SDK_INT >= 33) {
                    SetupRow(
                        done = notifOk,
                        title = str.setupNotifTitle,
                        subtitle = str.setupNotifSubtitle,
                        action = str.setupNotifAction,
                        active = protecting,
                    ) { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                }
                SetupRow(
                    done = batteryOk,
                    title = str.setupBatteryTitle,
                    subtitle = str.setupBatterySubtitle,
                    action = str.setupBatteryAction,
                    active = protecting,
                ) {
                    ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // Submenu flutuante em vidro fosco acionado pelo botão +
        ActionSubmenu(
            isOpen = isMenuOpen,
            active = protecting,
            settingsLabel = str.menuSettings,
            blockedCallsLabel = str.menuBlockedCalls,
            onOpenSettings = { isSettingsOpen = true },
            onOpenBlockedCalls = { isBlockedCallsOpen = true },
            onDismiss = { isMenuOpen = false },
        )

        // Aba lateral de configurações
        SettingsDrawer(
            isOpen = isSettingsOpen,
            active = protecting,
            currentLanguageCode = languageCode,
            onSaveLanguage = { newLang ->
                prefs.languageCode = newLang
                languageCode = newLang
            },
            onClose = { isSettingsOpen = false },
        )

        // Gaveta lateral de ligações bloqueadas
        BlockedCallsDrawer(
            isOpen = isBlockedCallsOpen,
            active = protecting,
            title = str.blockedCallsTitle,
            clearActionText = str.logClearAction,
            emptyText = str.logEmpty,
            rejectedText = str.logRejected,
            hiddenNumberText = str.logHiddenNumber,
            log = log,
            primaryColor = primaryColor,
            onClearLog = {
                prefs.clearLog()
                log = emptyList()
            },
            onClose = { isBlockedCallsOpen = false },
        )
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
    active: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Pal.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Pal.TextDim, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        GlassSwitch(checked = checked, active = active, onCheckedChange = onChange)
    }
}

@Composable
private fun SetupRow(
    done: Boolean,
    title: String,
    subtitle: String,
    action: String,
    active: Boolean = true,
    onAction: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(done = done, active = active)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Pal.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Pal.TextDim, fontSize = 12.sp, lineHeight = 16.sp)
        }
        if (!done) {
            Spacer(Modifier.width(10.dp))
            PillButton(action, active = active, onClick = onAction)
        }
    }
}

@Composable
private fun StatusDot(done: Boolean, active: Boolean = true) {
    val fill by animateFloatAsState(if (done) 1f else 0f, tween(400), label = "dot")
    val colStart by animateColorAsState(if (active) Pal.Green else Pal.Red, tween(400), label = "dotStart")
    val colEnd by animateColorAsState(if (active) Pal.Mint else Pal.Rose, tween(400), label = "dotEnd")

    Canvas(Modifier.size(26.dp)) {
        val r = size.minDimension / 2f
        drawCircle(
            color = Color.White.copy(alpha = 0.20f),
            radius = r - 1.dp.toPx(),
            style = Stroke(width = 1.5.dp.toPx()),
        )
        drawCircle(
            brush = Brush.linearGradient(listOf(colStart, colEnd)),
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
