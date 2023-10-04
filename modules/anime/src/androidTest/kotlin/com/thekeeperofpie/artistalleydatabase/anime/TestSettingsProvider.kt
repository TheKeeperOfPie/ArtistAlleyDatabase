package com.thekeeperofpie.artistalleydatabase.anime

import android.net.Uri
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TestSettingsProvider : NetworkSettings, AniListSettings, AnimeSettings, MonetizationSettings,
    EntrySettings {

    override val networkLoggingLevel =
        MutableStateFlow(NetworkSettings.NetworkLoggingLevel.NONE)
    override val enableNetworkCaching = MutableStateFlow(false)

    override val aniListViewer = MutableStateFlow<AniListViewer?>(null)

    override val savedAnimeFilters = MutableStateFlow(emptyMap<String, FilterData>())
    override val showAdult = MutableStateFlow(false)
    override val collapseAnimeFiltersOnClose = MutableStateFlow(false)
    override val showLessImportantTags = MutableStateFlow(false)
    override val showSpoilerTags = MutableStateFlow(false)
    override val animeNewsNetworkRegion =
        MutableStateFlow(AnimeNewsNetworkRegion.USA_CANADA)
    override val animeNewsNetworkCategoriesIncluded =
        MutableStateFlow(emptyList<AnimeNewsNetworkCategory>())
    override val animeNewsNetworkCategoriesExcluded =
        MutableStateFlow(emptyList<AnimeNewsNetworkCategory>())
    override val crunchyrollNewsCategoriesIncluded =
        MutableStateFlow(emptyList<CrunchyrollNewsCategory>())
    override val crunchyrollNewsCategoriesExcluded =
        MutableStateFlow(emptyList<CrunchyrollNewsCategory>())
    override val preferredMediaType = MutableStateFlow(MediaType.ANIME)
    override val mediaViewOption = MutableStateFlow(MediaViewOption.SMALL_CARD)
    override val rootNavDestination = MutableStateFlow(AnimeRootNavDestination.HOME)
    override val mediaHistoryEnabled = MutableStateFlow(false)
    override val mediaHistoryMaxEntries = MutableStateFlow(100)
    override val mediaIgnoreEnabled = MutableStateFlow(false)
    override val mediaIgnoreHide = MutableStateFlow(false)
    override val showIgnored = mediaIgnoreHide.map(Boolean::not)
    override val languageOptionMedia = MutableStateFlow(AniListLanguageOption.DEFAULT)
    override val languageOptionCharacters = MutableStateFlow(AniListLanguageOption.DEFAULT)
    override val languageOptionStaff = MutableStateFlow(AniListLanguageOption.DEFAULT)
    override val languageOptionVoiceActor =
        MutableStateFlow(VoiceActorLanguageOption.JAPANESE)
    override val showFallbackVoiceActor = MutableStateFlow(false)
    override val currentMediaListSizeAnime = MutableStateFlow(0)
    override val currentMediaListSizeManga = MutableStateFlow(0)
    override val lastCrash = MutableStateFlow("")
    override val lastCrashShown = MutableStateFlow(false)

    override val adsEnabled = MutableStateFlow(false)
    override val subscribed = MutableStateFlow(false)
    override val unlockAllFeatures = MutableStateFlow(false)

    override val cropDocumentUri = MutableStateFlow<Uri?>(null)
}
