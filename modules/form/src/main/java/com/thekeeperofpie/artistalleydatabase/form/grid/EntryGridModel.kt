package com.thekeeperofpie.artistalleydatabase.form.grid

import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.form.EntryId

interface EntryGridModel {
    val id: EntryId
    val imageUri: Uri?
    val placeholderText: String
    val imageWidth: Int?
    val imageHeight: Int?
    val imageWidthToHeightRatio: Float
}