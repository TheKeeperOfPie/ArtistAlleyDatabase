package com.thekeeperofpie.artistalleydatabase.settings

import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.serialization.Serializable

@Serializable
data class SettingsData(
    val artEntryTemplate: ArtEntry?,
    @Serializable(with = Converters.UriConverter::class)
    val cropDocumentUri: Uri?,
    val networkLoggingLevel: NetworkSettings.NetworkLoggingLevel,
    val searchQuery: ArtEntry?,
    val collapseAnimeFiltersOnClose: Boolean,
    val savedAnimeFilters: Map<String, FilterData> = emptyMap(),
    val showAdult: Boolean,
)
