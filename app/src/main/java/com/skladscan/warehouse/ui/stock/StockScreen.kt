package com.skladscan.warehouse.ui.stock

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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.SearchOff
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
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.domain.model.StockFilter
import com.skladscan.warehouse.ui.AppUiState
import com.skladscan.warehouse.ui.components.EmptyState
import com.skladscan.warehouse.ui.components.FilterPill
import com.skladscan.warehouse.ui.components.GlassCard
import com.skladscan.warehouse.ui.components.SearchField
import com.skladscan.warehouse.ui.components.SectionHeader
import com.skladscan.warehouse.ui.components.Sparkline
import com.skladscan.warehouse.ui.components.StockBadge
import com.skladscan.warehouse.ui.components.SyncChip
import com.skladscan.warehouse.ui.simpleViewModelFactory
import com.skladscan.warehouse.ui.theme.SkladTheme
import com.skladscan.warehouse.util.Dates

@Composable
fun StockScreen(
    container: AppContainer,
    app: AppUiState,
    onSync: () -> Unit,
    onOpenProduct: (Long) -> Unit,
    onOpenScanner: () -> Unit,
) {
    val viewModel: StockViewModel = viewModel(
        factory = simpleViewModelFactory { StockViewModel(container) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.queryFlow.collectAsStateWithLifecycle()
    val filter by viewModel.filterFlow.collectAsStateWithLifecycle()
    val week by viewModel.week.collectAsStateWithLifecycle()
    val colors = SkladTheme.colors
    val summary = app.summary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Header(
                app = app,
                onSync = onSync,
                onOpenScanner = onOpenScanner,
            )
        }
        item {
            SearchField(
                value = query,
                onValueChange = viewModel::setQuery,
            )
        }
        item {
            SummaryCard(
                summaryValue = Formatters.money(summary.inventoryValue),
                skus = summary.totalSkus,
                units = summary.totalUnits,
                low = summary.lowStock,
                today = summary.todayOperations,
                sparkValues = week.map { it.incoming.toFloat() },
            )
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StockFilter.entries.forEach { item ->
                    FilterPill(
                        text = item.label,
                        selected = filter == item,
                        count = state.counts[item],
                        onClick = { viewModel.setFilter(item) },
                    )
                }
            }
        }
        item {
            SectionHeader(
                title = "Позиции",
                count = state.products.size,
                action = if (query.isNotEmpty()) "Сбросить" else null,
                onAction = if (query.isNotEmpty()) ({ viewModel.setQuery("") }) else null,
            )
        }
        if (state.products.isEmpty() && !state.loading) {
            item {
                EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = if (query.isNotBlank()) "Ничего не найдено" else "Каталог пуст",
                    text = if (query.isNotBlank()) {
                        "Попробуйте изменить запрос или сбросить фильтр."
                    } else {
                        "Отсканируйте первый товар — он появится здесь."
                    },
                )
            }
        }
        items(state.products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                onClick = { onOpenProduct(product.id) },
            )
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Rounded.Inventory2,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Данные хранятся локально и синхронизируются автоматически",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun Header(
    app: AppUiState,
    onSync: () -> Unit,
    onOpenScanner: () -> Unit,
) {
    val colors = SkladTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "СКЛАД",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                    color = colors.textMuted,
                )
                Spacer(Modifier.height(4.dp))
                Text("Остатки", style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)
            }
            SyncChip(
                online = app.online,
                demoMode = app.settings.demoMode,
                pending = app.summary.pendingOperations,
                syncing = app.syncing,
                onClick = onSync,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.accentSoft)
                    .border(1.dp, colors.accent.copy(alpha = 0.25f), CircleShape)
                    .clickable(onClick = onOpenScanner),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.QrCodeScanner,
                    contentDescription = "Сканировать",
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                app.settings.warehouseName,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Text(" · ", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
            Text(
                "${app.summary.totalSkus} позиций",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Text(" · ", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
            Text(
                Dates.relative(app.summary.lastSyncAt),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SummaryCard(
    summaryValue: String,
    skus: Int,
    units: Int,
    low: Int,
    today: Int,
    sparkValues: List<Float>,
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
                        listOf(
                            colors.accent.copy(alpha = 0.16f),
                            colors.surface,
                            colors.surface,
                        )
                    )
                )
        ) {
            Box(
                Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            listOf(colors.accent.copy(alpha = 0.16f), Color.Transparent)
                        )
                    )
            )
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "СТОИМОСТЬ ЗАПАСОВ",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.3.sp),
                        color = colors.accent,
                    )
                    Spacer(Modifier.weight(1f))
                    if (today > 0) {
                        Text(
                            "сегодня +$today",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.surfaceHigh.copy(alpha = 0.8f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        summaryValue,
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    if (sparkValues.size > 1) {
                        Sparkline(
                            values = sparkValues,
                            color = colors.accent,
                            modifier = Modifier
                                .width(86.dp)
                                .height(40.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = colors.stroke)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    MiniStat(skus.toString(), "позиций", Modifier.weight(1f))
                    MiniStat(units.toString(), "единиц", Modifier.weight(1f))
                    MiniStat(
                        low.toString(),
                        "мало",
                        Modifier.weight(1f),
                        valueColor = if (low > 0) colors.amber else colors.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier = Modifier, valueColor: Color = SkladTheme.colors.textPrimary) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor)
        Text(label, style = MaterialTheme.typography.bodySmall, color = SkladTheme.colors.textMuted)
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    val colors = SkladTheme.colors
    val tint = categoryTint(product.category)
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        borderColor = if (product.isOutOfStock) colors.coral.copy(alpha = 0.3f) else colors.stroke,
        onClick = onClick,
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(tint.copy(alpha = 0.24f), tint.copy(alpha = 0.06f))
                        )
                    )
                    .border(1.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    product.category.firstOrNull()?.uppercase() ?: "Т",
                    style = MaterialTheme.typography.titleLarge,
                    color = tint,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    StockBadge(product)
                }
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.sku,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                    )
                    Text(" · ", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text(
                        product.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        Formatters.price(product.price),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun categoryTint(category: String): Color {
    val colors = SkladTheme.colors
    return when (category.lowercase()) {
        "оборудование" -> colors.cyan
        "расходники" -> colors.accent
        "упаковка" -> colors.violet
        "спецодежда" -> colors.amber
        else -> colors.coral
    }
}
