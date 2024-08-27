package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import kotlinx.io.Source
import kotlinx.io.files.Path

actual class AppFileSystem {
    actual fun cachePath(path: String): Path {
        TODO("Not yet implemented")
    }

    actual fun filePath(path: String): Path {
        TODO("Not yet implemented")
    }

    actual fun openUri(uri: Uri): Source? {
        TODO("Not yet implemented")
    }

    actual fun getImageWidthHeight(
        uri: Uri,
    ): Pair<Int?, Int?> {
        TODO("Not yet implemented")
    }

    actual fun getImageType(
        path: Path,
    ): String? {
        TODO("Not yet implemented")
    }

    actual fun writeEntryImage(
        outputPath: Path,
        imageUri: Uri?,
    ): Result<*>? {
        TODO("Not yet implemented")
    }
}
