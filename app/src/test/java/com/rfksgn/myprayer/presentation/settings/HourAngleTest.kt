package com.rfksgn.myprayer.presentation.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class HourAngleTest {
    @Test
    fun noonIsTopMidnightIsBottomClockwise() {
        assertEquals(0f, hourAngle("12:00"), 0.001f)
        assertEquals(90f, hourAngle("18:00"), 0.001f)
        assertEquals(180f, hourAngle("00:00"), 0.001f)
        assertEquals(270f, hourAngle("06:00"), 0.001f)
        assertEquals(108f, hourAngle("19:12"), 0.001f)
    }

    @Test
    fun malformedTimeFallsBackToMidnight() {
        assertEquals(180f, hourAngle("--:--"), 0.001f)
    }
}
