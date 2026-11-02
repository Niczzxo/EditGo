package io.github.niczzxo.editgo.domain

/**
 * v1 filters. These are preview-only in v1: the editor shows a colour
 * tint for the selected filter; baking a filter into the exported file
 * is a v2 task (Media3 Transformer effects). The enum itself is the
 * contract — export must never claim a filter was applied when it was not.
 */
enum class VideoFilter(val label: String, val description: String) {
    NORMAL("Normal", "No change"),
    BRIGHT("Bright", "Lighter, lifted shadows"),
    WARM("Warm", "Warmer tones"),
    COOL("Cool", "Cooler tones"),
    BW("B&W", "Black and white");

    companion object {
        val all: List<VideoFilter> get() = entries.toList()
    }
}
