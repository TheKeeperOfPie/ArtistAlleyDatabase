package com.thekeeperofpie.artistalleydatabase.utils.io

import com.eygraber.uri.Uri
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator

fun Path.toUri() = Uri.Builder()
    .scheme("file")
    .authority("")
    .path(toString())
    .build()

fun Path.resolve(vararg children: String) = Path(
    this.toString() + SystemPathSeparator
            + children.joinToString(separator = "$SystemPathSeparator")
)

fun FileSystem.walk(path: Path) = sequence<Path> {
    recursiveWalk(path)
}

private suspend fun SequenceScope<Path>.recursiveWalk(path: Path) {
    val metadata = SystemFileSystem.metadataOrNull(path)
    if (metadata?.isDirectory == true) {
        SystemFileSystem.list(path).forEach {
            recursiveWalk(it)
        }
    }
    yield(path)
}

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
