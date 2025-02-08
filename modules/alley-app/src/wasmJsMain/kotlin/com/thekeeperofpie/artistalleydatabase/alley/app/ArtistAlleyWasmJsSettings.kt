package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
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

@SingletonScope
@Inject
class ArtistAlleyWasmJsSettings(
    private val applicationScope: ApplicationScope,
) : ArtistAlleySettings {

    val updates = MutableSharedFlow<Pair<String, String?>>(extraBufferCapacity = 20)

    init {
        window.addEventListener("storage") {
            it as StorageEvent
            val key = it.key ?: return@addEventListener
            updates.tryEmit(key to it.newValue)
        }
    }

    override val lastKnownArtistsCsvSize by registerLong(-1L)
    override val lastKnownStampRalliesCsvSize by registerLong(-1L)
    override val displayType by registerString("")
    override val artistsSortOption by registerString("")
    override val artistsSortAscending by registerBoolean(true)
    override val stampRalliesSortOption by registerString("")
    override val stampRalliesSortAscending by registerBoolean(true)
    override val showGridByDefault by registerBoolean(false)
    override val showRandomCatalogImage by registerBoolean(false)
    override val showOnlyConfirmedTags by registerBoolean(false)
    override val showOnlyHasCommissions by registerBoolean(false)
    override val showOnlyFavorites by registerBoolean(false)
    override val forceOneDisplayColumn by registerBoolean(false)
    override val activeYearIs2025 by registerBoolean(true)

    private fun <T> register(serialize: (T) -> String, deserialize: (String?) -> T) =
        object : ReadOnlyProperty<Any?, MutableStateFlow<T>> {
            private lateinit var flow: MutableStateFlow<T>
            override fun getValue(thisRef: Any?, property: KProperty<*>): MutableStateFlow<T> {
                if (!::flow.isInitialized) {
                    val key = property.name
                    flow = MutableStateFlow(deserialize(localStorage.getItem(key)))
                    applicationScope.launch {
                        flow.drop(1)
                            .collectLatest {
                                localStorage.setItem(key, serialize(it))
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
