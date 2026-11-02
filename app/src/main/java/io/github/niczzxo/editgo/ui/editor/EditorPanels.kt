package io.github.niczzxo.editgo.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.niczzxo.editgo.domain.ExportQuality
import io.github.niczzxo.editgo.domain.VideoFilter
import io.github.niczzxo.editgo.domain.formatDuration
import io.github.niczzxo.editgo.domain.formatFileSize

/** Preview-only tint for the selected filter (v1 does not bake filters). */
fun filterTint(filter: VideoFilter): Color = when (filter) {
    VideoFilter.NORMAL -> Color.Transparent
    VideoFilter.BRIGHT -> Color.White.copy(alpha = 0.08f)
    VideoFilter.WARM -> Color(0xFFFFB74D).copy(alpha = 0.12f)
    VideoFilter.COOL -> Color(0xFF64B5F6).copy(alpha = 0.12f)
    VideoFilter.BW -> Color.Black.copy(alpha = 0.25f)
}

@Composable
fun TrimPanel(state: EditorUiState, onTrim: (Int, Int) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Trim", style = MaterialTheme.typography.titleMedium)
        Text(
            "${formatDuration(state.trim.startSec)} – ${formatDuration(state.trim.endSec)}" +
                "  (clip ${formatDuration(state.videoDurationSec)})",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Two sliders: start and end. Kept separate (not a range slider)
        // so each thumb has a clear label and 48dp touch target.
        Text("Start: ${formatDuration(state.trim.startSec)}")
        Slider(
            value = state.trim.startSec.toFloat(),
            onValueChange = { onTrim(it.toInt(), state.trim.endSec) },
            valueRange = 0f..state.videoDurationSec.toFloat().coerceAtLeast(1f),
        )
        Text("End: ${formatDuration(state.trim.endSec)}")
        Slider(
            value = state.trim.endSec.toFloat(),
            onValueChange = { onTrim(state.trim.startSec, it.toInt()) },
            valueRange = 0f..state.videoDurationSec.toFloat().coerceAtLeast(1f),
        )
    }
}

@Composable
fun TextPanel(state: EditorUiState, onText: (String) -> Unit, onSize: (Float) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Text overlay", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.overlayText,
            onValueChange = onText,
            label = { Text("Caption text") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Size: ${state.overlayTextSizeSp.toInt()}sp")
        Slider(
            value = state.overlayTextSizeSp,
            onValueChange = onSize,
            valueRange = 12f..72f,
        )
    }
}

@Composable
fun FilterPanel(state: EditorUiState, onFilter: (VideoFilter) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Filter", style = MaterialTheme.typography.titleMedium)
        Text(
            "Preview only in v1 — export keeps the original colours.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VideoFilter.all.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { onFilter(filter) },
                    label = { Text(filter.label) },
                )
            }
        }
    }
}

@Composable
fun MusicPanel(state: EditorUiState, onPickMusic: () -> Unit, onClearMusic: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Music", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(state.musicName ?: "No audio selected")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPickMusic) { Text("Pick audio") }
            if (state.musicUri != null) {
                TextButton(onClick = onClearMusic) { Text("Remove") }
            }
        }
        Text(
            "Music is preview-only in v1 — export is video-only for now.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SpeedPanel(state: EditorUiState, onSpeed: (Float) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Speed", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0.5f, 1f, 1.5f, 2f).forEach { speed ->
                FilterChip(
                    selected = state.speed == speed,
                    onClick = { onSpeed(speed) },
                    label = { Text("${speed}x") },
                )
            }
        }
    }
}

/** Built-in text stickers. Original labels only — no copied sticker packs. */
val STICKER_OPTIONS = listOf("NEW", "WOW", "LOVE", "SALE", "GO!")

@Composable
fun StickerPanel(state: EditorUiState, onSticker: (String?) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Sticker", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            STICKER_OPTIONS.forEach { sticker ->
                FilterChip(
                    selected = state.sticker == sticker,
                    onClick = { onSticker(if (state.sticker == sticker) null else sticker) },
                    label = { Text(sticker) },
                )
            }
        }
        Text(
            "Tap a sticker to place it on the video. Tap again to remove it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun TransitionPanel(state: EditorUiState, onTransition: (ClipTransition) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Transition", style = MaterialTheme.typography.titleMedium)
        Text(
            "Saved for clip joins. With one clip there is nothing to join " +
                "yet — multi-clip editing is v2.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClipTransition.entries.forEach { transition ->
                FilterChip(
                    selected = state.transition == transition,
                    onClick = { onTransition(transition) },
                    label = { Text(transition.label) },
                )
            }
        }
    }
}

@Composable
fun ExportPanel(
    state: EditorUiState,
    onQuality: (ExportQuality) -> Unit,
    onExport: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Export", style = MaterialTheme.typography.titleMedium)
        ExportQuality.entries.forEach { quality ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = state.quality == quality,
                        onClick = { onQuality(quality) },
                    ),
            ) {
                RadioButton(
                    selected = state.quality == quality,
                    onClick = { onQuality(quality) },
                )
                Text(quality.label)
            }
        }
        Text(
            "v1 export trims the video to your selection. " +
                "Resolution choice is saved for v2 transcoding.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        when (val export = state.exportState) {
            is ExportState.Idle -> Button(
                onClick = onExport,
                enabled = state.videoUri != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Export video") }
            is ExportState.Running -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Text("Exporting…", modifier = Modifier.padding(start = 12.dp))
            }
            is ExportState.Done -> {
                Text("Saved: ${export.fileName} (${formatFileSize(export.sizeBytes)})")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                    Text("Export again")
                }
            }
            is ExportState.Failed -> {
                Text(export.message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                    Text("Retry export")
                }
            }
        }
        if (state.videoUri == null) {
            Text(
                "Pick a video first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
