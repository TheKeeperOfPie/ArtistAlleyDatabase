package com.thekeeperofpie.artistalleydatabase.settings

import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.android_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry

data class SettingsData(
    val artEntryTemplate: ArtEntry?,
    val cropDocumentUri: Uri?,
    val networkLoggingLevel: NetworkSettings.NetworkLoggingLevel,
    val searchQuery: ArtEntry?,
)