package com.thekeeperofpie.artistalleydatabase.anime.media

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.MediaTagsQuery
import com.anilist.type.MediaFormat
import com.anilist.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.utils.toTextRes
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

        fun statuses() = MediaStatus.values().map { MediaFilterEntry(it to it.toTextRes()) }

        fun formats() = listOf(
            MediaFormat.TV,
            MediaFormat.TV_SHORT,
            MediaFormat.MOVIE,
            MediaFormat.SPECIAL,
            MediaFormat.OVA,
            MediaFormat.ONA,
            MediaFormat.MUSIC,
            // MANGA, NOVEL, and ONE_SHOT excluded since not anime
        ).map { MediaFilterEntry(it to it.toTextRes()) }
    }

    val sort = MutableStateFlow(sortEnumClass.java.enumConstants!!.first())
    val sortAscending = MutableStateFlow(false)

    val genres = MutableStateFlow(emptyList<MediaFilterEntry<String>>())
    val tagsByCategory =
        MutableStateFlow(emptyMap<String?, List<MediaFilterEntry<MediaTagsQuery.Data.MediaTagCollection>>>())
    val statuses = MutableStateFlow(statuses())
    val formats = MutableStateFlow(formats())

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
                                ?.map(::MediaFilterEntry)
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
                                ?.map(::MediaFilterEntry)
                                ?.groupBy { it.value.category }
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
                    if (it.value == genreName) {
                        it.copy(state = it.state.next())
                    } else it
                }
            }
    }

    private fun onTagClicked(tagId: Int) {
        tagsByCategory.value = tagsByCategory.value.toMutableMap()
            .apply {
                keys.forEach {
                    val existingTag = this[it]!!.find { it.value.id == tagId }
                    if (existingTag != null) {
                        this[it] = this[it]!!.toMutableList().apply {
                            replaceAll {
                                if (it.value.id == tagId) {
                                    it.copy(state = it.state.next())
                                } else it
                            }
                        }
                    }
                }
            }
    }

    private fun onStatusClicked(status: MediaStatus) {
        statuses.value = statuses.value.toMutableList()
            .apply {
                replaceAll {
                    if (it.value.first == status) {
                        it.copy(state = it.state.next())
                    } else it
                }
            }
    }

    private fun onFormatClicked(format: MediaFormat) {
        formats.value = formats.value.toMutableList()
            .apply {
                replaceAll {
                    if (it.value.first == format) {
                        it.copy(state = it.state.next())
                    } else it
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
        statuses = { statuses.collectAsState().value },
        onStatusClicked = ::onStatusClicked,
        formats = { formats.collectAsState().value },
        onFormatClicked = ::onFormatClicked,
    )

    class Data<SortOption>(
        val defaultOptions: List<SortOption>,
        val sort: @Composable () -> SortOption,
        val onSortChanged: (SortOption) -> Unit = {},
        val sortAscending: @Composable () -> Boolean = { false },
        val onSortAscendingChanged: (Boolean) -> Unit = {},
        val genres: @Composable () -> List<MediaFilterEntry<String>> = { emptyList() },
        val onGenreClicked: (String) -> Unit = {},
        val tagsByCategory: @Composable () -> Map<String?,
                List<MediaFilterEntry<MediaTagsQuery.Data.MediaTagCollection>>> = { emptyMap() },
        val onTagClicked: (Int) -> Unit = {},
        val statuses: @Composable () -> List<MediaFilterEntry<Pair<MediaStatus, Int>>> = {
            emptyList()
        },
        val onStatusClicked: (MediaStatus) -> Unit = {},
        val formats: @Composable () -> List<MediaFilterEntry<Pair<MediaFormat, Int>>> = {
            emptyList()
        },
        val onFormatClicked: (MediaFormat) -> Unit = {},
    ) {
        companion object {
            inline fun <reified T> forPreview(): Data<T>
                    where T : SortOption, T : Enum<*> {
                val enumConstants = T::class.java.enumConstants!!.toList()
                return Data(
                    defaultOptions = enumConstants,
                    sort = { enumConstants.first() },
                    genres = {
                        listOf("Action", "Adventure", "Drama", "Fantasy")
                            .map(::MediaFilterEntry)
                    },
                    tagsByCategory = {
                        mapOf(
                            "Category-One" to listOf(
                                "TagOne",
                                "TagTwo",
                                "TagThree"
                            ).mapIndexed { index, tag ->
                                MediaTagsQuery.Data.MediaTagCollection(id = index, name = tag)
                            }.map(::MediaFilterEntry),
                            "Category-Two" to listOf(
                                "TagFour",
                                "TagFive",
                                "TagSix"
                            ).mapIndexed { index, tag ->
                                MediaTagsQuery.Data.MediaTagCollection(id = index, name = tag)
                            }.map(::MediaFilterEntry)
                        )
                    },
                    statuses = { statuses() },
                    formats = { formats() },
                )
            }
        }

        interface SortOption {
            @get:StringRes
            val textRes: Int
        }
    }
}