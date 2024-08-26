package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import kotlinx.io.Source
import kotlinx.io.files.Path

expect class AppFileSystem {

    fun cachePath(path: String): Path
    fun filePath(path: String): Path

    fun openUri(uri: Uri): Source?
}
