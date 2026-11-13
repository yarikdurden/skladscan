package com.skladscan.warehouse.ui.stock

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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.Formatters
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.domain.model.StockMath
import com.skladscan.warehouse.domain.model.StockOperation
import com.skladscan.warehouse.ui.AppUiState
import com.skladscan.warehouse.ui.components.EmptyState
import com.skladscan.warehouse.ui.components.GlassCard
import com.skladscan.warehouse.ui.components.GradientButton
import com.skladscan.warehouse.ui.components.InfoRow
import com.skladscan.warehouse.ui.components.OperationPill
import com.skladscan.warehouse.ui.components.OperationRow
import com.skladscan.warehouse.ui.components.QuantityStepper
import com.skladscan.warehouse.ui.components.SectionHeader
import com.skladscan.warehouse.ui.components.StockBadge
import com.skladscan.warehouse.ui.components.StockLevelBar
import com.skladscan.warehouse.ui.components.SyncChip
import com.skladscan.warehouse.ui.components.operationIcon
import com.skladscan.warehouse.ui.components.operationLabel
import com.skladscan.warehouse.ui.components.operationTint
import com.skladscan.warehouse.ui.simpleViewModelFactory
import com.skladscan.warehouse.ui.theme.SkladTheme

@Composable
fun ProductDetailScreen(
    container: AppContainer,
    productId: Long,
    app: AppUiState,
    onBack: () -> Unit,
    onSync: () -> Unit,
) {
    val viewModel: ProductViewModel = viewModel(
        factory = simpleViewModelFactory { ProductViewModel(container, productId) }
    )
    val product by viewModel.product.collectAsStateWithLifecycle()
    val operations by viewModel.operations.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val colors = SkladTheme.colors
    var actionType by remember { mutableStateOf<OpType?>(null) }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(3_000)
            viewModel.consumeMessage()
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (product == null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                BackButton(onBack)
                Spacer(Modifier.weight(1f))
                Text(
                    "Товар не найден",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.weight(1f))
            }
        } else {
            val item = product!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BackButton(onBack)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("КАРТОЧКА ТОВАРА", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                            Text(item.category, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                        SyncChip(
                            online = app.online,
                            demoMode = app.settings.demoMode,
                            pending = app.summary.pendingOperations,
                            syncing = app.syncing,
                            onClick = onSync,
                        )
                    }
                }
                item { ProductHero(item) }
                if (item.isOutOfStock) {
                    item { OutOfStockBanner() }
                }
                item {
                    OperationsActions(
                        onAction = { actionType = it },
                    )
                }
                item { ProductInfoCard(item) }
                item {
                    SectionHeader(
                        title = "История движений",
                        count = operations.size,
                    )
                }
                if (operations.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.History,
                            title = "Нет операций",
                            text = "Первое движение появится после сканирования товара.",
                        )
                    }
                }
                items(operations.size, key = { operations[it].id }) { index ->
                    val operation = operations[index]
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        OperationRow(operation = operation, showFullDate = true)
                    }
                }
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

    actionType?.let { type ->
        product?.let { item ->
            OperationDialog(
                product = item,
                initialType = type,
                busy = busy,
                onDismiss = { actionType = null },
                onSubmit = { selectedType, quantity ->
                    viewModel.perform(selectedType, quantity)
                    actionType = null
                },
            )
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    val colors = SkladTheme.colors
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(1.dp, colors.stroke, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад", tint = colors.textPrimary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ProductHero(product: Product) {
    val colors = SkladTheme.colors
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.22f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(colors.accent.copy(alpha = 0.15f), colors.surface, colors.surface)
                    )
                )
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accent,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.accentSoft)
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    StockBadge(product)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "${product.sku} · ячейка ${product.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text("На складе", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                product.quantity.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                color = colors.textPrimary,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                product.unit,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            when {
                                product.isOutOfStock -> "Нет в наличии"
                                product.isLowStock -> "Ниже минимума"
                                else -> "В норме"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = when {
                                product.isOutOfStock -> colors.coral
                                product.isLowStock -> colors.amber
                                else -> colors.accent
                            },
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "мин. ${product.minQuantity} ${product.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                StockLevelBar(product.quantity, product.minQuantity)
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = colors.stroke)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    HeroStat(Formatters.price(product.price), "цена", Modifier.weight(1f))
                    HeroStat(Formatters.money(product.price * product.quantity), "на складе", Modifier.weight(1f))
                    HeroStat(product.minQuantity.toString(), "минимум", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleSmall, color = SkladTheme.colors.textPrimary, maxLines = 1)
        Text(label, style = MaterialTheme.typography.bodySmall, color = SkladTheme.colors.textMuted)
    }
}

