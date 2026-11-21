package com.rfksgn.myprayer.presentation.qibla

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RashdulKiblatTest {
    @Test
    fun findsTheNextSunOverTheKaaba() {
        assertEquals(Instant.parse("2027-05-27T09:18:00Z"), nextRashdulKiblat(Instant.parse("2026-11-19T06:00:00Z")))
        // Still the current one inside its five-minute window, the next day once it closes
        assertEquals(Instant.parse("2027-05-27T09:18:00Z"), nextRashdulKiblat(Instant.parse("2027-05-27T09:21:00Z")))
        assertEquals(Instant.parse("2027-05-28T09:18:00Z"), nextRashdulKiblat(Instant.parse("2027-05-27T09:24:00Z")))
        assertEquals(Instant.parse("2027-07-15T09:27:00Z"), nextRashdulKiblat(Instant.parse("2027-06-01T00:00:00Z")))
        // After July it rolls over to next May
        assertEquals(Instant.parse("2028-05-27T09:18:00Z"), nextRashdulKiblat(Instant.parse("2027-07-16T10:00:00Z")))
    }

    @Test
    fun sunIsHighEnoughInTheWestButSetInPapua() {
        assertEquals(90.0, sunAltitudeAtRashdul(21.4225, 39.8262), 0.001)
        val jakarta = sunAltitudeAtRashdul(-6.1754, 106.8272)
        assertTrue("Jakarta $jakarta", jakarta in 15.0..25.0)
        val makassar = sunAltitudeAtRashdul(-5.1477, 119.4327)
        assertTrue("Makassar $makassar", makassar in RashdulMinSunAltitude..15.0)
        val jayapura = sunAltitudeAtRashdul(-2.5337, 140.7181)
        assertTrue("Jayapura $jayapura", jayapura < RashdulMinSunAltitude)
    }
}
