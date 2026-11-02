package io.github.niczzxo.editgo.domain

/** Format seconds as mm:ss, e.g. 75 -> "01:15". Negative input is treated as 0. */
fun formatDuration(totalSeconds: Int): String {
    val seconds = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}

/** Human file size, e.g. 1536 -> "1.5 KB". Used for the export summary. */
fun formatFileSize(bytes: Long): String {
    val size = bytes.coerceAtLeast(0)
    return when {
        size < 1_024 -> "$size B"
        size < 1_024 * 1_024 -> "%.1f KB".format(size / 1_024.0)
        else -> "%.1f MB".format(size / 1_024.0 / 1_024.0)
    }
}
