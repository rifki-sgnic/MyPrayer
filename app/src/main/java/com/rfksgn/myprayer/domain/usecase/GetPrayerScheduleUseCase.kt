package com.rfksgn.myprayer.domain.usecase

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.Prayer
import com.batoulapps.adhan2.PrayerAdjustments
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.Qibla
import com.batoulapps.adhan2.data.DateComponents
import com.rfksgn.myprayer.domain.model.PrayerSchedule
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GetPrayerScheduleUseCase {
    operator fun invoke(
        latitude: Double,
        longitude: Double,
        date: LocalDate = LocalDate.now()
    ): PrayerSchedule {
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)

        val params = CalculationMethod.OTHER.parameters.copy(
            fajrAngle = 20.0,
            ishaAngle = 18.0,
            madhab = Madhab.SHAFI,
            prayerAdjustments = PrayerAdjustments(
                fajr = 2,
                sunrise = -2,
                dhuhr = 2,
                asr = 2,
                maghrib = 2,
                isha = 2
            )
        )

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        val qibla = Qibla(coordinates)

        val now = Clock.System.now()
        val nextPrayer = prayerTimes.nextPrayer(now)

        val (nextName, nextInstant) = if (nextPrayer != Prayer.NONE) {
            mapPrayerName(nextPrayer) to prayerTimes.timeForPrayer(nextPrayer)!!
        } else {
            // Jika sudah lewat Isya, ambil waktu Subuh besok
            val tomorrow = date.plusDays(1)
            val tomorrowComponents = DateComponents(tomorrow.year, tomorrow.monthValue, tomorrow.dayOfMonth)
            val tomorrowPrayers = PrayerTimes(coordinates, tomorrowComponents, params)
            "Subuh" to tomorrowPrayers.fajr
        }

        val dateFormatters = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))

        return PrayerSchedule(
            dateFormatted = date.format(dateFormatters),
            fajr = formatTime(prayerTimes.fajr),
            sunrise = formatTime(prayerTimes.sunrise),
            dhuhr = formatTime(prayerTimes.dhuhr),
            asr = formatTime(prayerTimes.asr),
            maghrib = formatTime(prayerTimes.maghrib),
            isha = formatTime(prayerTimes.isha),
            qiblaDirection = qibla.direction,
            nextPrayerName = nextName,
            nextPrayerTimeMillis = nextInstant.toEpochMilliseconds()
        )
    }

    private fun mapPrayerName(prayer: Prayer): String = when (prayer) {
        Prayer.FAJR -> "Subuh"
        Prayer.SUNRISE -> "Terbit"
        Prayer.DHUHR -> "Dzuhur"
        Prayer.ASR -> "Ashar"
        Prayer.MAGHRIB -> "Maghrib"
        Prayer.ISHA -> "Isya"
        Prayer.NONE -> "-"
    }

    @OptIn(ExperimentalTime::class)
    private fun formatTime(instant: Instant): String {
        val localTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "%02d:%02d".format(localTime.hour, localTime.minute)
    }
}