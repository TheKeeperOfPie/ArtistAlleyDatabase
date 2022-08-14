package com.thekeeperofpie.artistalleydatabase.form.grid

import java.io.File

interface EntryGridModel {
    val id: String
    val localImageFile: File?
    val placeholderText: String
    val imageWidth: Int?
    val imageWidthToHeightRatio: Float
}