package io.github.niczzxo.editgo.domain

/** Export resolution. Height in px; width follows the source aspect ratio. */
enum class ExportQuality(val label: String, val heightPx: Int) {
    P720("720p", 720),
    P1080("1080p", 1080);

    companion object {
        fun fromLabel(label: String): ExportQuality =
            entries.firstOrNull { it.label == label } ?: P1080
    }
}
