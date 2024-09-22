package com.thekeeperofpie.artistalleydatabase.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeRootNavDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.cr.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.tatarka.inject.annotations.Inject
import kotlin.reflect.KProperty0

@SingletonScope
@Inject
class SettingsProvider(
    scope: ApplicationScope,
    private val json: Json,
    private val featureOverrideProvider: FeatureOverrideProvider,
    private val settingsStore: SettingsStore,
) : ArtSettings, CropSettings, NetworkSettings, AnimeSettings, MonetizationSettings,
    AniListSettings, NewsSettings {

    companion object {
        const val EXPORT_FILE_NAME = "settings.json"
    }

    override val artEntryTemplate = MutableStateFlow<ArtEntry?>(deserialize("artEntryTemplate"))
    override val cropImageUri = MutableStateFlow<String?>(deserialize("cropImageUri"))
    override val networkLoggingLevel = MutableStateFlow(
        deserialize("networkLoggingLevel") ?: NetworkSettings.NetworkLoggingLevel.NONE
    )
    override val enableNetworkCaching =
        MutableStateFlow(deserialize("enableNetworkCaching") ?: false)
    override var savedAnimeFilters = MutableStateFlow(deserializeAnimeFilters())

    override var showAdult = ignoreOnRelease("showAdult", false)

    override var collapseAnimeFiltersOnClose = MutableStateFlow(
        deserialize("collapseAnimeFiltersOnClose") ?: true
    )

    override var showLessImportantTags =
        MutableStateFlow(deserialize("showLessImportantTags") ?: false)

    override var showSpoilerTags =
        MutableStateFlow(deserialize("showSpoilerTags") ?: false)

    override val animeNewsNetworkRegion =
        MutableStateFlow(deserialize("animeNewsNetworkRegion") ?: AnimeNewsNetworkRegion.USA_CANADA)

    override val animeNewsNetworkCategoriesIncluded = MutableStateFlow(
        deserialize("animeNewsNetworkCategoriesIncluded") ?: emptyList<AnimeNewsNetworkCategory>()
    )
    override val animeNewsNetworkCategoriesExcluded = MutableStateFlow(
        deserialize("animeNewsNetworkCategoriesExcluded") ?: emptyList<AnimeNewsNetworkCategory>()
    )

    override val crunchyrollNewsCategoriesIncluded = MutableStateFlow(
        deserialize("crunchyrollNewsCategoriesIncluded") ?: emptyList<CrunchyrollNewsCategory>()
    )
    override val crunchyrollNewsCategoriesExcluded = MutableStateFlow(
        deserialize("crunchyrollNewsCategoriesExcluded") ?: emptyList<CrunchyrollNewsCategory>()
    )

    override val adsEnabled = MutableStateFlow(deserialize("adsEnabled") ?: false)
    override val subscribed = MutableStateFlow(deserialize("subscribed") ?: false)

    val searchQuery = MutableStateFlow<ArtEntry?>(deserialize("searchQuery"))
    val navDrawerStartDestination =
        MutableStateFlow<String?>(deserialize("navDrawerStartDestination"))
    val hideStatusBar = MutableStateFlow(deserialize("hideStatusBar") ?: false)
    val appTheme = MutableStateFlow(deserialize("appTheme") ?: AppThemeSetting.AUTO)

    override val preferredMediaType =
        MutableStateFlow(deserialize("preferredMediaType") ?: MediaType.ANIME)

    override val mediaViewOption =
        MutableStateFlow(deserialize("mediaViewOption") ?: MediaViewOption.SMALL_CARD)

    override val rootNavDestination =
        MutableStateFlow(deserialize("rootNavDestination") ?: AnimeRootNavDestination.HOME)

    override val languageOptionMedia =
        MutableStateFlow(deserialize("languageOptionMedia") ?: AniListLanguageOption.DEFAULT)
    override val languageOptionCharacters =
        MutableStateFlow(deserialize("languageOptionCharacters") ?: AniListLanguageOption.DEFAULT)
    override val languageOptionStaff =
        MutableStateFlow(deserialize("languageOptionStaff") ?: AniListLanguageOption.DEFAULT)
    override val languageOptionVoiceActor =
        MutableStateFlow(deserialize("languageOptionVoiceActor") ?: VoiceActorLanguageOption.JAPANESE)
    override val showFallbackVoiceActor =
        MutableStateFlow(deserialize("showFallbackVoiceActor") ?: false)

    override val mediaHistoryEnabled = MutableStateFlow(deserialize("mediaHistoryEnabled") ?: false)
    override val mediaHistoryMaxEntries = MutableStateFlow(deserialize("mediaHistoryMaxEntries") ?: 200)

    override val mediaIgnoreEnabled = MutableStateFlow(deserialize("mediaIgnoreEnabled") ?: false)
    override val mediaIgnoreHide = MutableStateFlow(deserialize("mediaIgnoreHide") ?: false)
    override val showIgnored = mediaIgnoreHide.mapState(scope, Boolean::not)

    // Not exported
    override val aniListViewer = MutableStateFlow<AniListViewer?>(deserialize("aniListViewer"))
    override val ignoreViewer = MutableStateFlow(deserialize("ignoreViewer") ?: false)
    override val lastCrash = MutableStateFlow(deserialize("lastCrash") ?: "")
    override val lastCrashShown = MutableStateFlow(deserialize("lastCrashShown") ?: false)
    val screenshotMode = MutableStateFlow(deserialize("screenshotMode") ?: false)

    override val currentMediaListSizeAnime =
        MutableStateFlow(deserialize("currentMediaListSizeAnime") ?: 3)
    override val currentMediaListSizeManga =
        MutableStateFlow(deserialize("currentMediaListSizeManga") ?: 3)

    override var unlockAllFeatures = ignoreOnRelease("unlockAllFeatures", false)

    private fun deserializeAnimeFilters(): Map<String, FilterData> {
        val stringValue = settingsStore.readString("savedAnimeFilters")
        if (stringValue.isNullOrBlank()) return emptyMap()
        return json.decodeFromString(stringValue)
    }

    val settingsData: SettingsData
        get() = SettingsData(
            artEntryTemplate = artEntryTemplate.value,
            cropImageUri = cropImageUri.value,
            networkLoggingLevel = networkLoggingLevel.value,
            enableNetworkCaching = enableNetworkCaching.value,
            searchQuery = searchQuery.value,
            collapseAnimeFiltersOnClose = collapseAnimeFiltersOnClose.value,
            savedAnimeFilters = savedAnimeFilters.value,
            showAdult = showAdult.value,
            showLessImportantTags = showLessImportantTags.value,
            showSpoilerTags = showSpoilerTags.value,
            animeNewsNetworkRegion = animeNewsNetworkRegion.value,
            animeNewsNetworkCategoriesIncluded = animeNewsNetworkCategoriesIncluded.value,
            animeNewsNetworkCategoriesExcluded = animeNewsNetworkCategoriesExcluded.value,
            crunchyrollNewsCategoriesIncluded = crunchyrollNewsCategoriesIncluded.value,
            crunchyrollNewsCategoriesExcluded = crunchyrollNewsCategoriesExcluded.value,
            navDrawerStartDestination = navDrawerStartDestination.value,
            hideStatusBar = hideStatusBar.value,
            adsEnabled = adsEnabled.value,
            subscribed = subscribed.value,
            appTheme = appTheme.value,
            preferredMediaType = preferredMediaType.value,
            mediaViewOption = mediaViewOption.value,
            rootNavDestination = rootNavDestination.value,
            languageOptionMedia = languageOptionMedia.value,
            languageOptionCharacters = languageOptionCharacters.value,
            languageOptionStaff = languageOptionStaff.value,
            languageOptionVoiceActor = languageOptionVoiceActor.value,
            showFallbackVoiceActor = showFallbackVoiceActor.value,
            mediaHistoryEnabled = mediaHistoryEnabled.value,
            mediaHistoryMaxEntries = mediaHistoryMaxEntries.value,
            mediaIgnoreEnabled = mediaIgnoreEnabled.value,
            mediaIgnoreHide = mediaIgnoreHide.value,
        )

    init {
        subscribeProperty(scope, ::artEntryTemplate)
        subscribeProperty(scope, ::cropImageUri)
        subscribeProperty(scope, ::networkLoggingLevel)
        subscribeProperty(scope, ::enableNetworkCaching)
        subscribeProperty(scope, ::searchQuery)
        subscribeProperty(scope, ::collapseAnimeFiltersOnClose)
        subscribeProperty(scope, ::showAdult)
        subscribeProperty(scope, ::navDrawerStartDestination)
        subscribeProperty(scope, ::hideStatusBar)
        subscribeProperty(scope, ::aniListViewer)
        subscribeProperty(scope, ::ignoreViewer)
        subscribeProperty(scope, ::lastCrash)
        subscribeProperty(scope, ::lastCrashShown)
        subscribeProperty(scope, ::screenshotMode)
        subscribeProperty(scope, ::currentMediaListSizeAnime)
        subscribeProperty(scope, ::currentMediaListSizeManga)
        subscribeProperty(scope, ::unlockAllFeatures)
        subscribeProperty(scope, ::animeNewsNetworkRegion)
        subscribeProperty(scope, ::adsEnabled)
        subscribeProperty(scope, ::subscribed)
        subscribeProperty(scope, ::appTheme)
        subscribeProperty(scope, ::showLessImportantTags)
        subscribeProperty(scope, ::showSpoilerTags)
        subscribeProperty(scope, ::preferredMediaType)
        subscribeProperty(scope, ::mediaViewOption)
        subscribeProperty(scope, ::rootNavDestination)
        subscribeProperty(scope, ::languageOptionMedia)
        subscribeProperty(scope, ::languageOptionCharacters)
        subscribeProperty(scope, ::languageOptionStaff)
        subscribeProperty(scope, ::languageOptionVoiceActor)
        subscribeProperty(scope, ::showFallbackVoiceActor)
        subscribeProperty(scope, ::mediaHistoryEnabled)
        subscribeProperty(scope, ::mediaHistoryMaxEntries)
        subscribeProperty(scope, ::mediaIgnoreEnabled)
        subscribeProperty(scope, ::mediaIgnoreHide)

        scope.launch(CustomDispatchers.IO) {
            savedAnimeFilters.drop(1).collectLatest {
                val stringValue = json.run { encodeToString(it) }
                settingsStore.writeString("savedAnimeFilters", stringValue)
            }
        }
        scope.launch(CustomDispatchers.IO) {
            animeNewsNetworkCategoriesIncluded.drop(1).collectLatest {
                val stringValue = json.run { encodeToString(it) }
                settingsStore.writeString("animeNewsNetworkCategoriesIncluded", stringValue)
            }
        }
        scope.launch(CustomDispatchers.IO) {
            animeNewsNetworkCategoriesExcluded.drop(1).collectLatest {
                val stringValue = json.run { encodeToString(it) }
                settingsStore.writeString("animeNewsNetworkCategoriesExcluded", stringValue)
            }
        }
        scope.launch(CustomDispatchers.IO) {
            crunchyrollNewsCategoriesIncluded.drop(1).collectLatest {
                val stringValue = json.run { encodeToString(it) }
                settingsStore.writeString("crunchyrollNewsCategoriesIncluded", stringValue)
            }
        }
        scope.launch(CustomDispatchers.IO) {
            crunchyrollNewsCategoriesExcluded.drop(1).collectLatest {
                val stringValue = json.run { encodeToString(it) }
                settingsStore.writeString("crunchyrollNewsCategoriesExcluded", stringValue)
            }
        }
    }

    fun writeLastCrash(throwable: Throwable) {
        val stackString = throwable.stackTraceToString()
        lastCrash.value = stackString
        lastCrashShown.value = false
        serialize("lastCrash", stackString, commitImmediately = true)
        serialize("lastCrashShown", false, commitImmediately = true)
    }

    private inline fun <reified T> subscribeProperty(
        scope: CoroutineScope,
        property: KProperty0<MutableStateFlow<T>>,
    ) = scope.launch(CustomDispatchers.IO) {
        property.get().drop(1).collectLatest {
            serialize(property.name, it)
        }
    }

    @Composable
    fun composeSettingsData(): ComposeSettingsData {
        val screenshotMode by screenshotMode.collectAsState()
        return remember(screenshotMode) {
            ComposeSettingsData(
                screenshotMode = screenshotMode,
            )
        }
    }

    suspend fun overwrite(data: SettingsData) {
        artEntryTemplate.emit(data.artEntryTemplate)
        cropImageUri.emit(data.cropImageUri)
        networkLoggingLevel.emit(data.networkLoggingLevel)
        enableNetworkCaching.emit(data.enableNetworkCaching)
        searchQuery.emit(data.searchQuery)
        savedAnimeFilters.emit(data.savedAnimeFilters)
        collapseAnimeFiltersOnClose.emit(data.collapseAnimeFiltersOnClose)
        showAdult.emit(data.showAdult)
        showLessImportantTags.emit(data.showLessImportantTags)
        showSpoilerTags.emit(data.showSpoilerTags)
        animeNewsNetworkRegion.emit(data.animeNewsNetworkRegion)
        animeNewsNetworkCategoriesIncluded.emit(data.animeNewsNetworkCategoriesIncluded)
        animeNewsNetworkCategoriesExcluded.emit(data.animeNewsNetworkCategoriesExcluded)
        crunchyrollNewsCategoriesIncluded.emit(data.crunchyrollNewsCategoriesIncluded)
        crunchyrollNewsCategoriesExcluded.emit(data.crunchyrollNewsCategoriesExcluded)
        navDrawerStartDestination.emit(data.navDrawerStartDestination)
        hideStatusBar.emit(data.hideStatusBar)
        adsEnabled.emit(data.adsEnabled)
        subscribed.emit(data.subscribed)
        appTheme.emit(data.appTheme)
        preferredMediaType.emit(data.preferredMediaType)
        mediaViewOption.emit(data.mediaViewOption)
        rootNavDestination.emit(data.rootNavDestination)
        languageOptionMedia.emit(data.languageOptionMedia)
        languageOptionCharacters.emit(data.languageOptionCharacters)
        languageOptionStaff.emit(data.languageOptionStaff)
        languageOptionVoiceActor.emit(data.languageOptionVoiceActor)
        showFallbackVoiceActor.emit(data.showFallbackVoiceActor)
        mediaHistoryEnabled.emit(data.mediaHistoryEnabled)
        mediaHistoryMaxEntries.emit(data.mediaHistoryMaxEntries)
        mediaIgnoreEnabled.emit(data.mediaIgnoreEnabled)
        mediaIgnoreHide.emit(data.mediaIgnoreHide)
    }

    private inline fun <reified T> deserialize(name: String): T? {
        val stringValue = settingsStore.readString(name)
        return if (stringValue.isNullOrBlank()) {
            null
        } else try {
            json.decodeFromString<T>(stringValue)
        } catch (ignored: Throwable) {
            null
        }
    }

    private inline fun <reified T : Any?> serialize(name: String, value: T?) =
        serialize(name, value, commitImmediately = false)

    private inline fun <reified T : Any?> serialize(
        name: String,
        value: T,
        commitImmediately: Boolean,
    ) {
        val stringValue = json.encodeToString(json.serializersModule.serializer<T>(), value)
        settingsStore.writeString(name, stringValue, commitImmediately = commitImmediately)
    }

    private inline fun <reified T> ignoreOnRelease(propertyName: String, defaultValue: T) =
        if (featureOverrideProvider.isReleaseBuild) {
            IgnoringMutableStateFlow(defaultValue)
        } else {
            MutableStateFlow(deserialize<T>(propertyName) ?: defaultValue)
        }

    private class IgnoringMutableStateFlow<T>(defaultValue: T) : MutableStateFlow<T> {
        private val flow = MutableStateFlow(defaultValue)
        override val replayCache: List<T>
            get() = flow.replayCache
        override val subscriptionCount: StateFlow<Int>
            get() = flow.subscriptionCount
        override var value: T
            get() = flow.value
            set(value) {
                // Do not set anything on release
            }

        override suspend fun collect(collector: FlowCollector<T>) =
            flow.collect(collector)

        override fun compareAndSet(expect: T, update: T) = true

        @ExperimentalCoroutinesApi
        override fun resetReplayCache() = Unit

        override fun tryEmit(value: T) = true

        override suspend fun emit(value: T) = Unit
    }
}
