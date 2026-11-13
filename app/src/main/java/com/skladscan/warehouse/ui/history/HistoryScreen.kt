package com.skladscan.warehouse.ui.history

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.ui.AppUiState
import com.skladscan.warehouse.ui.components.EmptyState
import com.skladscan.warehouse.ui.components.FilterPill
import com.skladscan.warehouse.ui.components.GlassCard
import com.skladscan.warehouse.ui.components.IconTile
import com.skladscan.warehouse.ui.components.OperationRow
import com.skladscan.warehouse.ui.components.SyncChip
import com.skladscan.warehouse.ui.simpleViewModelFactory
import com.skladscan.warehouse.ui.theme.SkladTheme
import com.skladscan.warehouse.util.Dates

@Composable
fun HistoryScreen(
    container: AppContainer,
    app: AppUiState,
    onSync: () -> Unit,
) {
    val viewModel: HistoryViewModel = viewModel(
        factory = simpleViewModelFactory { HistoryViewModel(container) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val colors = SkladTheme.colors
    val pending = app.summary.pendingOperations

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "ЖУРНАЛ",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                            color = colors.textMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("История", style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)
                    }
                    SyncChip(
                        online = app.online,
                        demoMode = app.settings.demoMode,
                        pending = pending,
                        syncing = app.syncing,
                        onClick = onSync,
                    )
                    Spacer(Modifier.width(8.dp))
                    SyncButton(syncing = app.syncing, onClick = onSync)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Каждое движение товара фиксируется автоматически",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }

        if (pending > 0) {
            item {
                QueueBanner(
                    pending = pending,
                    syncing = app.syncing,
                    demo = app.settings.demoMode,
                    online = app.online,
                    onSync = onSync,
                )
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HistoryFilter.entries.forEach { item ->
                    FilterPill(
                        text = item.label,
                        selected = filter == item,
                        count = state.counts[item],
                        onClick = { viewModel.setFilter(item) },
                    )
                }
            }
        }

        if (state.entries.isEmpty() && !state.loading) {
            item {
                EmptyState(
                    icon = Icons.Rounded.History,
                    title = "Пока пусто",
                    text = "Отсканируйте товар и оформите приход или списание — операция появится здесь.",
                )
            }
        }

        items(state.entries) { entry ->
            when (entry) {
                is HistoryEntry.Header -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${entry.count}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.surfaceHigh)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "сортировка по времени",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted,
                        )
                    }
                }
                is HistoryEntry.Item -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(19.dp),
                    ) {
                        OperationRow(operation = entry.operation)
                    }
                }
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.CloudOff,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Без сети операции сохраняются и отправляются позже",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun QueueBanner(
    pending: Int,
    syncing: Boolean,
    demo: Boolean,
    online: Boolean,
    onSync: () -> Unit,
) {
    val colors = SkladTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.amber.copy(alpha = 0.08f))
            .border(1.dp, colors.amber.copy(alpha = 0.24f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(Icons.Rounded.CloudSync, colors.amber, size = 42.dp, corner = 14.dp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "$pending ${Dates.plural(pending, "операция ждёт", "операции ждут", "операций ждут")} отправки",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
            )
            Text(
                when {
                    demo -> "Демо-режим: отправка имитируется"
                    online -> "Можно отправить прямо сейчас"
                    else -> "Уйдёт автоматически при появлении сети"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        Box(
            Modifier
                .clip(CircleShape)
                .background(colors.amber.copy(alpha = 0.16f))
                .clickable(enabled = !syncing, onClick = onSync)
                .padding(horizontal = 13.dp, vertical = 8.dp),
        ) {
            Text(
                if (syncing) "Идёт..." else "Отправить",
                style = MaterialTheme.typography.labelMedium,
                color = colors.amber,
            )
        }
    }
}

@Composable
private fun SyncButton(syncing: Boolean, onClick: () -> Unit) {
    val colors = SkladTheme.colors
    val transition = rememberInfiniteTransition(label = "sync-rotate")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "angle",
    )
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(1.dp, colors.stroke, CircleShape)
            .clickable(enabled = !syncing, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Sync,
            contentDescription = "Синхронизировать",
            tint = if (syncing) colors.accent else colors.textSecondary,
            modifier = Modifier
                .size(19.dp)
                .then(if (syncing) Modifier.rotate(angle) else Modifier),
        )
    }
}
