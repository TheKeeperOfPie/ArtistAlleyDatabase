package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface DisplaySettings {
    var showGridByDefault: Boolean
    var showRandomCatalogImage: Boolean
    var showOnlyConfirmedTags: Boolean
    var forceOneDisplayColumn: Boolean

    companion object {
        @Composable
        fun rememberForPreview() = object : DisplaySettings {
            override var showGridByDefault by mutableStateOf(false)
            override var showRandomCatalogImage by mutableStateOf(true)
            override var showOnlyConfirmedTags by mutableStateOf(false)
            override var forceOneDisplayColumn by mutableStateOf(false)
        }
    }
}
