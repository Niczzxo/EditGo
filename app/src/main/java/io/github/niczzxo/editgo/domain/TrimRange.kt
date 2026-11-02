package io.github.niczzxo.editgo.domain

/**
 * A trim selection inside a clip, in whole seconds.
 * Invariants (enforced by [clamp] / [validate], never by crashing):
 * 0 <= start < end <= duration.
 */
data class TrimRange(val startSec: Int, val endSec: Int) {
    val durationSec: Int get() = endSec - startSec

    companion object {
        fun full(durationSec: Int): TrimRange =
            TrimRange(0, durationSec.coerceAtLeast(0))

        /** Clamp a requested range into [0, duration], keeping start < end. */
        fun clamp(startSec: Int, endSec: Int, durationSec: Int): TrimRange {
            val duration = durationSec.coerceAtLeast(0)
            val start = startSec.coerceIn(0, duration)
            val end = endSec.coerceIn(0, duration)
            return if (end <= start) {
                // Degenerate request: fall back to the full clip.
                full(duration)
            } else {
                TrimRange(start, end)
            }
        }

        fun validate(range: TrimRange, durationSec: Int): Boolean =
            durationSec >= 0 &&
                range.startSec >= 0 &&
                range.endSec <= durationSec &&
                range.startSec < range.endSec
    }
}
