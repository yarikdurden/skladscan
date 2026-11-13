package com.skladscan.warehouse.ui.scanner

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class ScannerFeedback(context: Context) {

    private val tone: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_MUSIC, 65)
    }.getOrNull()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun confirm(playSound: Boolean, vibrate: Boolean) {
        if (playSound) {
            runCatching { tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 80) }
        }
        if (vibrate) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(38, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(38)
                }
            }
        }
    }

    fun release() {
        runCatching { tone?.release() }
    }
}

@Composable
fun rememberScannerFeedback(): ScannerFeedback {
    val context = LocalContext.current
    val feedback = remember { ScannerFeedback(context) }
    DisposableEffect(Unit) {
        onDispose { feedback.release() }
    }
    return feedback
}
