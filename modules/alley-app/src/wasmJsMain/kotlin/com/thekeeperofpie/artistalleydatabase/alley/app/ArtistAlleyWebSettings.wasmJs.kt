package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// TODO: Merge into webMain, can't import kotlinx.browser for some reason
@SingleIn(AppScope::class)
@Inject
actual class ArtistAlleyWebSettings(
    private val applicationScope: ApplicationScope,
) : ArtistAlleySettings {

    val updates = MutableSharedFlow<Pair<String, String?>>(extraBufferCapacity = 20)

    init {
        initWebSettings { key, value -> updates.tryEmit(key to value) }
    }

    actual override val appTheme by register(
        serialize = { it.name },
        deserialize = {
            it?.let { theme -> AppThemeSetting.entries.find { it.name == theme } }
                ?: AppThemeSetting.AUTO
        },
    )
    actual override val lastKnownArtistsCsvSize by registerLong(-1L)
    actual override val lastKnownStampRalliesCsvSize by registerLong(-1L)
    actual override val displayType by register(
        serialize = { it.name },
        deserialize = { value ->
            SearchScreen.DisplayType.entries.find { it.name == value }
                ?: SearchScreen.DisplayType.CARD
        },
    )
    actual override val artistsSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            ArtistSearchSortOption.entries.find { it.name == value }
                ?: ArtistSearchSortOption.RANDOM
        },
    )
    actual override val artistsSortAscending by registerBoolean(true)
    actual override val stampRalliesSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            StampRallySearchSortOption.entries.find { it.name == value }
                ?: StampRallySearchSortOption.RANDOM
        },
    )
    actual override val stampRalliesSortAscending by registerBoolean(true)
    actual override val seriesSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            SeriesSearchSortOption.entries.find { it.name == value }
                ?: SeriesSearchSortOption.RANDOM
        },
    )
    actual override val seriesSortAscending by registerBoolean(true)
    actual override val showGridByDefault by registerBoolean(false)
    actual override val showRandomCatalogImage by registerBoolean(false)
    actual override val showOnlyConfirmedTags by registerBoolean(false)
    actual override val showOnlyWithCatalog by registerBoolean(false)
    actual override val forceOneDisplayColumn by registerBoolean(false)
    actual override val dataYear by register(
        serialize = { it.serializedName },
        deserialize = {
            it?.let { serializedName -> DataYear.entries.find { it.serializedName == serializedName } }
                ?: DataYear.LATEST
        },
    )
    actual override val languageOption by register(
        serialize = { it.name },
        deserialize = { value ->
            AniListLanguageOption.entries.find { it.name == value }
                ?: AniListLanguageOption.DEFAULT
        },
    )
    actual override val showOutdatedCatalogs by registerBoolean(false)

    private fun <T> register(serialize: (T) -> String, deserialize: (String?) -> T) =
        object : ReadOnlyProperty<Any?, MutableStateFlow<T>> {
            private lateinit var flow: MutableStateFlow<T>
            override fun getValue(thisRef: Any?, property: KProperty<*>): MutableStateFlow<T> {
                if (!::flow.isInitialized) {
                    val key = property.name
                    val value = try {
                        localStorage.getItem(key)
                    } catch (t: Throwable) {
                        ConsoleLogger.log("Failed to read $key from localStorage: ${t.message}")
                        null
                    }
                    flow = MutableStateFlow(deserialize(value))
                    applicationScope.launch {
                        flow.drop(1)
                            .collectLatest {
                                try {
                                    localStorage.setItem(key, serialize(it))
                                } catch (t: Throwable) {
                                    ConsoleLogger.log("Failed to write $key = $it to localStorage: ${t.message}")
                                }
                            }
                    }
                    applicationScope.launch {
                        updates.filter { it.first == key }
                            .collectLatest {
                                flow.emit(deserialize(it.second))
                            }
                    }
                }
                return flow
            }
        }

    private fun registerBoolean(defaultValue: Boolean) = register<Boolean>(
        serialize = Boolean::toString,
        deserialize = { it?.toBoolean() ?: defaultValue },
    )

    private fun registerString(defaultValue: String) = register<String>(
        serialize = { it },
        deserialize = { it ?: defaultValue },
    )

    private fun registerLong(defaultValue: Long) = register<Long>(
        serialize = Long::toString,
        deserialize = { it?.toLongOrNull() ?: defaultValue },
    )
}
