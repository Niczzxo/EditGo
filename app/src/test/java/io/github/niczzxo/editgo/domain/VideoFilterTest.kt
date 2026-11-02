package io.github.niczzxo.editgo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoFilterTest {

    @Test
    fun `all five v1 filters exist in a fixed order`() {
        assertEquals(
            listOf("Normal", "Bright", "Warm", "Cool", "B&W"),
            VideoFilter.all.map { it.label },
        )
        assertTrue(VideoFilter.all.contains(VideoFilter.NORMAL))
    }

    @Test
    fun `export quality defaults to 1080p for an unknown label`() {
        assertEquals(ExportQuality.P1080, ExportQuality.fromLabel("4K"))
        assertEquals(ExportQuality.P720, ExportQuality.fromLabel("720p"))
    }
}
