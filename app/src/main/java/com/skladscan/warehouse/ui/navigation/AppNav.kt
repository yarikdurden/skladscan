package com.skladscan.warehouse.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.ui.AppUiState
import com.skladscan.warehouse.ui.MainViewModel
import com.skladscan.warehouse.ui.analytics.AnalyticsScreen
import com.skladscan.warehouse.ui.history.HistoryScreen
import com.skladscan.warehouse.ui.scanner.ScannerScreen
import com.skladscan.warehouse.ui.settings.SettingsScreen
import com.skladscan.warehouse.ui.stock.ProductDetailScreen
import com.skladscan.warehouse.ui.stock.StockScreen
import com.skladscan.warehouse.ui.theme.SkladTheme
import com.skladscan.warehouse.ui.theme.WarehouseTheme
import com.skladscan.warehouse.ui.simpleViewModelFactory

@Composable
fun WarehouseRoot(container: AppContainer) {
    val mainViewModel: MainViewModel = viewModel(
        factory = simpleViewModelFactory { MainViewModel(container) }
    )
    val settings by mainViewModel.settings.collectAsStateWithLifecycle()
    val summary by mainViewModel.summary.collectAsStateWithLifecycle()
    val online by mainViewModel.online.collectAsStateWithLifecycle()
    val syncState by mainViewModel.syncState.collectAsStateWithLifecycle()

    val appState = AppUiState(
        settings = settings,
        summary = summary,
        online = online,
        syncing = syncState.syncing,
        syncMessage = syncState.message,
        syncError = syncState.error,
    )

    WarehouseTheme(theme = settings.theme) {
        WarehouseNavHost(
            container = container,
            app = appState,
            mainViewModel = mainViewModel,
        )
    }
}

@Composable
private fun WarehouseNavHost(
    container: AppContainer,
    app: AppUiState,
    mainViewModel: MainViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf("scanner", "stock", "history", "analytics", "settings")
    val colors = SkladTheme.colors

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val toastVisible = app.syncMessage != null || app.syncError != null
    LaunchedEffect(app.syncMessage, app.syncError) {
        if (toastVisible) {
            kotlinx.coroutines.delay(3_400)
            mainViewModel.clearSyncMessage()
        }
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    SkladBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = ::navigateTab,
                    )
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "scanner",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding()),
                enterTransition = {
                    fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 12 }
                },
                exitTransition = {
                    fadeOut(tween(140)) + slideOutHorizontally(tween(200)) { -it / 24 }
                },
                popEnterTransition = {
                    fadeIn(tween(220)) + slideInHorizontally(tween(260)) { -it / 12 }
                },
                popExitTransition = {
                    fadeOut(tween(140)) + slideOutHorizontally(tween(200)) { it / 24 }
                },
            ) {
                composable("scanner") {
                    ScannerScreen(
                        container = container,
                        app = app,
                        onSync = mainViewModel::syncNow,
                    )
                }
                composable("stock") {
                    StockScreen(
                        container = container,
                        app = app,
                        onSync = mainViewModel::syncNow,
                        onOpenProduct = { id -> navController.navigate("product/$id") },
                        onOpenScanner = { navigateTab("scanner") },
                    )
                }
                composable("history") {
                    HistoryScreen(
                        container = container,
                        app = app,
                        onSync = mainViewModel::syncNow,
                    )
                }
                composable("analytics") {
                    AnalyticsScreen(
                        container = container,
                        app = app,
                        onSync = mainViewModel::syncNow,
                        onOpenProduct = { id -> navController.navigate("product/$id") },
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        container = container,
                        app = app,
                        onSync = mainViewModel::syncNow,
                    )
                }
                composable(
                    route = "product/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.LongType }),
                ) { entry ->
                    ProductDetailScreen(
                        container = container,
                        productId = entry.arguments?.getLong("productId") ?: 0L,
                        app = app,
                        onBack = { navController.popBackStack() },
                        onSync = mainViewModel::syncNow,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = toastVisible,
            enter = slideInVertically(tween(220)) { it / 2 } + fadeIn(tween(160)),
            exit = slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(140)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (showBottomBar) 96.dp else 22.dp)
                .padding(horizontal = 20.dp),
        ) {
            val error = app.syncError != null
            val tint = if (error) colors.coral else colors.accent
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.surfaceHigh,
                border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
                shadowElevation = 16.dp,
            ) {
                Row(
                    Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (error) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        app.syncError ?: app.syncMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                    )
                }
            }
        }
    }
}
