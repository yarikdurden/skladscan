package com.skladscan.warehouse.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skladscan.warehouse.AppContainer
import com.skladscan.warehouse.domain.model.Formatters
import com.skladscan.warehouse.domain.model.OpType
import com.skladscan.warehouse.domain.model.Product
import com.skladscan.warehouse.domain.model.StockMath
import com.skladscan.warehouse.ui.AppUiState
import com.skladscan.warehouse.ui.components.GradientButton
import com.skladscan.warehouse.ui.components.IconTile
import com.skladscan.warehouse.ui.components.LabelValue
import com.skladscan.warehouse.ui.components.OutlineButton
import com.skladscan.warehouse.ui.components.OperationPill
import com.skladscan.warehouse.ui.components.QuantityStepper
import com.skladscan.warehouse.ui.components.SkladBackground
import com.skladscan.warehouse.ui.components.SkladTextField
import com.skladscan.warehouse.ui.components.StockBadge
import com.skladscan.warehouse.ui.components.StockLevelBar
import com.skladscan.warehouse.ui.components.SyncChip
import com.skladscan.warehouse.ui.components.TinyBarcode
import com.skladscan.warehouse.ui.simpleViewModelFactory
import com.skladscan.warehouse.ui.theme.Lime
import com.skladscan.warehouse.ui.theme.LimeMint
import com.skladscan.warehouse.ui.theme.SkladTheme

@Composable
fun ScannerScreen(
    container: AppContainer,
    app: AppUiState,
    onSync: () -> Unit,
) {
    val viewModel: ScannerViewModel = viewModel(
        factory = simpleViewModelFactory { ScannerViewModel(container) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val feedback = rememberScannerFeedback()

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var asked by rememberSaveable { mutableStateOf(false) }
    var manualDialog by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        granted = result
        asked = true
    }

    LaunchedEffect(Unit) {
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            asked = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.target) {
        if (state.target != null) {
            feedback.confirm(app.settings.soundEnabled, app.settings.vibrationEnabled)
        }
    }

    LaunchedEffect(state.snackbar) {
        if (state.snackbar != null) {
            kotlinx.coroutines.delay(3_000)
            viewModel.consumeSnackbar()
        }
    }

    val target = state.target
    var lastTarget by remember { mutableStateOf<ScanTarget?>(null) }
    if (target != null) lastTarget = target

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (granted) {
            CameraScanner(
                active = state.target == null,
                torch = state.torch,
                onBarcode = viewModel::onBarcode,
                onCameraError = { cameraError = it },
            )
            ScannerOverlay(
                app = app,
                torch = state.torch,
                cameraError = cameraError,
                onTorch = { viewModel.setTorch(!state.torch) },
                onSync = onSync,
                onManual = { manualDialog = true },
            )
        } else {
            PermissionContent(
                app = app,
                asked = asked,
                canAskAgain = activity?.let {
                    androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        Manifest.permission.CAMERA,
                    )
                } ?: false,
                onRequest = {
                    if (asked && activity?.let {
                            !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                                it,
                                Manifest.permission.CAMERA,
                            )
                        } != false
                    ) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null),
                            )
                        )
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onManual = { manualDialog = true },
                onSync = onSync,
            )
        }

        AnimatedVisibility(
            visible = target != null,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(160)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(enabled = !state.busy) { viewModel.dismiss() }
            )
        }

        AnimatedVisibility(
            visible = target != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.86f, stiffness = 360f),
            ) + fadeIn(tween(150)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(220)) + fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            lastTarget?.let { scanTarget ->
                ScanSheet(
                    target = scanTarget,
                    type = state.type,
                    quantity = state.quantity,
                    busy = state.busy,
                    onTypeChange = viewModel::setType,
                    onQuantityChange = viewModel::setQuantity,
                    onConfirm = viewModel::confirm,
                    onDismiss = viewModel::dismiss,
                    onAddProduct = viewModel::addProduct,
                )
            }
        }

        AnimatedVisibility(
            visible = state.snackbar != null,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            state.snackbar?.let { message ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SkladTheme.colors.surfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SkladTheme.colors.stroke),
                    shadowElevation = 12.dp,
                ) {
                    Row(
                        Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = SkladTheme.colors.accent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkladTheme.colors.textPrimary,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }

    if (manualDialog) {
        ManualBarcodeDialog(
            onDismiss = { manualDialog = false },
            onSubmit = { code ->
                manualDialog = false
                viewModel.onBarcode(code, "Вручную")
            },
        )
    }
}

