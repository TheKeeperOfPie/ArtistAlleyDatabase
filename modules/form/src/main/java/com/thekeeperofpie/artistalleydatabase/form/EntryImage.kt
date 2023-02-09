package com.thekeeperofpie.artistalleydatabase.form

import android.net.Uri
import androidx.annotation.StringRes

data class EntryImage(
    val entryId: EntryId?,
    val uri: Uri?,
    val width: Int,
    val height: Int,
    @StringRes val contentDescriptionRes: Int,
    val label: String = "",
    val croppedUri: Uri? = null,
    val croppedWidth: Int? = null,
    val croppedHeight: Int? = null,
) {
    val widthToHeightRatio = if (croppedWidth != null && croppedHeight != null) {
        croppedHeight / croppedWidth.toFloat()
    } else height / width.toFloat()
}