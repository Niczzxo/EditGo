package io.github.niczzxo.editgo.domain

/** A local edit project. v1 stores projects in memory only (see README). */
data class Project(
    val id: String,
    val name: String,
    val videoUri: String?,
    val createdAtMs: Long,
) {
    companion object {
        fun new(name: String, videoUri: String?, nowMs: Long): Project =
            Project(
                id = "project-$nowMs",
                name = name.trim().ifBlank { "Untitled project" },
                videoUri = videoUri,
                createdAtMs = nowMs,
            )
    }
}
