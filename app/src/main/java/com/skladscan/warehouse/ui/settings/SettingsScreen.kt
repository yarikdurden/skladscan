package com.skladscan.warehouse.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.BuildConfig
import com.skladscan.warehouse.ui.AppUiState
import com.skladscan.warehouse.ui.components.GlassCard
import com.skladscan.warehouse.ui.components.GradientButton
import com.skladscan.warehouse.ui.components.IconTile
import com.skladscan.warehouse.ui.components.InfoRow
import com.skladscan.warehouse.ui.components.OutlineButton
import com.skladscan.warehouse.ui.components.SkladTextField
import com.skladscan.warehouse.ui.components.SyncChip
import com.skladscan.warehouse.ui.simpleViewModelFactory
import com.skladscan.warehouse.ui.theme.LimeMint
import com.skladscan.warehouse.ui.theme.SkladTheme
import com.skladscan.warehouse.util.Dates

const val GITHUB_URL = "https://github.com/your-username/skladscan"

@Composable
fun SettingsScreen(
    container: AppContainer,
    app: AppUiState,
    onSync: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = simpleViewModelFactory { SettingsViewModel(container) }
    )
    val connection by viewModel.connection.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val colors = SkladTheme.colors
    val settings = app.settings
    val uriHandler = LocalUriHandler.current
    var serverUrl by rememberSaveable { mutableStateOf(settings.serverUrl) }
    var confirmReset by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(3_000)
            viewModel.consumeMessage()
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "КОНФИГУРАЦИЯ",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                                color = colors.textMuted,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("Настройки", style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)
                        }
                        SyncChip(
                            online = app.online,
                            demoMode = settings.demoMode,
                            pending = app.summary.pendingOperations,
                            syncing = app.syncing,
                            onClick = onSync,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Устройство, сервер и поведение сканера",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                }
            }

            item {
                ProfileCard(
                    warehouse = settings.warehouseName,
                    device = settings.deviceName,
                    demo = settings.demoMode,
                    online = app.online,
                )
            }

            item { SectionLabel("Синхронизация") }
            item {
                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(Icons.Rounded.CloudSync, colors.cyan, size = 40.dp, corner = 13.dp)
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text("REST API", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text(
                                    if (settings.demoMode) "Демо-режим · сервер эмулируется" else "Offline-first · очередь операций",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                )
                            }
                            ConnectionBadge(connection = connection, demo = settings.demoMode)
                        }
                        Spacer(Modifier.height(13.dp))
                        SkladTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = "Адрес сервера",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri,
                        )
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "GET /api/v1/products · POST /api/v1/operations",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (serverUrl != settings.serverUrl) {
                                Text(
                                    "Сохранить",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.accent,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { viewModel.saveServerUrl(serverUrl) }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            OutlineButton(
                                text = "Проверить",
                                onClick = viewModel::testConnection,
                                icon = Icons.Rounded.Wifi,
                                modifier = Modifier.weight(1f),
                                height = 46.dp,
                                enabled = connection != ConnectionState.Testing,
                            )
                            GradientButton(
                                text = if (app.syncing) "Идёт..." else "Синхронизировать",
                                onClick = onSync,
                                icon = Icons.Rounded.Sync,
                                modifier = Modifier.weight(1.3f),
                                height = 46.dp,
                                enabled = !app.syncing,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = colors.stroke)
                        SwitchRow(
                            icon = Icons.Rounded.CloudSync,
                            title = "Демо-режим",
                            subtitle = "Имитировать сервер и синхронизацию без сети",
                            checked = settings.demoMode,
                            onCheckedChange = viewModel::setDemoMode,
                        )
                        HorizontalDivider(color = colors.stroke)
                        InfoRow("Последняя синхронизация", Dates.relative(app.summary.lastSyncAt))
                        HorizontalDivider(color = colors.stroke)
                        InfoRow(
                            "В очереди",
                            app.summary.pendingOperations.toString(),
                            valueColor = if (app.summary.pendingOperations > 0) colors.amber else colors.textPrimary,
                        )
                    }
                }
            }

            item { SectionLabel("Сканирование") }
            item {
                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        SwitchRow(
                            icon = Icons.Rounded.QrCodeScanner,
                            title = "Непрерывное сканирование",
                            subtitle = "Не закрывать карточку после действия",
                            checked = settings.continuousScan,
                            onCheckedChange = viewModel::setContinuousScan,
                        )
                        HorizontalDivider(color = colors.stroke)
                        SwitchRow(
                            icon = Icons.Rounded.VolumeUp,
                            title = "Звук сканера",
                            subtitle = "Короткий сигнал при распознавании",
                            checked = settings.soundEnabled,
                            onCheckedChange = viewModel::setSound,
                        )
                        HorizontalDivider(color = colors.stroke)
                        SwitchRow(
                            icon = Icons.Rounded.Vibration,
                            title = "Вибрация",
                            subtitle = "Тактильный отклик на штрихкод",
                            checked = settings.vibrationEnabled,
                            onCheckedChange = viewModel::setVibration,
                        )
                    }
                }
            }

            item { SectionLabel("Оформление") }
            item {
                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Тема интерфейса", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(10.dp))
                        ThemeSelector(
                            current = settings.theme,
                            onSelect = viewModel::setTheme,
                        )
                    }
                }
            }

            item { SectionLabel("Данные") }
            item {
                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        InfoRow("Позиций в каталоге", app.summary.totalSkus.toString())
                        HorizontalDivider(color = colors.stroke)
                        InfoRow("Единиц на складе", app.summary.totalUnits.toString())
                        HorizontalDivider(color = colors.stroke)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { confirmReset = true }
                                .padding(vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconTile(Icons.Rounded.DeleteSweep, colors.coral, size = 34.dp, corner = 11.dp)
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Сбросить демо-данные", style = MaterialTheme.typography.titleSmall, color = colors.coral)
                                Text(
                                    "Вернуть исходный каталог и историю",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                )
                            }
                        }
                    }
                }
            }

            item { SectionLabel("О приложении") }
            item {
                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(colors.accentFill, LimeMint))),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Rounded.QrCodeScanner,
                                    contentDescription = null,
                                    tint = colors.accentInk,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("СкладСкан", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text(
                                    "Kotlin · Compose · CameraX · ML Kit · Room",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                )
                            }
                            Text(
                                "v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMuted,
                            )
                        }
                        Spacer(Modifier.height(13.dp))
                        HorizontalDivider(color = colors.stroke)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { uriHandler.openUri(GITHUB_URL) }
                                .padding(vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconTile(Icons.Rounded.Code, colors.violet, size = 34.dp, corner = 11.dp)
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Исходный код", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                Text(
                                    GITHUB_URL,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                Icons.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Сделано для складов, где важен каждый скан",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
        ) {
            message?.let {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surfaceHigh,
                    border = BorderStroke(1.dp, colors.stroke),
                    shadowElevation = 12.dp,
                ) {
                    Row(
                        Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = colors.surface,
            icon = { Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = colors.coral) },
            title = { Text("Сбросить данные?", color = colors.textPrimary) },
            text = {
                Text(
                    "Каталог и история операций будут заменены демонстрационными. Локальные изменения пропадут.",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        viewModel.resetDemoData()
                    }
                ) {
                    Text("Сбросить", color = colors.coral)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Отмена", color = colors.textMuted)
                }
            },
        )
    }
}

