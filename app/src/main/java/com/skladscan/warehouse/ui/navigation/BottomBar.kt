package com.skladscan.warehouse.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.skladscan.warehouse.ui.theme.LimeMint
import com.skladscan.warehouse.ui.theme.SkladTheme

data class NavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val center: Boolean = false,
)

val tabs = listOf(
    NavTab("stock", "Склад", Icons.Rounded.Inventory2),
    NavTab("history", "История", Icons.Rounded.History),
    NavTab("scanner", "Сканер", Icons.Rounded.QrCodeScanner, center = true),
    NavTab("analytics", "Сводка", Icons.Rounded.Insights),
    NavTab("settings", "Настройки", Icons.Rounded.Settings),
)

@Composable
fun SkladBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    val colors = SkladTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp)
            .padding(top = 18.dp, bottom = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color.Black.copy(alpha = 0.5f),
                ),
            shape = RoundedCornerShape(28.dp),
            color = colors.surface.copy(alpha = 0.98f),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.stroke),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                tabs.forEach { tab ->
                    if (tab.center) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        NavItem(
                            tab = tab,
                            selected = currentRoute == tab.route,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(tab.route) },
                        )
                    }
                }
            }
        }

        ScanCircleButton(
            selected = currentRoute == "scanner",
            onClick = { onNavigate("scanner") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
                .zIndex(2f),
        )
    }
}

@Composable
private fun NavItem(
    tab: NavTab,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = SkladTheme.colors
    val tint by animateColorAsState(
        if (selected) colors.accent else colors.textMuted,
        animationSpec = tween(200),
        label = "nav-tint",
    )
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = tint, modifier = Modifier.size(21.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.1.sp),
            color = tint,
            maxLines = 1,
        )
        Spacer(Modifier.height(3.dp))
        Box(
            Modifier
                .size(width = if (selected) 14.dp else 0.dp, height = 3.dp)
                .clip(CircleShape)
                .background(if (selected) colors.accent else Color.Transparent)
        )
    }
}

@Composable
private fun ScanCircleButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SkladTheme.colors
    val pulse = rememberInfiniteTransition(label = "scan-pulse")
    val glow by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "glow",
    )
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(66.dp)
            .clip(CircleShape)
            .background(colors.backgroundElevated)
            .border(1.dp, colors.stroke, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (selected) 18.dp else 10.dp,
                    shape = CircleShape,
                    spotColor = colors.accent.copy(alpha = glow),
                )
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.accentFill, LimeMint))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.QrCodeScanner,
                contentDescription = "Сканер",
                tint = colors.accentInk,
                modifier = Modifier.size(27.dp),
            )
        }
    }
}
private val Double.sp get() = this.dp
