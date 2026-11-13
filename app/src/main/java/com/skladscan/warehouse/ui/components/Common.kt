package com.skladscan.warehouse.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.ui.theme.Lime
import com.skladscan.warehouse.ui.theme.LimeMint
import com.skladscan.warehouse.ui.theme.SkladTheme

@Composable
fun SkladBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = SkladTheme.colors
    Box(
        modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(colors.accent.copy(alpha = if (colors.isDark) 0.10f else 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.92f, size.height * 0.05f),
                    radius = size.width * 0.8f,
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.92f, size.height * 0.05f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(colors.cyan.copy(alpha = if (colors.isDark) 0.08f else 0.12f), Color.Transparent),
                    center = Offset(0f, size.height * 0.78f),
                    radius = size.width * 0.75f,
                ),
                radius = size.width * 0.75f,
                center = Offset(0f, size.height * 0.78f),
            )
        }
        content()
    }
}

fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier = graphicsLayer {
    // Scale is applied by the composable wrapper below; this modifier is kept for API symmetry.
}

@Composable
fun Modifier.pressable(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    enabled: Boolean = true,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "press",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
            enabled = enabled,
            onClick = onClick,
        )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = SkladTheme.colors.surface,
    borderColor: Color = SkladTheme.colors.stroke,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        val interaction = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            modifier = modifier.pressable(interaction, onClick),
            shape = shape,
            color = color,
            contentColor = SkladTheme.colors.textPrimary,
            border = BorderStroke(1.dp, borderColor),
            interactionSource = interaction,
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            contentColor = SkladTheme.colors.textPrimary,
            border = BorderStroke(1.dp, borderColor),
        ) {
            content()
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 54.dp,
) {
    val colors = SkladTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .height(height)
            .pressable(interaction, onClick, enabled)
            .clip(shape)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(colors.accentFill, LimeMint))
                } else {
                    Brush.horizontalGradient(listOf(colors.surfaceHigh, colors.surfaceHigh))
                }
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(9.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) colors.accentInk else colors.textMuted,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = SkladTheme.colors.textPrimary,
    enabled: Boolean = true,
    height: Dp = 52.dp,
) {
    val colors = SkladTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(height)
            .pressable(interaction, onClick, enabled)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceHigh.copy(alpha = 0.6f))
            .border(1.dp, colors.stroke, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = tint, maxLines = 1)
        }
    }
}

