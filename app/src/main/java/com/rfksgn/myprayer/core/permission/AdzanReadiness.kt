package com.rfksgn.myprayer.core.permission

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * The permissions the adzan depends on. Home's status row checks notifications (without them the stop control
 * never appears); the navigation bar's Setelan badge counts both.
 */
data class AdzanReadiness(
    val notifications: Boolean,
    val exactAlarm: Boolean
) {
    val missing: Int get() = listOf(notifications, exactAlarm).count { !it }

    companion object {
        fun check(context: Context) = AdzanReadiness(
            notifications = checkNotificationPermission(context),
            exactAlarm = checkExactAlarmPermission(context)
        )
    }
}

/** Re-checks on every resume, so returning from system settings updates the UI. */
@Composable
fun rememberAdzanReadiness(): AdzanReadiness {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var readiness by remember { mutableStateOf(AdzanReadiness.check(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) readiness = AdzanReadiness.check(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return readiness
}

fun checkNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

fun checkExactAlarmPermission(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else true
