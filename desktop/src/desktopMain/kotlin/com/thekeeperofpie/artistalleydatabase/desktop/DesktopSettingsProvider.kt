package com.thekeeperofpie.artistalleydatabase.desktop

import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeRootNavDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.news.ann.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.news.ann.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.news.cr.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import kotlinx.coroutines.flow.MutableStateFlow

class DesktopSettingsProvider : AniListSettings, AnimeSettings, CropSettings, MonetizationSettings, NewsSettings, NetworkSettings {

    val appTheme = MutableStateFlow(AppThemeSetting.AUTO)

    override val aniListViewer = MutableStateFlow<AniListViewer?>(null)
    override val ignoreViewer = MutableStateFlow(false)

    override val savedAnimeFilters = MutableStateFlow(emptyMap<String, FilterData>())
    override val showAdult = MutableStateFlow(false)
    override val collapseAnimeFiltersOnClose = MutableStateFlow(false)
    override val showLessImportantTags = MutableStateFlow(false)
    override val showSpoilerTags = MutableStateFlow(false)
    override val preferredMediaType = MutableStateFlow(MediaType.ANIME)
    override val mediaViewOption = MutableStateFlow(MediaViewOption.SMALL_CARD)
    override val rootNavDestination = MutableStateFlow(AnimeRootNavDestination.HOME)
    override val mediaHistoryEnabled = MutableStateFlow(false)
    override val mediaHistoryMaxEntries = MutableStateFlow(200)
    override val mediaIgnoreEnabled = MutableStateFlow(false)
    override val mediaIgnoreHide = MutableStateFlow(false)
    override val showIgnored = MutableStateFlow(true)
    override val languageOptionMedia = MutableStateFlow(AniListLanguageOption.DEFAULT)
    override val languageOptionCharacters = MutableStateFlow(AniListLanguageOption.DEFAULT)
    override val languageOptionStaff = MutableStateFlow(AniListLanguageOption.DEFAULT)
    override val languageOptionVoiceActor = MutableStateFlow(VoiceActorLanguageOption.JAPANESE)
    override val showFallbackVoiceActor = MutableStateFlow(true)
    override val currentMediaListSizeAnime = MutableStateFlow(3)
    override val currentMediaListSizeManga = MutableStateFlow(3)
    override val lastCrash = MutableStateFlow("")
    override val lastCrashShown = MutableStateFlow(false)

    override val cropImageUri = MutableStateFlow<String?>(null)

    override val adsEnabled = MutableStateFlow(false)
    override val subscribed = MutableStateFlow(false)
    override val unlockAllFeatures = MutableStateFlow(false)

    override val animeNewsNetworkRegion = MutableStateFlow(AnimeNewsNetworkRegion.USA_CANADA)
    override val animeNewsNetworkCategoriesIncluded =
        MutableStateFlow(emptyList<AnimeNewsNetworkCategory>())
    override val animeNewsNetworkCategoriesExcluded =
        MutableStateFlow(emptyList<AnimeNewsNetworkCategory>())
    override val crunchyrollNewsCategoriesIncluded =
        MutableStateFlow(emptyList<CrunchyrollNewsCategory>())
    override val crunchyrollNewsCategoriesExcluded =
        MutableStateFlow(emptyList<CrunchyrollNewsCategory>())

    override val networkLoggingLevel = MutableStateFlow(NetworkSettings.NetworkLoggingLevel.NONE)
    override val enableNetworkCaching = MutableStateFlow(false)
}