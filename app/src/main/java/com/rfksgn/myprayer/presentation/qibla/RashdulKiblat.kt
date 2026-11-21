package com.rfksgn.myprayer.presentation.qibla

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

private const val KaabaLatitude = 21.4225
private const val KaabaLongitude = 39.8262

/** Rashdul Kiblat: the days the sun stands over the Ka'bah, as (month, day, minutes after 00:00 UTC). */
private val RashdulDays = listOf(
    Triple(5, 27, 9 * 60 + 18),
    Triple(5, 28, 9 * 60 + 18),
    Triple(7, 15, 9 * 60 + 27),
    Triple(7, 16, 9 * 60 + 27)
)

/** Shadows hold the Qibla line for a few minutes either side of the moment. */
private const val RashdulWindowMinutes = 5L

/** The next moment the sun stands over the Ka'bah, counting the current one until its window closes. */
internal fun nextRashdulKiblat(now: Instant): Instant {
    val year = LocalDateTime.ofInstant(now, ZoneOffset.UTC).year
    return (year..year + 1).asSequence()
        .flatMap { y -> RashdulDays.asSequence().map { (m, d, min) -> LocalDateTime.of(y, m, d, 0, 0).plusMinutes(min.toLong()) } }
        .map { it.toInstant(ZoneOffset.UTC) }
        .first { it.plus(RashdulWindowMinutes, ChronoUnit.MINUTES) > now }
}

/**
 * The sun's altitude at a place while it stands over the Ka'bah: 90° minus the arc to Mecca, ignoring
 * refraction. Below a few degrees (eastern Indonesia, around sunset there) no shadow can be read.
 */
internal fun sunAltitudeAtRashdul(latitude: Double, longitude: Double): Double {
    val la1 = Math.toRadians(latitude)
    val la2 = Math.toRadians(KaabaLatitude)
    val dLon = Math.toRadians(longitude - KaabaLongitude)
    val c = sin(la1) * sin(la2) + cos(la1) * cos(la2) * cos(dLon)
    return 90.0 - Math.toDegrees(acos(c.coerceIn(-1.0, 1.0)))
}

/** The sun must stand at least this high for a stick's shadow to be sharp and long enough to follow. */
internal const val RashdulMinSunAltitude = 5.0
