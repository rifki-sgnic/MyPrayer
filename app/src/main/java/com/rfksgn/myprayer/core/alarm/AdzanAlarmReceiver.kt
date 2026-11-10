package com.rfksgn.myprayer.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.rfksgn.myprayer.core.service.AdzanForegroundService
import com.rfksgn.myprayer.data.local.LocationPreferences
import com.rfksgn.myprayer.domain.usecase.GetPrayerScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdzanAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        // 1. Jika HP habis restart atau ganti jam, jadwalkan ulang semua alarm
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_TIME_CHANGED) {
            rescheduleAllAlarms(context)
            return
        }

        // 2. Jika alarm waktu sholat berbunyi, nyalakan ForegroundService Adzan
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Sholat"
        val serviceIntent = Intent(context, AdzanForegroundService::class.java).apply {
            this.action = AdzanForegroundService.ACTION_START_ADZAN
            putExtra(AdzanForegroundService.EXTRA_PRAYER_NAME, prayerName)
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        // 3. Jadwalkan ulang otomatis untuk hari berikutnya
        rescheduleAllAlarms(context)
    }

    private fun rescheduleAllAlarms(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = LocationPreferences(context)
                val savedLoc = prefs.locationFlow.first()
                val modesMap = prefs.reminderModesFlow.first()
                val schedule = GetPrayerScheduleUseCase()(savedLoc.latitude, savedLoc.longitude)

                AdzanAlarmScheduler(context).scheduleDailyAlarms(schedule, modesMap)
            } finally {
                pendingResult.finish()
            }
        }
    }
}