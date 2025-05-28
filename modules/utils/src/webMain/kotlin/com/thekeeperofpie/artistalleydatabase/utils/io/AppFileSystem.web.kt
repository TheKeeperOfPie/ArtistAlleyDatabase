package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.datetime.Instant
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import me.tatarka.inject.annotations.Inject

// TODO
@SingletonScope
@Inject
actual class AppFileSystem {

    actual fun cachePath(path: String) = Path("/cache/$path")

    actual fun filePath(path: String) = Path("/files/$path")

    actual fun openUriSource(uri: Uri): Source? =
        SystemFileSystem.source(Path(uri.pathOrThrow)).buffered()

    // TODO
    actual fun openUriSink(uri: Uri, mode: String): Sink? =
        SystemFileSystem.sink(Path(uri.pathOrThrow)).buffered()

    // TODO
    actual fun getImageWidthHeight(uri: Uri): Pair<Int?, Int?> = null to null

    // TODO
    actual fun getImageType(path: Path): String? = null

    actual fun writeEntryImage(outputPath: Path, imageUri: Uri?): Result<*>? {
        imageUri ?: return null
        try {
            openUriSource(imageUri).use { input ->
                input
                    ?: return Result.failure<Unit>(IllegalArgumentException("Could not open image"))
                SystemFileSystem.sink(outputPath).buffered().use { output ->
                    input.transferTo(output)
                }
            }
        } catch (t: Throwable) {
            return Result.failure<Unit>(t)
        }
        return Result.success(Unit)
    }

    actual fun openEncryptedSource(path: Path) = SystemFileSystem.source(path).buffered()

    actual fun openEncryptedSink(path: Path) = SystemFileSystem.sink(path).buffered()

    actual fun exists(path: Path) = SystemFileSystem.exists(path)

    actual fun createDirectories(path: Path) =
        SystemFileSystem.createDirectories(path, mustCreate = false)

    actual fun delete(path: Path) = SystemFileSystem.delete(path, mustExist = false)

    actual fun metadataOrNull(path: Path) = SystemFileSystem.metadataOrNull(path)

    actual fun sink(path: Path) = SystemFileSystem.sink(path)

    actual fun list(path: Path) = SystemFileSystem.list(path)

    actual fun atomicMove(source: Path, destination: Path) =
        SystemFileSystem.atomicMove(source, destination)

    actual fun source(path: Path) = SystemFileSystem.source(path)

    private val Uri.pathOrThrow: String
        get() {
            if (scheme != "file" || path == null) {
                throw IllegalArgumentException("Platform only supports file:// URIs, got $this")
            }
            return path!!
        }

    // TODO: Doesn't handle multiplatform
    actual fun lastModifiedTime(path: Path) = Instant.DISTANT_PAST
}