@Composable
private fun ProfileCard(
    warehouse: String,
    device: String,
    demo: Boolean,
    online: Boolean,
) {
    val colors = SkladTheme.colors
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.stroke),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(colors.accent.copy(alpha = 0.12f), colors.surface, colors.surface)
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(colors.accentFill, LimeMint))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "К",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.accentInk,
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(warehouse, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(device, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniChip(if (demo) "Демо" else "Продакшн", if (demo) colors.cyan else colors.accent)
                    MiniChip(if (online) "Онлайн" else "Офлайн", if (online) colors.accent else colors.coral)
                    MiniChip("Локальный кэш", colors.violet)
                }
            }
        }
    }
}

@Composable
private fun MiniChip(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
        color = SkladTheme.colors.textMuted,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp),
    )
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = SkladTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, if (checked) colors.accent else colors.textMuted, size = 36.dp, corner = 12.dp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accentInk,
                checkedTrackColor = colors.accentFill,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceHigh,
                uncheckedBorderColor = colors.stroke,
            ),
        )
    }
}

@Composable
private fun ConnectionBadge(connection: ConnectionState, demo: Boolean) {
    val colors = SkladTheme.colors
    val (text, color) = when (connection) {
        ConnectionState.Idle -> (if (demo) "Демо" else "Не проверено") to (if (demo) colors.cyan else colors.textMuted)
        ConnectionState.Testing -> "Проверка..." to colors.amber
        is ConnectionState.Success -> "Доступен" to colors.accent
        is ConnectionState.Error -> "Ошибка" to colors.coral
    }
    Row(
        Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (connection is ConnectionState.Error) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        } else {
            Box(Modifier.size(5.dp).background(color, CircleShape))
        }
        Spacer(Modifier.width(5.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun ThemeSelector(current: String, onSelect: (String) -> Unit) {
    val colors = SkladTheme.colors
    val options = listOf("dark" to "Тёмная", "light" to "Светлая", "system" to "Системная")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(colors.surfaceHigh.copy(alpha = 0.6f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (current == value) colors.accentFill else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (current == value) colors.accentInk else colors.textSecondary,
                )
            }
        }
    }
}
