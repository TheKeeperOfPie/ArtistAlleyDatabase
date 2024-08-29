package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.files.Path

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
}
