package com.thekeeperofpie.artistalleydatabase.entry

import com.eygraber.uri.Uri
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class EntryImage(
    // TODO: Remove this; the entry ID should not be held by the image
    val entryId: EntryId?,
    val imageId: String = Uuid.random().toString(),
    val uri: Uri?,
    internal val width: Int,
    internal val height: Int,
    val label: String = "",
    val croppedUri: Uri? = null,
    internal val croppedWidth: Int? = null,
    internal val croppedHeight: Int? = null,
) {
    val finalWidth get() = croppedWidth ?: width
    val finalHeight get() = croppedHeight ?: height
    val widthToHeightRatio = finalHeight / finalWidth.toFloat()

    val finalUri get() = croppedUri ?: uri
}
