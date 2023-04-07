package com.thekeeperofpie.artistalleydatabase.anime.media

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

class AnimeMediaFilterController<T>(
    private val sortEnumClass: KClass<T>,
    private val aniListApi: AuthedAniListApi,
) where T : AnimeMediaFilterController.Data.SortOption, T : Enum<*> {

    companion object {
        private const val TAG = "AnimeMediaFilterController"
    }

    val sort = MutableStateFlow(sortEnumClass.java.enumConstants!!.first())
    val sortAscending = MutableStateFlow(false)

    val genres = MutableStateFlow(emptyList<MediaGenreEntry>())
    val tagsByCategory = MutableStateFlow(emptyMap<String?, List<MediaTagEntry>>())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun initialize(viewModel: ViewModel, refreshUpdates: StateFlow<*>) {
        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            refreshUpdates
                .mapLatestNotNull {
                    withContext(CustomDispatchers.IO) {
                        try {
                            aniListApi.genres().dataAssertNoErrors
                                .genreCollection
                                ?.filterNotNull()
                                ?.map(::MediaGenreEntry)
                        } catch (e: Exception) {
                            Log.d(TAG, "Error loading genres", e)
                            null
                        }
                    }
                }
                .take(1)
                .collectLatest(genres::emit)
        }

        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            refreshUpdates
                .mapLatestNotNull {
                    withContext(CustomDispatchers.IO) {
                        try {
                            aniListApi.tags().dataAssertNoErrors
                                .mediaTagCollection
                                ?.filterNotNull()
                                ?.map {
                                    MediaTagEntry(
                                        id = it.id.toString(),
                                        category = it.category,
                                        name = it.name,
                                        description = it.description,
                                        adult = it.isAdult ?: false,
                                        generalSpoiler = it.isGeneralSpoiler ?: false,
                                    )
                                }
                                ?.groupBy { it.category }
                                ?.toSortedMap { first, second ->
                                    when {
                                        first == second -> 0
                                        first == null -> -1
                                        second == null -> 1
                                        else -> String.CASE_INSENSITIVE_ORDER
                                            .compare(first, second)
                                    }
                                }
                        } catch (e: Exception) {
                            Log.d(TAG, "Error loading genres", e)
                            null
                        }
                    }
                }
                .take(1)
                .collectLatest(tagsByCategory::emit)
        }
    }

    private fun onSortChanged(option: T) = sort.update { option }

    private fun onSortAscendingChanged(ascending: Boolean) = sortAscending.update { ascending }

    private fun onGenreClicked(genreName: String) {
        genres.value = genres.value.toMutableList()
            .apply {
                replaceAll {
                    if (it.name == genreName) {
                        val values = MediaGenreEntry.State.values()
                        val newState = values[(values.indexOf(it.state) + 1) % values.size]
                        it.copy(state = newState)
                    } else it
                }
            }
    }

    private fun onTagClicked(tagId: String) {
        tagsByCategory.value = tagsByCategory.value.toMutableMap()
            .apply {
                keys.forEach {
                    val existingTag = this[it]!!.find { it.id == tagId }
                    if (existingTag != null) {
                        this[it] = this[it]!!.toMutableList().apply {
                            replaceAll {
                                if (it.id == tagId) {
                                    val values = MediaTagEntry.State.values()
                                    val newState =
                                        values[(values.indexOf(it.state) + 1) % values.size]
                                    it.copy(state = newState)
                                } else it
                            }
                        }
                    }
                }
            }
    }

    fun data() = Data(
        defaultOptions = sortEnumClass.java.enumConstants!!.toList(),
        sort = { sort.collectAsState().value },
        onSortChanged = ::onSortChanged,
        sortAscending = { sortAscending.collectAsState().value },
        onSortAscendingChanged = ::onSortAscendingChanged,
        genres = { genres.collectAsState().value },
        onGenreClicked = ::onGenreClicked,
        tagsByCategory = { tagsByCategory.collectAsState().value },
        onTagClicked = ::onTagClicked,
    )

    class Data<SortOption>(
        val defaultOptions: List<SortOption>,
        val sort: @Composable () -> SortOption,
        val onSortChanged: (SortOption) -> Unit = {},
        val sortAscending: @Composable () -> Boolean = { false },
        val onSortAscendingChanged: (Boolean) -> Unit = {},
        val genres: @Composable () -> List<MediaGenreEntry> = { emptyList() },
        val onGenreClicked: (String) -> Unit = {},
        val tagsByCategory: @Composable () -> Map<String?, List<MediaTagEntry>> = { emptyMap() },
        val onTagClicked: (String) -> Unit = {},
    ) {
        companion object {
            inline fun <reified T> forPreview(): Data<T>
                    where T : SortOption, T : Enum<*> {
                val enumConstants = T::class.java.enumConstants!!.toList()
                return Data(
                    defaultOptions = enumConstants,
                    sort = { enumConstants.first() }
                )
            }
        }

        interface SortOption {
            @get:StringRes
            val textRes: Int
        }
    }
}