package com.thekeeperofpie.artistalleydatabase.settings

import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeRootNavDestination
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.cr.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import kotlinx.serialization.Serializable

@Serializable
data class SettingsData(
    val artEntryTemplate: ArtEntry? = null,
    val cropImageUri: String? = null,
    val networkLoggingLevel: NetworkSettings.NetworkLoggingLevel = NetworkSettings.NetworkLoggingLevel.NONE,
    val enableNetworkCaching: Boolean = false,
    val searchQuery: ArtEntry? = null,
    val collapseAnimeFiltersOnClose: Boolean = true,
    val savedAnimeFilters: Map<String, FilterData> = emptyMap(),
    val showAdult: Boolean = false,
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
    val mediaViewOption: MediaViewOption = MediaViewOption.SMALL_CARD,
    val rootNavDestination: AnimeRootNavDestination = AnimeRootNavDestination.HOME,
    val languageOptionMedia: AniListLanguageOption = AniListLanguageOption.DEFAULT,
    val languageOptionCharacters: AniListLanguageOption = AniListLanguageOption.DEFAULT,
    val languageOptionStaff: AniListLanguageOption = AniListLanguageOption.DEFAULT,
    val languageOptionVoiceActor: VoiceActorLanguageOption = VoiceActorLanguageOption.JAPANESE,
    val showFallbackVoiceActor: Boolean = true,
    val mediaHistoryEnabled: Boolean = false,
    val mediaHistoryMaxEntries: Int = 200,
    val mediaIgnoreEnabled: Boolean = false,
    val mediaIgnoreHide: Boolean = false,
)
