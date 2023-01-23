package com.thekeeperofpie.artistalleydatabase.form.grid

import android.net.Uri

interface EntryGridModel {
    val id: String
    val imageUri: Uri?
    val placeholderText: String
    val imageWidth: Int?
    val imageWidthToHeightRatio: Float
}