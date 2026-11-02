package io.github.niczzxo.editgo.export

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import io.github.niczzxo.editgo.domain.ExportQuality
import io.github.niczzxo.editgo.domain.TrimRange
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Real v1 export: trims the input video with Media3 Transformer.
 *
 * What it does: cuts [trim] out of [inputUri] and writes an .mp4 to
 * [outputFile]. What it does NOT do in v1: bake filters, text overlays
 * or music into the file — those are preview-only / v2. The caller must
 * never report success unless this returns [Result.success].
 */
@UnstableApi
class VideoExporter(private val context: Context) {

    suspend fun exportTrim(
        inputUri: Uri,
        trim: TrimRange,
        quality: ExportQuality,
        outputFile: File,
    ): Result<File> {
        require(trim.endSec > trim.startSec) { "Trim end must be after start" }
        outputFile.parentFile?.mkdirs()
        if (outputFile.exists()) outputFile.delete()

        return suspendCancellableCoroutine { continuation ->
            // Clipping goes on the MediaItem, not on EditedMediaItem:
            // EditedMediaItem.Builder has no setClippingConfiguration
            // method (that was the run #13 compile error). The official
            // Media3 trim pattern sets it on MediaItem.Builder.
            val clipping = MediaItem.ClippingConfiguration
                .Builder()
                .setStartPositionMs(trim.startSec * 1_000L)
                .setEndPositionMs(trim.endSec * 1_000L)
                .build()
            val mediaItem = MediaItem.Builder()
                .setUri(inputUri)
                .setClippingConfiguration(clipping)
                .build()
            val editedItem = EditedMediaItem.Builder(mediaItem).build()
            // NOTE: quality.heightPx is recorded on the export for v2
            // (Transformer video transcoding to an exact height). In v1
            // the trim keeps the source resolution; forcing a resize here
            // would silently re-encode at the wrong aspect on some clips,
            // so we do not pretend 720p/1080p changes the pixels yet.
            @Suppress("UNUSED_VARIABLE")
            val requestedHeightPx = quality.heightPx

            val transformer = Transformer.Builder(context).build()
            continuation.invokeOnCancellation { transformer.cancel() }
            transformer.addListener(
                object : Transformer.Listener {
                    override fun onCompleted(
                        composition: Composition,
                        result: ExportResult,
                    ) {
                        if (continuation.isActive) {
                            continuation.resume(Result.success(outputFile))
                        }
                    }

                    override fun onError(
                        composition: Composition,
                        result: ExportResult,
                        exception: ExportException,
                    ) {
                        if (continuation.isActive) {
                            continuation.resume(
                                Result.failure(
                                    IllegalStateException(
                                        "Export failed: ${exception.message}",
                                        exception,
                                    ),
                                ),
                            )
                        }
                    }
                },
            )
            transformer.start(editedItem, outputFile.absolutePath)
        }
    }

    companion object {
        fun outputFile(context: Context, projectName: String): File {
            val safe = projectName.filter { it.isLetterOrDigit() }.ifBlank { "editgo" }
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            return File(dir, "$safe-${System.currentTimeMillis()}.mp4")
        }
    }
}