@Composable
private fun OutOfStockBanner() {
    val colors = SkladTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.coral.copy(alpha = 0.08f))
            .border(1.dp, colors.coral.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = colors.coral, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Товар закончился", style = MaterialTheme.typography.titleSmall, color = colors.coral)
            Text(
                "Оформите приход или запустите пересчёт, чтобы обновить остаток.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun OperationsActions(onAction: (OpType) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        OpType.entries.forEach { type ->
            val tint = operationTint(type)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(tint.copy(alpha = 0.1f))
                    .border(1.dp, tint.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                    .clickable { onAction(type) }
                    .padding(vertical = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(operationIcon(type), contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(6.dp))
                Text(operationLabel(type), style = MaterialTheme.typography.labelMedium, color = tint)
            }
        }
    }
}

@Composable
private fun ProductInfoCard(product: Product) {
    GlassCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            InfoRow("Штрихкод", Formatters.barcode(product.barcode), mono = true)
            HorizontalDivider(color = SkladTheme.colors.stroke)
            InfoRow("Артикул", product.sku, mono = true)
            HorizontalDivider(color = SkladTheme.colors.stroke)
            InfoRow("Категория", product.category)
            HorizontalDivider(color = SkladTheme.colors.stroke)
            InfoRow("Ячейка", product.location, mono = true)
            HorizontalDivider(color = SkladTheme.colors.stroke)
            InfoRow("Единица", product.unit)
            HorizontalDivider(color = SkladTheme.colors.stroke)
            InfoRow("Обновлено", Formatters.day(product.updatedAt) + ", " + Formatters.time(product.updatedAt))
        }
    }
}

@Composable
private fun OperationDialog(
    product: Product,
    initialType: OpType,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (OpType, Int) -> Unit,
) {
    val colors = SkladTheme.colors
    var type by remember { mutableStateOf(initialType) }
    var quantity by remember {
        mutableStateOf(if (initialType == OpType.INVENTORY) product.quantity else 1)
    }
    val error = StockMath.validate(type, quantity, product.quantity)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.stroke),
        ) {
            Column(Modifier.padding(22.dp)) {
                Text(
                    operationLabel(type).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = operationTint(type),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Сейчас на складе: ${product.quantity} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    OpType.entries.forEach { item ->
                        OperationPill(
                            type = item,
                            selected = type == item,
                            modifier = Modifier.weight(1f),
                        ) {
                            type = item
                            quantity = if (item == OpType.INVENTORY) product.quantity else 1
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    if (type == OpType.INVENTORY) "Фактическое количество" else "Количество",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted,
                )
                Spacer(Modifier.height(7.dp))
                QuantityStepper(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth(),
                    min = 0,
                    max = if (type == OpType.WRITE_OFF) product.quantity else 99_999,
                    unit = product.unit,
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, style = MaterialTheme.typography.bodySmall, color = colors.coral)
                }
                Spacer(Modifier.height(16.dp))
                GradientButton(
                    text = when (type) {
                        OpType.RECEIVE -> "Оприходовать $quantity ${product.unit}"
                        OpType.WRITE_OFF -> "Списать $quantity ${product.unit}"
                        OpType.INVENTORY -> "Подтвердить пересчёт"
                    },
                    onClick = { onSubmit(type, quantity) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy && error == null,
                    icon = operationIcon(type),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Отмена",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}
