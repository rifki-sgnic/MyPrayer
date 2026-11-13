package com.rfksgn.myprayer.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassPointNameTest {
    @Test
    fun namesBearingsAcrossIndonesiaAndTheWraparound() {
        assertEquals("Barat-Barat Laut", compassPointName(295.1)) // Jakarta
        assertEquals("Barat-Barat Laut", compassPointName(292.5)) // Makassar-ish boundary
        assertEquals("Utara-Timur Laut", compassPointName(19.2))
        assertEquals("Utara", compassPointName(355.0))            // wraps back to north
        assertEquals("Utara", compassPointName(-5.0))             // negative input
        assertEquals("Selatan", compassPointName(180.0))
    }
}
