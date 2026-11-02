package io.github.niczzxo.editgo

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import io.github.niczzxo.editgo.data.InMemoryProjectRepository
import io.github.niczzxo.editgo.domain.Project
import io.github.niczzxo.editgo.ui.editor.EditorScreen
import io.github.niczzxo.editgo.ui.editor.EditorViewModel
import io.github.niczzxo.editgo.ui.home.HomeScreen
import io.github.niczzxo.editgo.ui.theme.EditGoTheme

private enum class AppScreen { HOME, EDITOR }

@UnstableApi
class MainActivity : ComponentActivity() {

    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EditGoTheme {
                EditGoApp(editorViewModel)
            }
        }
    }
}

@UnstableApi
@Composable
private fun MainActivity.EditGoApp(editorViewModel: EditorViewModel) {
    val repository = remember { InMemoryProjectRepository() }
    val projects = remember { mutableStateListOf<Project>() }
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    var hasMediaPermission by remember {
        mutableStateOf(hasMediaPermission())
    }
    val editorState by editorViewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        hasMediaPermission = results.values.any { it }
    }

    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val project = Project.new(
            name = "Project ${projects.size + 1}",
            videoUri = uri.toString(),
            nowMs = System.currentTimeMillis(),
        )
        repository.add(project)
        projects.clear()
        projects.addAll(repository.list())
        editorViewModel.openProject(project, uri, videoDurationSec(uri))
        screen = AppScreen.EDITOR
    }

    when (screen) {
        AppScreen.HOME -> HomeScreen(
            projects = projects,
            hasMediaPermission = hasMediaPermission,
            onGrantPermission = {
                permissionLauncher.launch(mediaPermissions())
            },
            onNewProject = { videoPicker.launch("video/*") },
            onOpenProject = { project ->
                val uri = project.videoUri?.let(Uri::parse)
                editorViewModel.openProject(project, uri, uri?.let { videoDurationSec(it) } ?: 0)
                screen = AppScreen.EDITOR
            },
            modifier = Modifier.fillMaxSize(),
        )

        AppScreen.EDITOR -> EditorScreen(
            state = editorState,
            player = editorViewModel.player,
            onBack = { screen = AppScreen.HOME },
            onSelectTool = editorViewModel::selectTool,
            onTrim = editorViewModel::setTrim,
            onText = editorViewModel::setOverlayText,
            onTextSize = editorViewModel::setOverlayTextSize,
            onFilter = editorViewModel::setFilter,
            onMusic = editorViewModel::setMusic,
            onSpeed = editorViewModel::setSpeed,
            onSticker = editorViewModel::setSticker,
            onTransition = editorViewModel::setTransition,
            onQuality = editorViewModel::setQuality,
            onExport = editorViewModel::export,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun MainActivity.hasMediaPermission(): Boolean =
    mediaPermissions().any { permission ->
        ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

private fun mediaPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

/** Video length in whole seconds, 0 when it cannot be read. */
private fun MainActivity.videoDurationSec(uri: Uri): Int {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(this, uri)
        val ms = retriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION,
        )?.toLongOrNull() ?: 0L
        (ms / 1_000).toInt().coerceAtLeast(0)
    } catch (_: Exception) {
        0
    } finally {
        try { retriever.release() } catch (_: Exception) { /* best effort */ }
    }
}
