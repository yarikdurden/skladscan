package com.skladscan.warehouse.ui.analytics

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.Formatters
import com.skladscan.warehouse.ui.AppUiState
import com.skladscan.warehouse.ui.components.CategoryDonut
import com.skladscan.warehouse.ui.components.CategoryLegend
import com.skladscan.warehouse.ui.components.GlassCard
import com.skladscan.warehouse.ui.components.GradientButton
import com.skladscan.warehouse.ui.components.IconTile
import com.skladscan.warehouse.ui.components.OperationRow
import com.skladscan.warehouse.ui.components.SectionHeader
import com.skladscan.warehouse.ui.components.Sparkline
import com.skladscan.warehouse.ui.components.StatTile
import com.skladscan.warehouse.ui.components.StockLevelBar
import com.skladscan.warehouse.ui.components.SyncChip
import com.skladscan.warehouse.ui.components.WeekBarChart
import com.skladscan.warehouse.ui.simpleViewModelFactory
import com.skladscan.warehouse.ui.theme.SkladTheme
import com.skladscan.warehouse.util.Dates

@Composable
fun AnalyticsScreen(
    container: AppContainer,
    app: AppUiState,
    onSync: () -> Unit,
    onOpenProduct: (Long) -> Unit,
) {
    val viewModel: AnalyticsViewModel = viewModel(
        factory = simpleViewModelFactory { AnalyticsViewModel(container) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = SkladTheme.colors
    val summary = app.summary

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
                            "АНАЛИТИКА",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                            color = colors.textMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Сводка", style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)
                    }
                    SyncChip(
                        online = app.online,
                        demoMode = app.settings.demoMode,
                        pending = summary.pendingOperations,
                        syncing = app.syncing,
                        onClick = onSync,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Сегодня, ${Formatters.day(System.currentTimeMillis()).lowercase()} · ${summary.todayOperations} операций",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }

        item {
            AnalyticsHero(
                value = Formatters.money(summary.inventoryValue),
                skus = summary.totalSkus,
                units = summary.totalUnits,
                low = summary.lowStock + summary.outOfStock,
                spark = state.week.map { (it.incoming - it.outgoing).toFloat() },
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = "Принято за 7 дней",
                    value = state.incoming.toString(),
                    icon = Icons.Rounded.ArrowDownward,
                    tint = colors.accent,
                    hint = "ед.",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Списано за 7 дней",
                    value = state.outgoing.toString(),
                    icon = Icons.Rounded.ArrowUpward,
                    tint = colors.coral,
                    hint = "ед.",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = "Операций сегодня",
                    value = state.todayOperations.toString(),
                    icon = Icons.Rounded.FactCheck,
                    tint = colors.cyan,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Требуют внимания",
                    value = (summary.lowStock + summary.outOfStock).toString(),
                    icon = Icons.Rounded.WarningAmber,
                    tint = colors.amber,
                    hint = "SKU",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp)) {
                    SectionHeader("Движение за неделю")
                    Spacer(Modifier.height(14.dp))
                    WeekBarChart(bars = state.week)
                }
            }
        }

        if (state.lowStock.isNotEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(18.dp)) {
                        SectionHeader(
                            title = "Пора заказать",
                            count = state.lowStock.size,
                            action = "Все",
                        )
                        Spacer(Modifier.height(4.dp))
                        state.lowStock.forEachIndexed { index, product ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenProduct(product.id) }
                                    .padding(vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (product.isOutOfStock) colors.coral else colors.amber)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        product.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    StockLevelBar(product.quantity, product.minQuantity)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "осталось ${product.quantity} · мин. ${product.minQuantity} · ${product.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    product.quantity.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (product.isOutOfStock) colors.coral else colors.amber,
                                )
                            }
                            if (index != state.lowStock.lastIndex) {
                                HorizontalDivider(color = colors.stroke)
                            }
                        }
                    }
                }
            }
        }

        if (state.categories.isNotEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(18.dp)) {
                        SectionHeader("Структура запасов", count = state.categories.size)
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryDonut(
                                slices = state.categories,
                                centerLabel = "SKU",
                                centerValue = summary.totalSkus.toString(),
                                modifier = Modifier.size(132.dp),
                            )
                            Spacer(Modifier.width(18.dp))
                            CategoryLegend(
                                slices = state.categories,
                                total = state.categoryTotal,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        if (state.recent.isNotEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Последние операции",
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${state.recent.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMuted,
                            )
                        }
                        state.recent.forEach { operation ->
                            OperationRow(
                                operation = operation,
                                onClick = { onOpenProduct(operation.productId) },
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(Icons.Rounded.CloudSync, colors.cyan, size = 42.dp, corner = 14.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Синхронизация", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Text(
                                when {
                                    app.settings.demoMode -> "Демо-режим · REST API эмулируется"
                                    app.online -> app.settings.serverUrl
                                    else -> "Офлайн · данные на устройстве"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Последняя синхронизация",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMuted,
                            )
                            Text(
                                Dates.relative(summary.lastSyncAt),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "В очереди",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMuted,
                            )
                            Text(
                                summary.pendingOperations.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (summary.pendingOperations > 0) colors.amber else colors.accent,
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    GradientButton(
                        text = if (app.syncing) "Синхронизация..." else "Синхронизировать сейчас",
                        onClick = onSync,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !app.syncing,
                        icon = Icons.Rounded.CloudSync,
                        height = 46.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHero(
    value: String,
    skus: Int,
    units: Int,
    low: Int,
    spark: List<Float>,
) {
    val colors = SkladTheme.colors
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.accent.copy(alpha = 0.22f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(colors.accent.copy(alpha = 0.16f), colors.surface, colors.surface)
                    )
                )
        ) {
            Column(Modifier.padding(19.dp)) {
                Text(
                    "КАПИТАЛ НА СКЛАДЕ",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
                    color = colors.accent,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        value,
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    if (spark.size > 1) {
                        Sparkline(
                            values = spark,
                            color = colors.accent,
                            modifier = Modifier
                                .width(92.dp)
                                .height(42.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = colors.stroke)
                Spacer(Modifier.height(12.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text(skus.toString(), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Text("позиций", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(units.toString(), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Text("единиц", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            low.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (low > 0) colors.amber else colors.textPrimary,
                        )
                        Text("к заказу", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    }
                }
            }
        }
    }
}


