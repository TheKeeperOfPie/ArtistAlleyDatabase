package com.thekeeperofpie.artistalleydatabase.settings

import android.net.Uri
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.serialization.Serializable

@Serializable
data class SettingsData(
    val artEntryTemplate: ArtEntry? = null,
    @Serializable(with = Converters.UriConverter::class)
    val cropDocumentUri: Uri? = null,
    val networkLoggingLevel: NetworkSettings.NetworkLoggingLevel = NetworkSettings.NetworkLoggingLevel.NONE,
    val searchQuery: ArtEntry? = null,
    val collapseAnimeFiltersOnClose: Boolean = true,
    val savedAnimeFilters: Map<String, FilterData> = emptyMap(),
    val showAdult: Boolean = false,
    val showIgnored: Boolean = false,
    val ignoredAniListMediaIds: Set<Int> = emptySet(),
    val navDrawerStartDestination: String? = null,
)
