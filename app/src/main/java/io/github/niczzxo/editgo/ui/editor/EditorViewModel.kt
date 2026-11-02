package io.github.niczzxo.editgo.ui.editor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import io.github.niczzxo.editgo.domain.ExportQuality
import io.github.niczzxo.editgo.domain.Project
import io.github.niczzxo.editgo.domain.TrimRange
import io.github.niczzxo.editgo.domain.VideoFilter
import io.github.niczzxo.editgo.export.VideoExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditorTool { NONE, TRIM, TEXT, FILTER, MUSIC, SPEED, STICKER, TRANSITION, EXPORT }

/** v1 transitions. Stored on the project; with a single clip there is no
 *  join to animate yet, so the choice is preview-only until v2 multi-clip. */
enum class ClipTransition(val label: String) {
    NONE("None"),
    FADE("Fade"),
    SLIDE("Slide"),
    ZOOM("Zoom"),
}

sealed interface ExportState {
    data object Idle : ExportState
    data object Running : ExportState
    data class Done(val fileName: String, val sizeBytes: Long) : ExportState
    data class Failed(val message: String) : ExportState
}

data class EditorUiState(
    val project: Project? = null,
    val videoUri: Uri? = null,
    val videoDurationSec: Int = 0,
    val trim: TrimRange = TrimRange.full(0),
    val activeTool: EditorTool = EditorTool.NONE,
    val overlayText: String = "",
    val overlayTextSizeSp: Float = 24f,
    val filter: VideoFilter = VideoFilter.NORMAL,
    val musicUri: Uri? = null,
    val musicName: String? = null,
    val speed: Float = 1f,
    val sticker: String? = null,
    val transition: ClipTransition = ClipTransition.NONE,
    val quality: ExportQuality = ExportQuality.P1080,
    val exportState: ExportState = ExportState.Idle,
    val error: String? = null,
)

/** Holds editor state. All updates are immutable copies (ECC rule). */
@UnstableApi
class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val exporter = VideoExporter(application.applicationContext)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(application.applicationContext).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
    }

    fun openProject(project: Project, videoUri: Uri?, durationSec: Int) {
        val duration = durationSec.coerceAtLeast(0)
        _uiState.update {
            it.copy(
                project = project,
                videoUri = videoUri,
                videoDurationSec = duration,
                trim = TrimRange.full(duration),
                activeTool = EditorTool.NONE,
                exportState = ExportState.Idle,
                error = null,
            )
        }
        videoUri?.let { uri ->
            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
            player.prepare()
            player.play()
        }
    }

    fun selectTool(tool: EditorTool) {
        _uiState.update { it.copy(activeTool = if (it.activeTool == tool) EditorTool.NONE else tool) }
    }

    fun setTrim(startSec: Int, endSec: Int) {
        _uiState.update { state ->
            state.copy(trim = TrimRange.clamp(startSec, endSec, state.videoDurationSec))
        }
    }

    fun setOverlayText(text: String) {
        // Cap overlay text: a caption that never ends is a broken caption.
        _uiState.update { it.copy(overlayText = text.take(120)) }
    }

    fun setOverlayTextSize(sizeSp: Float) {
        _uiState.update { it.copy(overlayTextSizeSp = sizeSp.coerceIn(12f, 72f)) }
    }

    fun setFilter(filter: VideoFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setMusic(uri: Uri?, name: String?) {
        _uiState.update { it.copy(musicUri = uri, musicName = name) }
    }

    fun setSpeed(speed: Float) {
        _uiState.update { it.copy(speed = speed.coerceIn(0.25f, 4f)) }
    }

    /** Text stickers are short labels (NEW / WOW / …) drawn over the video. */
    fun setSticker(sticker: String?) {
        _uiState.update { it.copy(sticker = sticker?.take(12)) }
    }

    fun setTransition(transition: ClipTransition) {
        _uiState.update { it.copy(transition = transition) }
    }

    fun setQuality(quality: ExportQuality) {
        _uiState.update { it.copy(quality = quality) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun export() {
        val state = _uiState.value
        val uri = state.videoUri
        if (uri == null) {
            _uiState.update { it.copy(error = "Pick a video first") }
            return
        }
        if (state.exportState is ExportState.Running) return
        _uiState.update { it.copy(exportState = ExportState.Running, error = null) }
        viewModelScope.launch {
            val output = VideoExporter.outputFile(
                getApplication(),
                state.project?.name ?: "editgo",
            )
            val result = exporter.exportTrim(uri, state.trim, state.quality, output)
            result.onSuccess { file ->
                _uiState.update {
                    it.copy(exportState = ExportState.Done(file.name, file.length()))
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(exportState = ExportState.Failed(error.message ?: "Export failed"))
                }
            }
        }
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
