package com.thekeeperofpie.artistalleydatabase.alley.app

import coil3.util.ServiceLoaderComponentRegistry.register
import com.thekeeperofpie.artistalleydatabase.alley.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.w3c.dom.StorageEvent
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

expect fun initWebSettings(onNewValue: (key: String, value: String?) -> Unit)

@SingletonScope
@Inject
class ArtistAlleyWebSettings(
    private val applicationScope: ApplicationScope,
) : ArtistAlleySettings {

    val updates = MutableSharedFlow<Pair<String, String?>>(extraBufferCapacity = 20)

    init {
        initWebSettings { key, value -> updates.tryEmit(key to value) }
    }

    override val appTheme by register(
        serialize = { it.name },
        deserialize = {
            it?.let { theme -> AppThemeSetting.entries.find { it.name == theme } }
                ?: AppThemeSetting.AUTO
        },
    )
    override val lastKnownArtistsCsvSize by registerLong(-1L)
    override val lastKnownStampRalliesCsvSize by registerLong(-1L)
    override val displayType by register(
        serialize = { it.name },
        deserialize = { value ->
            SearchScreen.DisplayType.entries.find { it.name == value }
                ?: SearchScreen.DisplayType.CARD
        },
    )
    override val artistsSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            ArtistSearchSortOption.entries.find { it.name == value }
                ?: ArtistSearchSortOption.RANDOM
        },
    )
    override val artistsSortAscending by registerBoolean(true)
    override val stampRalliesSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            StampRallySearchSortOption.entries.find { it.name == value }
                ?: StampRallySearchSortOption.RANDOM
        },
    )
    override val stampRalliesSortAscending by registerBoolean(true)
    override val seriesSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            SeriesSearchSortOption.entries.find { it.name == value }
                ?: SeriesSearchSortOption.RANDOM
        },
    )
    override val seriesSortAscending by registerBoolean(true)
    override val showGridByDefault by registerBoolean(false)
    override val showRandomCatalogImage by registerBoolean(false)
    override val showOnlyConfirmedTags by registerBoolean(false)
    override val showOnlyWithCatalog by registerBoolean(false)
    override val forceOneDisplayColumn by registerBoolean(false)
    override val dataYear by register(
        serialize = { it.year.toString() },
        deserialize = {
            it?.toIntOrNull()?.let { year -> DataYear.entries.find { it.year == year } }
                ?: DataYear.YEAR_2025
        },
    )
    override val languageOption by register(
        serialize = { it.name },
        deserialize = { value ->
            AniListLanguageOption.entries.find { it.name == value }
                ?: AniListLanguageOption.DEFAULT
        },
    )

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
