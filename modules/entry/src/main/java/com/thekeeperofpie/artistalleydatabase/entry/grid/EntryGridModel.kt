package com.thekeeperofpie.artistalleydatabase.entry.grid

import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

interface EntryGridModel {
    val id: EntryId
    val imageUri: Uri?
    val placeholderText: String
    val imageWidth: Int?
    val imageHeight: Int?
    val imageWidthToHeightRatio: Float
}