package com.thekeeperofpie.artistalleydatabase.entry

import android.net.Uri
import androidx.annotation.StringRes

data class EntryImage(
    val entryId: EntryId?,
    val uri: Uri?,
    internal val width: Int,
    internal val height: Int,
    @StringRes val contentDescriptionRes: Int,
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
