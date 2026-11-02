package io.github.niczzxo.editgo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrimRangeTest {

    @Test
    fun `full range covers the whole clip`() {
        assertEquals(TrimRange(0, 60), TrimRange.full(60))
        assertEquals(60, TrimRange.full(60).durationSec)
    }

    @Test
    fun `clamp keeps a valid range unchanged`() {
        assertEquals(TrimRange(5, 20), TrimRange.clamp(5, 20, 60))
    }

    @Test
    fun `clamp pulls out-of-bounds values inside the clip`() {
        assertEquals(TrimRange(0, 60), TrimRange.clamp(-5, 90, 60))
    }

    @Test
    fun `clamp falls back to full clip when end is not after start`() {
        assertEquals(TrimRange(0, 60), TrimRange.clamp(30, 30, 60))
        assertEquals(TrimRange(0, 60), TrimRange.clamp(40, 10, 60))
    }

    @Test
    fun `validate accepts a proper range and rejects bad ones`() {
        assertTrue(TrimRange.validate(TrimRange(0, 10), 60))
        assertFalse(TrimRange.validate(TrimRange(-1, 10), 60))
        assertFalse(TrimRange.validate(TrimRange(0, 61), 60))
        assertFalse(TrimRange.validate(TrimRange(10, 10), 60))
        assertFalse(TrimRange.validate(TrimRange(20, 10), 60))
    }
}
