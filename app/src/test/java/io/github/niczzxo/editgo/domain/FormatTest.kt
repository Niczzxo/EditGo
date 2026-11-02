package io.github.niczzxo.editgo.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun `formatDuration renders mm colon ss`() {
        assertEquals("00:00", formatDuration(0))
        assertEquals("00:45", formatDuration(45))
        assertEquals("01:15", formatDuration(75))
        assertEquals("10:00", formatDuration(600))
    }

    @Test
    fun `formatDuration treats negative as zero`() {
        assertEquals("00:00", formatDuration(-5))
    }

    @Test
    fun `formatFileSize uses B KB MB`() {
        assertEquals("512 B", formatFileSize(512))
        assertEquals("1.5 KB", formatFileSize(1536))
        assertEquals("2.0 MB", formatFileSize(2 * 1024 * 1024))
    }
}
