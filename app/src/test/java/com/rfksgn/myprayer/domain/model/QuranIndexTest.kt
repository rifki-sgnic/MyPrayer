package com.rfksgn.myprayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class QuranIndexTest {
    @Test
    fun globalIndexRoundTrips() {
        assertEquals(1, QuranIndex.globalIndex(1, 1))
        assertEquals(QuranIndex.TOTAL, QuranIndex.surahEnd(114))
        assertEquals(3 to 190, QuranIndex.fromGlobal(QuranIndex.globalIndex(3, 190)))
        assertEquals(4 to 1, QuranIndex.fromGlobal(QuranIndex.surahEnd(3) + 1))
    }

    @Test
    fun juzBoundaries() {
        assertEquals(1, QuranIndex.juzOf(2, 141))
        assertEquals(2, QuranIndex.juzOf(2, 142))
        assertEquals(4, QuranIndex.juzOf(4, 23))
        assertEquals(5, QuranIndex.juzOf(4, 24))
        assertEquals(30, QuranIndex.juzOf(114, 6))
        assertEquals(QuranIndex.globalIndex(4, 23), QuranIndex.juzEndGlobal(4))
        assertEquals(QuranIndex.TOTAL, QuranIndex.juzEndGlobal(30))
    }
}