@Composable
private fun ScannerTopBar(
    app: AppUiState,
    torch: Boolean,
    onTorch: () -> Unit,
    onSync: () -> Unit,
) {
    val colors = SkladTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(colors.accentFill, LimeMint))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.QrCodeScanner,
                contentDescription = null,
                tint = colors.accentInk,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                "СКЛАДСКАН",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                color = colors.textPrimary,
            )
            Text(
                app.settings.warehouseName,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        Spacer(Modifier.weight(1f))
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
                .background(colors.surface.copy(alpha = 0.75f))
                .border(1.dp, colors.stroke, CircleShape)
                .clickable(onClick = onTorch),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (torch) Icons.Rounded.FlashOff else Icons.Rounded.FlashOn,
                contentDescription = "Фонарик",
                tint = if (torch) colors.accent else colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ScannerOverlay(
    app: AppUiState,
    torch: Boolean,
    cameraError: String?,
    onTorch: () -> Unit,
    onSync: () -> Unit,
    onManual: () -> Unit,
) {
    val colors = SkladTheme.colors
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val frameWidth = minOf(maxWidth * 0.84f, 340.dp)
        val frameHeight = frameWidth * 0.64f
        val frameOffset = (-18).dp
        val holeCorner = 26.dp

        Canvas(Modifier.fillMaxSize()) {
            val hole = Rect(
                offset = Offset(
                    (size.width - frameWidth.toPx()) / 2f,
                    size.height / 2f + frameOffset.toPx() - frameHeight.toPx() / 2f,
                ),
                size = Size(frameWidth.toPx(), frameHeight.toPx()),
            )
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, size))
                addRoundRect(
                    RoundRect(
                        rect = hole,
                        cornerRadius = CornerRadius(holeCorner.toPx(), holeCorner.toPx()),
                    )
                )
            }
            drawPath(path, Color.Black.copy(alpha = 0.55f))
        }

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(170.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                    )
                )
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(230.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                    )
                )
        )

        Column(Modifier.align(Alignment.TopCenter)) {
            ScannerTopBar(app = app, torch = torch, onTorch = onTorch, onSync = onSync)
        }

        if (cameraError == null) {
            ScanFrame(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = frameOffset)
                    .size(frameWidth, frameHeight),
            )
        } else {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 30.dp),
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.stroke),
            ) {
                Column(
                    Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconTile(Icons.Rounded.CameraAlt, colors.amber, size = 52.dp, corner = 17.dp)
                    Spacer(Modifier.height(12.dp))
                    Text("Камера недоступна", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Можно продолжить работу: введите штрихкод вручную.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlineButton("Ввести вручную", onManual, icon = Icons.Rounded.Keyboard, height = 46.dp)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(colors.accent))
                Spacer(Modifier.width(7.dp))
                Text(
                    if (cameraError == null) "Наведите на штрихкод" else "Ручной ввод",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
            Spacer(Modifier.height(7.dp))
            Text(
                "EAN-13 · Code 128 · QR · Data Matrix",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
            )
            Spacer(Modifier.height(16.dp))
            OutlineButton(
                text = "Ввести вручную",
                onClick = onManual,
                icon = Icons.Rounded.Keyboard,
                tint = Color.White,
                height = 46.dp,
            )
        }
    }
}

