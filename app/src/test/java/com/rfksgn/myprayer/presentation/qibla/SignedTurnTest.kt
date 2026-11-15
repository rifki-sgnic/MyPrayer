package com.rfksgn.myprayer.presentation.qibla

import org.junit.Assert.assertEquals
import org.junit.Test

class SignedTurnTest {
    @Test
    fun takesTheShortWayRoundAndSignsTheDirection() {
        assertEquals(47.1f, signedTurn(248f, 295.1f), 0.001f)   // turn right
        assertEquals(-8.9f, signedTurn(304f, 295.1f), 0.001f)   // turn left
        assertEquals(20f, signedTurn(350f, 10f), 0.001f)         // across north, clockwise
        assertEquals(-20f, signedTurn(10f, 350f), 0.001f)        // across north, anticlockwise
        assertEquals(0f, signedTurn(720f, 0f), 0.001f)           // unwrapped input
    }
}
