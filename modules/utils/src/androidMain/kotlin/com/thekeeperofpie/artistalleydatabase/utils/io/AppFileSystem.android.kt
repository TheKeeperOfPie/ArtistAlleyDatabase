package com.thekeeperofpie.artistalleydatabase.utils.io

import android.app.Application
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path

actual class AppFileSystem(val application: Application) {

    actual fun cachePath(path: String) = Path(application.cacheDir.resolve(path).path)
    actual fun filePath(path: String) = Path(application.filesDir.resolve(path).path)
    actual fun openUri(uri: Uri): Source? =
        application.contentResolver.openInputStream(uri.toAndroidUri())?.asSource()?.buffered()
}
