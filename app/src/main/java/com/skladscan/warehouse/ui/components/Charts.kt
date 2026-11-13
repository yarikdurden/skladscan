package com.skladscan.warehouse.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skladscan.warehouse.domain.model.DayBar
import com.skladscan.warehouse.ui.theme.SkladTheme
import kotlin.math.max

data class CategorySlice(val label: String, val value: Double, val color: Color)

@Composable
fun WeekBarChart(
    bars: List<DayBar>,
    modifier: Modifier = Modifier,
) {
    val colors = SkladTheme.colors
    val progress = remember { Animatable(0f) }
    LaunchedEffect(bars) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
    }
    val maxValue = max(1, bars.maxOfOrNull { max(it.incoming, it.outgoing) } ?: 1)
    val incoming = bars.sumOf { it.incoming }
    val outgoing = bars.sumOf { it.outgoing }

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(colors.accent, "Приход $incoming")
            Spacer(Modifier.width(14.dp))
            LegendDot(colors.coral, "Расход $outgoing")
            Spacer(Modifier.weight(1f))
            Text("7 дней", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
        }
        Spacer(Modifier.height(18.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val gridLines = 4
                repeat(gridLines) { i ->
                    val y = size.height * i / (gridLines - 1)
                    drawLine(
                        color = colors.chartGrid,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                if (bars.isEmpty()) return@Canvas
                val groupWidth = size.width / bars.size
                val barWidth = (groupWidth * 0.22f).coerceAtMost(15.dp.toPx())
                val gap = 4.dp.toPx()
                val chartHeight = size.height - 2.dp.toPx()
                bars.forEachIndexed { index, bar ->
                    val centerX = groupWidth * index + groupWidth / 2f
                    val incomingHeight = (bar.incoming.toFloat() / maxValue) * chartHeight * progress.value
                    val outgoingHeight = (bar.outgoing.toFloat() / maxValue) * chartHeight * progress.value

                    if (incomingHeight > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(colors.accent, colors.accent.copy(alpha = 0.55f))),
                            topLeft = Offset(centerX - barWidth - gap / 2f, chartHeight - incomingHeight),
                            size = Size(barWidth, incomingHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                        )
                    }
                    if (outgoingHeight > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(colors.coral, colors.coral.copy(alpha = 0.45f))),
                            topLeft = Offset(centerX + gap / 2f, chartHeight - outgoingHeight),
                            size = Size(barWidth, outgoingHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Text(
                    bar.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = SkladTheme.colors.textSecondary)
    }
}

@Composable
fun Sparkline(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val maxValue = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
        val minValue = values.minOrNull() ?: 0f
        val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f
        val step = size.width / (values.size - 1)
        val points = values.mapIndexed { index, value ->
            Offset(
                x = step * index,
                y = size.height - ((value - minValue) / range) * (size.height * 0.78f) - size.height * 0.1f,
            )
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fill = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            fill,
            Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), Color.Transparent)),
        )
        drawPath(
            path,
            color = color,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(color, radius = 3.2.dp.toPx(), center = points.last())
        drawCircle(color.copy(alpha = 0.2f), radius = 7.dp.toPx(), center = points.last())
    }
}

@Composable
fun CategoryDonut(
    slices: List<CategorySlice>,
    centerLabel: String,
    centerValue: String,
    modifier: Modifier = Modifier,
) {
    val colors = SkladTheme.colors
    val total = slices.sumOf { it.value }.takeIf { it > 0.0 } ?: 1.0
    val progress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val inset = stroke / 2 + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            var start = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total * 360f).toFloat() * progress.value
                if (sweep > 0.8f) {
                    drawArc(
                        color = slice.color,
                        startAngle = start,
                        sweepAngle = sweep - 2.5f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
            Text(centerValue, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
        }
    }
}

@Composable
fun CategoryLegend(slices: List<CategorySlice>, total: Double, modifier: Modifier = Modifier) {
    val colors = SkladTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        slices.forEach { slice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(slice.color))
                Spacer(Modifier.width(8.dp))
                Text(
                    slice.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${((slice.value / total.coerceAtLeast(1.0)) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary,
                )
            }
        }
    }
}

@Composable
fun StockLevelBar(
    quantity: Int,
    minQuantity: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SkladTheme.colors
    val color = when {
        quantity <= 0 -> colors.coral
        quantity <= minQuantity -> colors.amber
        else -> colors.accent
    }
    val ratio = if (minQuantity <= 0) 1f else (quantity.toFloat() / (minQuantity * 2f)).coerceIn(0.03f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(colors.surfaceHigh)
    ) {
        Box(
            Modifier
                .fillMaxWidth(ratio)
                .height(8.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.65f)))
                )
        )
        if (minQuantity > 0) {
            Box(
                Modifier
                    .fillMaxWidth(0.5f)
                    .height(8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(Modifier.width(2.dp).height(8.dp).background(colors.textMuted.copy(alpha = 0.5f)))
            }
        }
    }
}
