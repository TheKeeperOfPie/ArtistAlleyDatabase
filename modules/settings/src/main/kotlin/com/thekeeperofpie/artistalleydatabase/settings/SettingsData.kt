package com.thekeeperofpie.artistalleydatabase.settings

import android.net.Uri
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.serialization.Serializable

@Serializable
data class SettingsData(
    val artEntryTemplate: ArtEntry? = null,
    @Serializable(with = Converters.UriConverter::class)
    val cropDocumentUri: Uri? = null,
    val networkLoggingLevel: NetworkSettings.NetworkLoggingLevel = NetworkSettings.NetworkLoggingLevel.NONE,
    val enableNetworkCaching: Boolean = false,
    val searchQuery: ArtEntry? = null,
    val collapseAnimeFiltersOnClose: Boolean = true,
    val savedAnimeFilters: Map<String, FilterData> = emptyMap(),
    val showAdult: Boolean = false,
    val showIgnored: Boolean = false,
    val ignoredAniListMediaIds: Set<Int> = emptySet(),
    val showLessImportantTags: Boolean = false,
    val showSpoilerTags: Boolean = false,
    val animeNewsNetworkRegion: AnimeNewsNetworkRegion = AnimeNewsNetworkRegion.USA_CANADA,
    val animeNewsNetworkCategoriesIncluded: List<AnimeNewsNetworkCategory> = emptyList(),
    val animeNewsNetworkCategoriesExcluded: List<AnimeNewsNetworkCategory> = emptyList(),
    val crunchyrollNewsCategoriesIncluded: List<CrunchyrollNewsCategory> = emptyList(),
    val crunchyrollNewsCategoriesExcluded: List<CrunchyrollNewsCategory> = emptyList(),
    val navDrawerStartDestination: String? = null,
    val hideStatusBar: Boolean = false,
    val adsEnabled: Boolean = false,
    val subscribed: Boolean = false,
    val appTheme: AppThemeSetting = AppThemeSetting.AUTO,
    val preferredMediaType: MediaType = MediaType.ANIME,
    val languageOptionMedia: AniListLanguageOption = AniListLanguageOption.DEFAULT,
    val languageOptionCharacters: AniListLanguageOption = AniListLanguageOption.DEFAULT,
    val languageOptionStaff: AniListLanguageOption = AniListLanguageOption.DEFAULT,
)
