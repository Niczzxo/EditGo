package io.github.niczzxo.editgo.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectTest {

    @Test
    fun `blank project name becomes Untitled project`() {
        val project = Project.new("   ", null, 1_000L)
        assertEquals("Untitled project", project.name)
        assertEquals("project-1000", project.id)
    }

    @Test
    fun `project keeps its video uri`() {
        val project = Project.new("Reel 1", "content://video/1", 2_000L)
        assertEquals("content://video/1", project.videoUri)
    }
}
