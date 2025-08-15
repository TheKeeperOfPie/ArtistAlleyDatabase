package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import kotlin.time.Instant

expect class AppFileSystem {

    // Vararg these to support SystemPathSeparator
    fun cachePath(path: String): Path
    fun filePath(path: String): Path
    fun openUriSource(uri: Uri): Source?
    fun openUriSink(uri: Uri, mode: String = "wt"): Sink?

    fun getImageWidthHeight(uri: Uri): Pair<Int?, Int?>
    fun getImageType(path: Path): String?
    fun writeEntryImage(
        outputPath: Path,
        imageUri: Uri?,
    ): Result<*>?

    fun openEncryptedSource(path: Path): Source
    fun openEncryptedSink(path: Path): Sink

    fun exists(path: Path): Boolean
    fun createDirectories(path: Path)
    fun delete(path: Path)
    fun metadataOrNull(path: Path): FileMetadata?
    fun sink(path: Path): RawSink
    fun list(path: Path): Collection<Path>
    fun atomicMove(source: Path, destination: Path)
    fun source(path: Path): RawSource
    fun lastModifiedTime(path: Path): Instant
}
