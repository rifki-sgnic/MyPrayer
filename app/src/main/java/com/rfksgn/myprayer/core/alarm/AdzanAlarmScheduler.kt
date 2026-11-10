package com.rfksgn.myprayer.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rfksgn.myprayer.MainActivity
import com.rfksgn.myprayer.domain.model.PrayerSchedule
import com.rfksgn.myprayer.domain.model.ReminderMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AdzanAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val prayerCodes = mapOf(
        "Subuh" to 201,
        "Dzuhur" to 202,
        "Ashar" to 203,
        "Maghrib" to 204,
        "Isya" to 205
    )

    fun scheduleDailyAlarms(
        schedule: PrayerSchedule,
        modesMap: Map<String, ReminderMode>
    ) {
        val prayerTimesMap = mapOf(
            "Subuh" to schedule.fajr,
            "Dzuhur" to schedule.dhuhr,
            "Ashar" to schedule.asr,
            "Maghrib" to schedule.maghrib,
            "Isya" to schedule.isha
        )

        val nowMillis = System.currentTimeMillis()
        val today = LocalDate.now()

        prayerTimesMap.forEach { (prayerName, timeString) ->
            val requestCode = prayerCodes[prayerName] ?: return@forEach
            val mode = modesMap[prayerName] ?: ReminderMode.ADZAN

            // Jika mode SILENT (Nonaktif), batalkan alarm untuk waktu sholat tersebut
            if (mode == ReminderMode.SILENT) {
                cancelAlarm(prayerName, requestCode)
                return@forEach
            }

            val triggerMillis = parseTimeToMillis(today, timeString)

            // Jika waktu sholat hari ini masih akan datang, jadwalkan hari ini.
            // Jika sudah lewat (misal Subuh hari ini sudah lewat), jadwalkan untuk besok.
            val targetMillis = if (triggerMillis > nowMillis) {
                triggerMillis
            } else {
                parseTimeToMillis(today.plusDays(1), timeString)
            }

            setExactPrayerAlarm(prayerName, requestCode, targetMillis)
        }
    }

    private fun setExactPrayerAlarm(prayerName: String, requestCode: Int, triggerAtMillis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val receiverIntent = Intent(context, AdzanAlarmReceiver::class.java).apply {
            putExtra(AdzanAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showAppIntent = Intent(context, MainActivity::class.java)
        val showAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setAlarmClock menjamin alarm tepat waktu 100% walaupun HP dalam kondisi Deep Doze
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showAppPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    private fun cancelAlarm(prayerName: String, requestCode: Int) {
        val intent = Intent(context, AdzanAlarmReceiver::class.java).apply {
            putExtra(AdzanAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun parseTimeToMillis(date: LocalDate, timeStr: String): Long {
        val parts = timeStr.split(":")
        val localTime = LocalTime.of(parts[0].toInt(), parts[1].toInt(), 0)
        return LocalDateTime.of(date, localTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}