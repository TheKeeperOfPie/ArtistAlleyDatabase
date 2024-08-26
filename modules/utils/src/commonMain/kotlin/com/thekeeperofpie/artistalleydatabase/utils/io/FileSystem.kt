package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

fun Path.toUri() = Uri.Builder()
    .scheme("file")
    .authority("")
    .path(toString())
    .build()

fun FileSystem.deleteRecursively(path: Path) {
    val metadata = metadataOrNull(path)
    if (metadata?.isDirectory == true) {
        list(path)
            .forEach { deleteRecursively(it) }
        delete(path, mustExist = false)
    } else {
        delete(path, mustExist = false)
    }
}