@Composable
fun SyncChip(
    online: Boolean,
    demoMode: Boolean,
    pending: Int,
    syncing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkladTheme.colors
    val (label, color) = when {
        syncing -> "Синхр..." to colors.accent
        pending > 0 -> "$pending в очереди" to colors.amber
        demoMode -> "Демо" to colors.cyan
        online -> "Онлайн" to colors.accent
        else -> "Офлайн" to colors.coral
    }
    val pulse = rememberInfiniteTransition(label = "sync")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "dot",
    )
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.28f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(color.copy(alpha = if (syncing || pending > 0) dotAlpha else 1f), CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        AnimatedContent(
            targetState = label,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(120)) },
            label = "sync-label",
        ) { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun ScreenHeader(
    eyebrow: String?,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = SkladTheme.colors
    Column(modifier.fillMaxWidth()) {
        if (eyebrow != null) {
            Text(
                eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
            )
            Spacer(Modifier.height(6.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            action?.invoke()
        }
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    count: Int? = null,
) {
    val colors = SkladTheme.colors
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        if (count != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.surfaceHigh)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelMedium,
                color = colors.accent,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = onAction != null) { onAction?.invoke() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun QuantityStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 9999,
    unit: String = "шт.",
) {
    val colors = SkladTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceHigh)
            .border(1.dp, colors.stroke, RoundedCornerShape(20.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepButton(
            icon = Icons.Rounded.Remove,
            enabled = value > min,
            onClick = { if (value > min) onValueChange(value - 1) },
        )
        Column(
            modifier = Modifier
                .width(86.dp)
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                maxLines = 1,
            )
            Text(unit, style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
        }
        StepButton(
            icon = Icons.Rounded.Add,
            enabled = value < max,
            onClick = { if (value < max) onValueChange(value + 1) },
        )
    }
}

@Composable
fun StepButton(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val colors = SkladTheme.colors
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (enabled) colors.surface else colors.surfaceHigh)
            .border(1.dp, colors.stroke, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) colors.textPrimary else colors.textMuted.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun StockBadge(product: Product, modifier: Modifier = Modifier) {
    val colors = SkladTheme.colors
    val color = when {
        product.isOutOfStock -> colors.coral
        product.isLowStock -> colors.amber
        else -> colors.accent
    }
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(
            "${product.quantity} ${product.unit}",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
fun IconTile(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    corner: Dp = 16.dp,
) {
    val colors = SkladTheme.colors
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    listOf(tint.copy(alpha = 0.22f), tint.copy(alpha = 0.06f))
                )
            )
            .border(1.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.44f))
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = SkladTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTile(icon = icon, tint = colors.accent, size = 64.dp, corner = 22.dp)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    val colors = SkladTheme.colors
    GlassCard(modifier = modifier, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(icon, tint, size = 34.dp, corner = 11.dp)
                Spacer(Modifier.weight(1f))
                if (hint != null) {
                    Text(hint, style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    mono: Boolean = false,
    valueColor: Color = SkladTheme.colors.textPrimary,
) {
    val colors = SkladTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            style = if (mono) {
                MaterialTheme.typography.bodyMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            } else {
                MaterialTheme.typography.titleSmall
            },
            color = valueColor,
            maxLines = 2,
        )
    }
}

@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val colors = SkladTheme.colors
    val background by animateColorAsState(
        if (selected) colors.accentFill else colors.surface,
        animationSpec = tween(180),
        label = "pill-bg",
    )
    val content by animateColorAsState(
        if (selected) colors.accentInk else colors.textSecondary,
        animationSpec = tween(180),
        label = "pill-content",
    )
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .border(1.dp, if (selected) Color.Transparent else colors.stroke, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = content, maxLines = 1)
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
fun SkladTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    val colors = SkladTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted,
                maxLines = 1,
            )
        },
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(15.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceHigh.copy(alpha = 0.55f),
            unfocusedContainerColor = colors.surfaceHigh.copy(alpha = 0.55f),
            disabledContainerColor = colors.surfaceHigh.copy(alpha = 0.3f),
            focusedBorderColor = colors.accent.copy(alpha = 0.65f),
            unfocusedBorderColor = colors.stroke,
            cursorColor = colors.accent,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.textMuted,
        ),
    )
}

@Composable
fun SearchField(    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Поиск по названию, SKU или штрихкоду",
) {
    val colors = SkladTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted, maxLines = 1)
        },
        leadingIcon = {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Очистить",
                    tint = colors.textMuted,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onValueChange("") }
                        .padding(5.dp),
                )
            }
        },
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.accent.copy(alpha = 0.7f),
            unfocusedBorderColor = colors.stroke,
            cursorColor = colors.accent,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
        ),
    )
}

@Composable
fun LabelValue(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    valueColor: Color = LocalContentColor.current,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = SkladTheme.colors.textMuted)
        Spacer(Modifier.height(3.dp))
        Text(value, style = valueStyle, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CheckMark(selected: Boolean, modifier: Modifier = Modifier) {
    val colors = SkladTheme.colors
    Box(
        modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) colors.accentFill else colors.surfaceHigh)
            .border(1.dp, if (selected) Color.Transparent else colors.stroke, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun TinyBarcode(color: Color = SkladTheme.colors.textMuted, modifier: Modifier = Modifier) {
    Canvas(modifier.size(width = 34.dp, height = 16.dp)) {
        val widths = listOf(2f, 1f, 3f, 1f, 2f, 3f, 1f, 1f, 3f, 1f, 2f, 1f)
        var x = 0f
        widths.forEachIndexed { index, w ->
            val barWidth = w.dp.toPx() * 0.8f
            if (index % 2 == 0) {
                drawRect(color, topLeft = Offset(x, 0f), size = androidx.compose.ui.geometry.Size(barWidth, size.height))
            }
            x += barWidth + 1.5.dp.toPx()
        }
    }
}

@Composable
fun QuantityUnit(quantity: Int, unit: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            quantity.toString(),
            style = MaterialTheme.typography.displayMedium,
            color = SkladTheme.colors.textPrimary,
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.width(4.dp))
        Text(unit, style = MaterialTheme.typography.bodyMedium, color = SkladTheme.colors.textSecondary)
    }
}

@Composable
fun Dot(color: Color, size: Dp = 6.dp) {
    Box(Modifier.size(size).background(color, CircleShape))
}
