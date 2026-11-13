package com.skladscan.warehouse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skladscan.warehouse.domain.model.Formatters
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.StockOperation
import com.skladscan.warehouse.domain.model.SyncStatus
import com.skladscan.warehouse.ui.theme.SkladTheme

@Composable
fun operationTint(type: OpType): Color = when (type) {
    OpType.RECEIVE -> SkladTheme.colors.accent
    OpType.WRITE_OFF -> SkladTheme.colors.coral
    OpType.INVENTORY -> SkladTheme.colors.cyan
}

fun operationIcon(type: OpType): ImageVector = when (type) {
    OpType.RECEIVE -> Icons.Rounded.ArrowDownward
    OpType.WRITE_OFF -> Icons.Rounded.ArrowUpward
    OpType.INVENTORY -> Icons.Rounded.FactCheck
}

fun operationLabel(type: OpType): String = when (type) {
    OpType.RECEIVE -> "Приход"
    OpType.WRITE_OFF -> "Списание"
    OpType.INVENTORY -> "Пересчёт"
}

@Composable
fun OperationPill(
    type: OpType,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = SkladTheme.colors
    val tint = operationTint(type)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected) tint.copy(alpha = 0.14f) else colors.surfaceHigh.copy(alpha = 0.5f))
            .border(1.dp, if (selected) tint.copy(alpha = 0.45f) else colors.stroke, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            operationIcon(type),
            contentDescription = null,
            tint = if (selected) tint else colors.textMuted,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            operationLabel(type),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) tint else colors.textSecondary,
            maxLines = 1,
        )
    }
}

@Composable
fun OperationTypeBadge(
    type: OpType,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier,
) {
    val tint = operationTint(type)
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(tint.copy(alpha = 0.13f))
            .border(1.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(size * 0.34f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            operationIcon(type),
            contentDescription = operationLabel(type),
            tint = tint,
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

@Composable
fun OperationRow(
    operation: StockOperation,
    modifier: Modifier = Modifier,
    showFullDate: Boolean = false,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = SkladTheme.colors
    val tint = operationTint(operation.type)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OperationTypeBadge(operation.type)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                operation.productName,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    operation.note.ifBlank { operationLabel(operation.type) },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (showFullDate) Formatters.day(operation.createdAt) + ", " + Formatters.time(operation.createdAt)
                    else Formatters.time(operation.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    maxLines = 1,
                )
                if (operation.isPending) {
                    Spacer(Modifier.width(5.dp))
                    SyncMark(operation.syncStatus)
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                operation.signedQuantity,
                style = MaterialTheme.typography.titleMedium,
                color = tint,
            )
            Text(
                "остаток ${operation.quantityAfter}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )
        }
    }
}

@Composable
fun SyncMark(status: SyncStatus) {
    val colors = SkladTheme.colors
    val color = when (status) {
        SyncStatus.PENDING -> colors.amber
        SyncStatus.SYNCING -> colors.cyan
        SyncStatus.FAILED -> colors.coral
        SyncStatus.SYNCED -> colors.accent
    }
    Icon(
        Icons.Rounded.Sync,
        contentDescription = when (status) {
            SyncStatus.PENDING -> "Ожидает синхронизации"
            SyncStatus.SYNCING -> "Синхронизируется"
            SyncStatus.FAILED -> "Ошибка синхронизации"
            SyncStatus.SYNCED -> "Синхронизировано"
        },
        tint = color,
        modifier = Modifier.size(13.dp),
    )
}
