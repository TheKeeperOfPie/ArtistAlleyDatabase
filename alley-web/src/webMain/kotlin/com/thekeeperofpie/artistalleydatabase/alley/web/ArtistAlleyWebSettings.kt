package com.thekeeperofpie.artistalleydatabase.alley.web

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.thekeeperofpie.artistalleydatabase.alley.AlleyRootDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistTag
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.listener
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.dom.StorageEvent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class ArtistAlleyWebSettings(
    private val applicationScope: ApplicationScope,
) : ArtistAlleySettings {

    val updates = MutableSharedFlow<Pair<String, String?>>(extraBufferCapacity = 20)

    // Separate instance to ensure app changes don't affect persistent settings
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        window.addEventListener("storage") {
            it as StorageEvent
            val key = it.key ?: return@addEventListener
            updates.tryEmit(key to it.newValue)
        }
    }

    override var appTheme by register(
        serialize = { it.name },
        deserialize = {
            it?.let { theme -> AppThemeSetting.entries.find { it.name == theme } }
                ?: AppThemeSetting.AUTO
        },
    )
    override var displayType by register(
        serialize = { it.name },
        deserialize = { value ->
            SearchDisplayType.entries.find { it.name == value }
                ?: SearchDisplayType.CARD
        },
    )
    override var artistsSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            ArtistSearchSortOption.entries.find { it.name == value }
                ?: ArtistSearchSortOption.RANDOM
        },
    )
    override var artistsSortAscending by registerBoolean(true)
    override var stampRalliesSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            StampRallySearchSortOption.entries.find { it.name == value }
                ?: StampRallySearchSortOption.RANDOM
        },
    )
    override var stampRalliesSortAscending by registerBoolean(true)
    override var seriesSortOption by register(
        serialize = { it.name },
        deserialize = { value ->
            SeriesSearchSortOption.entries.find { it.name == value }
                ?: SeriesSearchSortOption.RANDOM
        },
    )
    override var seriesSortAscending by registerBoolean(true)
    override var showGridByDefault by registerBoolean(false)
    override var showRandomCatalogImage by registerBoolean(false)
    override var showOnlyConfirmedTags by registerBoolean(false)
    override var forceOneDisplayColumn by registerBoolean(false)
    override var dataYear by register(
        serialize = { it.serializedName },
        deserialize = {
            val value = try {
                localStorage.getItem("dataYearLatest")?.let(DataYear::deserialize)
            } catch (t: Throwable) {
                ConsoleLogger.log("Failed to read dataYearLatest from localStorage: ${t.message}")
                null
            }
            if (value == DataYear.LATEST) {
                try {
                    localStorage.getItem("dataYear")?.let(DataYear::deserialize)
                } catch (t: Throwable) {
                    ConsoleLogger.log("Failed to read dataYear from localStorage: ${t.message}")
                    null
                } ?: DataYear.LATEST
            } else {
                try {
                    localStorage.setItem("dataYear", DataYear.LATEST.serializedName)
                } catch (t: Throwable) {
                    ConsoleLogger.log("Failed to write dataYear = ${DataYear.LATEST.serializedName} to localStorage: ${t.message}")
                }
                try {
                    localStorage.setItem("dataYearLatest", DataYear.LATEST.serializedName)
                } catch (t: Throwable) {
                    ConsoleLogger.log("Failed to write dataYearLatest = ${DataYear.LATEST.serializedName} to localStorage: ${t.message}")
                }
                DataYear.LATEST
            }
        },
    )
    override var languageOption by register(
        serialize = { it.name },
        deserialize = { value ->
            AniListLanguageOption.entries.find { it.name == value }
                ?: AniListLanguageOption.DEFAULT
        },
    )
    override var showOutdatedCatalogs by registerBoolean(false)
    override var easterEggEnabled by registerBoolean(false)

    override var artistsTagsIn by register(
        serialize = { json.encodeToString<Set<String>>(it.map { it.name }.toSet()) },
        deserialize = { value ->
            value?.let { json.decodeFromString<Set<String>>(it) }
                .orEmpty()
                .mapNotNull { target ->
                    ArtistTag.entries.find { it.name == target }
                }
                .toSet()
        },
    )
    override var artistsTagsNotIn by register(
        serialize = { json.encodeToString<Set<String>>(it.map { it.name }.toSet()) },
        deserialize = { value ->
            value?.let { json.decodeFromString<Set<String>>(it) }
                .orEmpty()
                .mapNotNull { target ->
                    ArtistTag.entries.find { it.name == target }
                }
                .toSet()
        },
    )
    override var rootDestination by register(
        serialize = { it.name },
        deserialize = { value ->
            AlleyRootDestination.entries.find { it.name == value }
                ?: AlleyRootDestination.ARTISTS
        },
    )

    private fun <T> register(serialize: (T) -> String, deserialize: (String?) -> T) =
        object : ReadWriteProperty<Any?, T> {
            private lateinit var state: MutableState<T>
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                if (!::state.isInitialized) {
                    val key = property.name
                    val value = try {
                        localStorage.getItem(key)
                    } catch (t: Throwable) {
                        ConsoleLogger.log("Failed to read $key from localStorage: ${t.message}")
                        null
                    }
                    state = mutableStateOf(deserialize(value))
                    applicationScope.launch {
                        listener {
                            val value = state.value
                            OnChangeEffect(value) {
                                ConsoleLogger.log("Writing $key = $value")
                                try {
                                    localStorage.setItem(key, serialize(value))
                                } catch (t: Throwable) {
                                    ConsoleLogger.log("Failed to write $key = $value to localStorage: ${t.message}")
                                }
                            }
                        }
                    }
                    applicationScope.launch {
                        updates.filter { it.first == key }
                            .collectLatest {
                                ConsoleLogger.log("Propagating $key = $value")
                                state.value = deserialize(it.second)
                            }
                    }
                }
                return state.value
            }

            override fun setValue(
                thisRef: Any?,
                property: KProperty<*>,
                value: T,
            ) {
                state.value = value
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
