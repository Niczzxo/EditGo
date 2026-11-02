package io.github.niczzxo.editgo.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import io.github.niczzxo.editgo.domain.VideoFilter
import io.github.niczzxo.editgo.domain.formatDuration

private data class ToolDef(val tool: EditorTool, val label: String)

// Same tool order as the reference workflow: Trim, Text, Filter, Music,
// Speed, Export — plus Sticker and Transition, which are cheap in v1.
private val TOOLS = listOf(
    ToolDef(EditorTool.TRIM, "Trim"),
    ToolDef(EditorTool.TEXT, "Text"),
    ToolDef(EditorTool.FILTER, "Filter"),
    ToolDef(EditorTool.MUSIC, "Music"),
    ToolDef(EditorTool.SPEED, "Speed"),
    ToolDef(EditorTool.STICKER, "Sticker"),
    ToolDef(EditorTool.TRANSITION, "Transition"),
    ToolDef(EditorTool.EXPORT, "Export"),
)

/**
 * Edits-style editor layout (original implementation, same workflow):
 * 9:16 preview on top, timeline + tool row pinned at the bottom,
 * one tool panel open at a time above the tool row.
 */
@Composable
fun EditorScreen(
    state: EditorUiState,
    player: Player,
    onBack: () -> Unit,
    onSelectTool: (EditorTool) -> Unit,
    onTrim: (Int, Int) -> Unit,
    onText: (String) -> Unit,
    onTextSize: (Float) -> Unit,
    onFilter: (VideoFilter) -> Unit,
    onMusic: (Uri?, String?) -> Unit,
    onSpeed: (Float) -> Unit,
    onSticker: (String?) -> Unit,
    onTransition: (ClipTransition) -> Unit,
    onQuality: (io.github.niczzxo.editgo.domain.ExportQuality) -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val musicPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        // v1 shows the file name only; the URI itself is kept for v2 mixing.
        onMusic(uri, uri?.lastPathSegment)
    }

    // Keep playback speed in sync with the Speed panel.
    LaunchedEffect(state.speed) {
        player.setPlaybackSpeed(state.speed)
    }

    val toolsEnabled = state.videoUri != null

    Column(modifier = modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = state.project?.name ?: "Editor",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        // --- 9:16 preview, top, takes the free space ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(9f / 16f)
                    .fillMaxHeight()
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                if (state.videoUri == null) {
                    Text(
                        "No video yet — pick one from Home",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                this.player = player
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Filter preview tint + overlays, on top of the video.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(filterTint(state.filter)),
                    )
                    if (state.overlayText.isNotBlank()) {
                        Text(
                            text = state.overlayText,
                            color = Color.White,
                            fontSize = state.overlayTextSizeSp.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 48.dp),
                        )
                    }
                    state.sticker?.let { sticker ->
                        Text(
                            text = sticker,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 24.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.45f),
                                    shape = MaterialTheme.shapes.small,
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }

        // --- Bottom: timeline, tool row, active panel ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (state.videoDurationSec > 0) {
                TimelineBar(state)
                Text(
                    "Trimmed: ${formatDuration(state.trim.durationSec)} " +
                        "of ${formatDuration(state.videoDurationSec)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (state.videoUri != null) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
            ) {
                TOOLS.forEach { def ->
                    FilterChip(
                        selected = state.activeTool == def.tool,
                        onClick = { onSelectTool(def.tool) },
                        label = { Text(def.label) },
                        enabled = toolsEnabled,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                when (state.activeTool) {
                    EditorTool.TRIM -> TrimPanel(state, onTrim)
                    EditorTool.TEXT -> TextPanel(state, onText, onTextSize)
                    EditorTool.FILTER -> FilterPanel(state, onFilter)
                    EditorTool.MUSIC -> MusicPanel(
                        state,
                        onPickMusic = { musicPicker.launch("audio/*") },
                        onClearMusic = { onMusic(null, null) },
                    )
                    EditorTool.SPEED -> SpeedPanel(state, onSpeed)
                    EditorTool.STICKER -> StickerPanel(state, onSticker)
                    EditorTool.TRANSITION -> TransitionPanel(state, onTransition)
                    EditorTool.EXPORT -> ExportPanel(state, onQuality, onExport)
                    EditorTool.NONE -> Unit
                }
            }
        }

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun TimelineBar(state: EditorUiState) {
    val duration = state.videoDurationSec.toFloat().coerceAtLeast(1f)
    val startFrac = (state.trim.startSec / duration).coerceIn(0f, 1f)
    val endFrac = (state.trim.endSec / duration).coerceIn(0f, 1f)
    val selectedFrac = (endFrac - startFrac).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
            ),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(startFrac.coerceAtLeast(0.001f)))
            Box(
                modifier = Modifier
                    .weight(selectedFrac.coerceAtLeast(0.001f))
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(vertical = 6.dp),
            )
            Spacer(modifier = Modifier.weight((1f - endFrac).coerceAtLeast(0.001f)))
        }
    }
}