@Composable
private fun ScanFrame(modifier: Modifier = Modifier) {
    val colors = SkladTheme.colors
    val transition = rememberInfiniteTransition(label = "scanline")
    val line by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "line",
    )
    val cornerLength = 26.dp
    val cornerStroke = 3.dp

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val len = cornerLength.toPx()
            val stroke = cornerStroke.toPx()
            val inset = 2.dp.toPx()
            val radius = 24.dp.toPx()

            drawRoundRect(
                color = colors.accent.copy(alpha = 0.18f),
                cornerRadius = CornerRadius(radius, radius),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
            )

            fun bracket(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
                val path = Path().apply {
                    moveTo(x1, y1)
                    lineTo(x2, y2)
                    lineTo(x3, y3)
                }
                drawPath(
                    path,
                    color = colors.accent,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = stroke,
                        cap = StrokeCap.Round,
                    ),
                )
            }

            bracket(inset, inset + len, inset, inset, inset + len, inset)
            bracket(w - inset - len, inset, w - inset, inset, w - inset, inset + len)
            bracket(inset, h - inset - len, inset, h - inset, inset + len, h - inset)
            bracket(w - inset - len, h - inset, w - inset, h - inset, w - inset, h - inset - len)
        }

        val travel = 205.dp
        Box(
            Modifier
                .fillMaxWidth()
                .height(26.dp)
                .offset(y = (travel - 26.dp) * line)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, colors.accent.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
            )
            Box(
                Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, colors.accent.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
            )
        }

        Column(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 12.dp)
        ) {
            Text(
                "СКАНИРОВАНИЕ",
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun PermissionContent(
    app: AppUiState,
    asked: Boolean,
    canAskAgain: Boolean,
    onRequest: () -> Unit,
    onManual: () -> Unit,
    onSync: () -> Unit,
) {
    val colors = SkladTheme.colors
    SkladBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(colors.accentFill, LimeMint))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.QrCodeScanner,
                        contentDescription = null,
                        tint = colors.accentInk,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "СКЛАДСКАН",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp),
                        color = colors.textPrimary,
                    )
                    Text("Мобильный терминал", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                Spacer(Modifier.weight(1f))
                SyncChip(
                    online = app.online,
                    demoMode = app.settings.demoMode,
                    pending = app.summary.pendingOperations,
                    syncing = app.syncing,
                    onClick = onSync,
                )
            }

            Spacer(Modifier.weight(1f))

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(44.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(colors.accent.copy(alpha = 0.2f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 1.5.dp.toPx()
                        repeat(3) { index ->
                            drawCircle(
                                color = colors.accent.copy(alpha = 0.16f - index * 0.04f),
                                radius = size.minDimension * (0.24f + index * 0.12f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke),
                            )
                        }
                        val len = 26.dp.toPx()
                        val inset = size.width * 0.29f
                        val c = colors.accent
                        drawLine(c, Offset(inset, inset), Offset(inset + len, inset), 3.dp.toPx(), StrokeCap.Round)
                        drawLine(c, Offset(inset, inset), Offset(inset, inset + len), 3.dp.toPx(), StrokeCap.Round)
                        drawLine(c, Offset(size.width - inset, inset), Offset(size.width - inset - len, inset), 3.dp.toPx(), StrokeCap.Round)
                        drawLine(c, Offset(size.width - inset, inset), Offset(size.width - inset, inset + len), 3.dp.toPx(), StrokeCap.Round)
                        drawLine(c, Offset(inset, size.height - inset), Offset(inset + len, size.height - inset), 3.dp.toPx(), StrokeCap.Round)
                        drawLine(c, Offset(inset, size.height - inset), Offset(inset, size.height - inset - len), 3.dp.toPx(), StrokeCap.Round)
                        drawLine(c, Offset(size.width - inset, size.height - inset), Offset(size.width - inset - len, size.height - inset), 3.dp.toPx(), StrokeCap.Round)
                        drawLine(c, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - len), 3.dp.toPx(), StrokeCap.Round)
                    }
                    Icon(
                        Icons.Rounded.QrCodeScanner,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(64.dp),
                    )
                }
            }

            Spacer(Modifier.height(30.dp))
            Text(
                "Камера — это сканер",
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Наведите объектив на штрихкод товара — карточка появится мгновенно. Распознавание идёт на устройстве, кадры никуда не отправляются.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                listOf("EAN-13", "Code 128", "QR", "Data Matrix").forEach { format ->
                    Text(
                        format,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.stroke, CircleShape)
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = if (asked && !canAskAgain) "Открыть настройки" else "Разрешить камеру",
                onClick = onRequest,
                icon = if (asked && !canAskAgain) Icons.Rounded.Settings else Icons.Rounded.CameraAlt,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            OutlineButton(
                text = "Ввести штрихкод вручную",
                onClick = onManual,
                icon = Icons.Rounded.Keyboard,
                modifier = Modifier.fillMaxWidth(),
            )
            if (asked && !canAskAgain) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Разрешение отключено. Включите камеру в настройках приложения.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.amber,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ScanSheet(
    target: ScanTarget,
    type: OpType,
    quantity: Int,
    busy: Boolean,
    onTypeChange: (OpType) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onAddProduct: (String, String, Double, Int, String, Int, String) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 620.dp),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        color = SkladTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SkladTheme.colors.stroke),
        shadowElevation = 30.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 22.dp)
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(SkladTheme.colors.textMuted.copy(alpha = 0.4f))
            )
            Spacer(Modifier.height(12.dp))
            when (target) {
                is ScanTarget.Found -> FoundContent(
                    product = target.product,
                    format = target.format,
                    type = type,
                    quantity = quantity,
                    busy = busy,
                    onTypeChange = onTypeChange,
                    onQuantityChange = onQuantityChange,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss,
                )
                is ScanTarget.Unknown -> UnknownContent(
                    barcode = target.barcode,
                    format = target.format,
                    busy = busy,
                    onAddProduct = onAddProduct,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun FoundContent(
    product: Product,
    format: String,
    type: OpType,
    quantity: Int,
    busy: Boolean,
    onTypeChange: (OpType) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = SkladTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            format.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.accent,
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.accentSoft)
                .padding(horizontal = 9.dp, vertical = 5.dp),
        )
        Spacer(Modifier.weight(1f))
        StockBadge(product)
        Spacer(Modifier.width(8.dp))
        CloseButton(onDismiss)
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "${product.sku} · ${product.location}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.weight(1f))
        Text(Formatters.price(product.price), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
    }
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text("Остаток", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    product.quantity.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.textPrimary,
                    fontSize = 40.sp,
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    product.unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "мин. ${product.minQuantity} ${product.unit}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textMuted,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    product.isOutOfStock -> "Нет в наличии"
                    product.isLowStock -> "Пора пополнить"
                    else -> "В норме"
                },
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    product.isOutOfStock -> colors.coral
                    product.isLowStock -> colors.amber
                    else -> colors.accent
                },
            )
        }
    }
    Spacer(Modifier.height(9.dp))
    StockLevelBar(product.quantity, product.minQuantity)
    Spacer(Modifier.height(18.dp))

    Text("Действие", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OperationPill(OpType.RECEIVE, type == OpType.RECEIVE, Modifier.weight(1f)) { onTypeChange(OpType.RECEIVE) }
        OperationPill(OpType.WRITE_OFF, type == OpType.WRITE_OFF, Modifier.weight(1f)) { onTypeChange(OpType.WRITE_OFF) }
        OperationPill(OpType.INVENTORY, type == OpType.INVENTORY, Modifier.weight(1f)) { onTypeChange(OpType.INVENTORY) }
    }
    Spacer(Modifier.height(16.dp))

    Text(
        if (type == OpType.INVENTORY) "Фактическое количество" else "Количество",
        style = MaterialTheme.typography.labelMedium,
        color = colors.textMuted,
    )
    Spacer(Modifier.height(8.dp))
    QuantityStepper(
        value = quantity,
        onValueChange = onQuantityChange,
        modifier = Modifier.fillMaxWidth(),
        min = 0,
        max = if (type == OpType.WRITE_OFF) product.quantity else 99_999,
        unit = product.unit,
    )
    val error = StockMath.validate(type, quantity, product.quantity)
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
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth(),
        enabled = !busy && error == null,
        icon = when (type) {
            OpType.RECEIVE -> Icons.Rounded.ArrowDownward
            OpType.WRITE_OFF -> Icons.Rounded.ArrowUpward
            OpType.INVENTORY -> Icons.Rounded.FactCheck
        },
    )
    Spacer(Modifier.height(10.dp))
    Text(
        "Операция сохранится на устройстве и уйдёт на сервер автоматически",
        style = MaterialTheme.typography.bodySmall,
        color = colors.textMuted,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun UnknownContent(
    barcode: String,
    format: String,
    busy: Boolean,
    onAddProduct: (String, String, Double, Int, String, Int, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = SkladTheme.colors
    var name by remember(barcode) { mutableStateOf("") }
    var category by remember(barcode) { mutableStateOf("") }
    var price by remember(barcode) { mutableStateOf("") }
    var quantity by remember(barcode) { mutableStateOf("0") }
    var location by remember(barcode) { mutableStateOf("") }
    var minQuantity by remember(barcode) { mutableStateOf("5") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "НОВЫЙ ТОВАР",
            style = MaterialTheme.typography.labelSmall,
            color = colors.amber,
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.amber.copy(alpha = 0.12f))
                .padding(horizontal = 9.dp, vertical = 5.dp),
        )
        Spacer(Modifier.weight(1f))
        CloseButton(onDismiss)
    }
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        TinyBarcode(color = colors.textMuted)
        Spacer(Modifier.width(8.dp))
        Text(
            Formatters.barcode(barcode),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = colors.textPrimary,
        )
        Spacer(Modifier.width(8.dp))
        Text(format, style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
    }
    Spacer(Modifier.height(6.dp))
    Text(
        "Штрихкод не найден в каталоге. Добавьте товар — он сразу появится на складе.",
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary,
    )
    Spacer(Modifier.height(14.dp))
    SkladTextField(
        value = name,
        onValueChange = { name = it },
        label = "Название товара",
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(9.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        SkladTextField(
            value = category,
            onValueChange = { category = it },
            label = "Категория",
            modifier = Modifier.weight(1f),
        )
        SkladTextField(
            value = price,
            onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' } },
            label = "Цена, ₽",
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(0.7f),
        )
    }
    Spacer(Modifier.height(9.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        SkladTextField(
            value = quantity,
            onValueChange = { quantity = it.filter { ch -> ch.isDigit() } },
            label = "Количество",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
        SkladTextField(
            value = minQuantity,
            onValueChange = { minQuantity = it.filter { ch -> ch.isDigit() } },
            label = "Мин. остаток",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
        SkladTextField(
            value = location,
            onValueChange = { location = it },
            label = "Ячейка",
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(16.dp))
    GradientButton(
        text = "Добавить в каталог",
        onClick = {
            onAddProduct(
                name.trim(),
                category.trim(),
                price.replace(',', '.').toDoubleOrNull() ?: 0.0,
                quantity.toIntOrNull() ?: 0,
                location.trim(),
                minQuantity.toIntOrNull() ?: 5,
                "шт.",
            )
        },
        icon = Icons.Rounded.Add,
        modifier = Modifier.fillMaxWidth(),
        enabled = name.isNotBlank() && !busy,
        height = 52.dp,
    )
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SkladTheme.colors.surfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Close,
            contentDescription = "Закрыть",
            tint = SkladTheme.colors.textSecondary,
            modifier = Modifier.size(17.dp),
        )
    }
}

@Composable
private fun ManualBarcodeDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var code by remember { mutableStateOf("") }
    val colors = SkladTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.stroke),
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Rounded.Keyboard, colors.accent, size = 42.dp, corner = 14.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Ручной ввод", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        Text(
                            "Введите цифры со штрихкода",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { value -> code = value.filter { it.isLetterOrDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                    placeholder = {
                        Text("4601000012343", color = colors.textMuted, style = MaterialTheme.typography.bodyMedium)
                    },
                    shape = RoundedCornerShape(15.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent.copy(alpha = 0.7f),
                        unfocusedBorderColor = colors.stroke,
                        cursorColor = colors.accent,
                        focusedContainerColor = colors.surfaceHigh.copy(alpha = 0.5f),
                        unfocusedContainerColor = colors.surfaceHigh.copy(alpha = 0.5f),
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                    ),
                )
                Spacer(Modifier.height(16.dp))
                GradientButton(
                    text = "Найти товар",
                    onClick = { if (code.isNotBlank()) onSubmit(code) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = code.isNotBlank(),
                    icon = Icons.Rounded.QrCodeScanner,
                )
                Spacer(Modifier.height(6.dp))
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
