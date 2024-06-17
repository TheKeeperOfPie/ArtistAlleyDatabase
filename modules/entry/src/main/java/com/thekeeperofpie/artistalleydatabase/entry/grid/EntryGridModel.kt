package com.thekeeperofpie.artistalleydatabase.entry.grid

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

interface EntryGridModel {
    val id: EntryId
    val imageUri: Uri?
    val placeholderText: String
    val imageWidth: Int?
    val imageHeight: Int?
    val imageWidthToHeightRatio: Float

    /**
     * Indicator icons to show to the user on top of the grid cell. "Error" isn't quite the right
     * term to use here, but "indicator" isn't, either.
     */
    @Composable
    fun ErrorIcons(modifier: Modifier) = Unit
}
