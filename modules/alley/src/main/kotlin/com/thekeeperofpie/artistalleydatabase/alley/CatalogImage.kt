package com.thekeeperofpie.artistalleydatabase.alley

import android.net.Uri

data class CatalogImage(
    val uri: Uri,
    val width: Int?,
    val height: Int?,
)