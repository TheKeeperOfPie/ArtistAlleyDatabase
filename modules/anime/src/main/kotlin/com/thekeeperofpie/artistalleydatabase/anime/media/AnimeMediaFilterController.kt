package com.thekeeperofpie.artistalleydatabase.anime.media

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.MediaTagsQuery.Data.MediaTagCollection
import com.anilist.type.MediaFormat
import com.anilist.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.anime.utils.toTextRes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.SortedMap
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

    val genres = MutableStateFlow(emptyList<GenreEntry>())
    val tagsByCategory = MutableStateFlow(emptyMap<String, TagSection>())
    val statuses = MutableStateFlow(StatusEntry.statuses())
    val formats = MutableStateFlow(FormatEntry.formats())
    val showAdult = MutableStateFlow(false)

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
                                ?.map(::GenreEntry)
                        } catch (e: Exception) {
                            Log.d(TAG, "Error loading genres", e)
                            null
                        }
                    }
                }
                .take(1)
                .flatMapLatest { genres ->
                    showAdult.map {
                        if (it) {
                            genres
                        } else {
                            genres.filterNot { it.isAdult }
                        }
                    }
                }
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
                                ?.let(::buildTagSections)
                        } catch (e: Exception) {
                            Log.d(TAG, "Error loading genres", e)
                            null
                        }
                    }
                }
                .take(1)
                .flatMapLatest { tags ->
                    showAdult.map { showAdult ->
                        if (showAdult) return@map tags
                        tags.values.mapNotNull { it.filter { it.isAdult != true } }
                            .associateBy { it.name }
                            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
                    }
                }
                .collectLatest(tagsByCategory::emit)
        }
    }

    /**
     * Categories are provided from the API in the form of "Parent-Child". This un-flattens the
     * tag list into a tree of sections, separated by the "-" dash.
     */
    private fun buildTagSections(tags: List<MediaTagCollection>): SortedMap<String, TagSection> {
        val sections = mutableMapOf<String, Any>()
        tags.forEach {
            var categories = it.category?.split('-')

            // Manually handle the "Sci-Fi" category, which contains a dash, but shouldn't be split
            if (categories != null) {
                val sciIndex = categories.indexOf("Sci")
                if (sciIndex >= 0) {
                    val hasFi = categories.getOrNull(sciIndex + 1) == "Fi"
                    if (hasFi) {
                        categories = categories.toMutableList().apply {
                            removeAt(sciIndex + 1)
                            set(sciIndex, "Sci-Fi")
                        }
                    }
                }
            }

            var currentCategory: TagSection.Category.Builder? = null
            categories?.forEach {
                currentCategory = if (currentCategory == null) {
                    sections.getOrPut(it) { TagSection.Category.Builder(it) }
                            as TagSection.Category.Builder
                } else {
                    (currentCategory as TagSection.Category.Builder).getOrPutCategory(it)
                }
            }

            if (currentCategory == null) {
                sections[it.name] = TagSection.Tag(it)
            } else {
                currentCategory!!.addChild(it)
            }
        }

        return sections.mapValues { (_, value) ->
            when (value) {
                is TagSection.Category.Builder -> value.build()
                is TagSection.Tag -> value
                else -> throw IllegalStateException("Unexpected value $value")
            }
        }.toSortedMap(String.CASE_INSENSITIVE_ORDER)
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
        fun TagSection.replace(): TagSection = when (this) {
            is TagSection.Category -> {
                copy(children = children.mapValues { (_, value) -> value.replace() }
                    .toSortedMap(String.CASE_INSENSITIVE_ORDER))
            }
            is TagSection.Tag -> {
                if (id == tagId.toString()) {
                    copy(state = state.next())
                } else this
            }
        }

        tagsByCategory.value = tagsByCategory.value
            .mapValues { (_, value) -> value.replace() }
    }

    private fun onStatusClicked(status: MediaStatus) {
        statuses.value = statuses.value.toMutableList()
            .apply {
                replaceAll {
                    if (it.value == status) {
                        it.copy(state = it.state.next())
                    } else it
                }
            }
    }

    private fun onFormatClicked(format: MediaFormat) {
        formats.value = formats.value.toMutableList()
            .apply {
                replaceAll {
                    if (it.value == format) {
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
        statuses = { statuses.collectAsState().value },
        onStatusClicked = ::onStatusClicked,
        formats = { formats.collectAsState().value },
        onFormatClicked = ::onFormatClicked,
        genres = { genres.collectAsState().value },
        onGenreClicked = ::onGenreClicked,
        tags = { tagsByCategory.collectAsState().value },
        onTagClicked = ::onTagClicked,
        showAdult = { showAdult.collectAsState().value },
        onShowAdultToggled = { showAdult.value = it },
    )

    class Data<SortOption>(
        val defaultOptions: List<SortOption>,
        val sort: @Composable () -> SortOption,
        val onSortChanged: (SortOption) -> Unit = {},
        val sortAscending: @Composable () -> Boolean = { false },
        val onSortAscendingChanged: (Boolean) -> Unit = {},
        val statuses: @Composable () -> List<StatusEntry> = { emptyList() },
        val onStatusClicked: (MediaStatus) -> Unit = {},
        val formats: @Composable () -> List<FormatEntry> = { emptyList() },
        val onFormatClicked: (MediaFormat) -> Unit = {},
        val genres: @Composable () -> List<GenreEntry> = { emptyList() },
        val onGenreClicked: (String) -> Unit = {},
        val tags: @Composable () -> Map<String, TagSection> = { emptyMap() },
        val onTagClicked: (Int) -> Unit = {},
        val showAdult: @Composable () -> Boolean = { false },
        val onShowAdultToggled: (Boolean) -> Unit = {},
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
                            .map(::GenreEntry)
                    },
                    tags = {
                        mapOf(
                            "CategoryOne" to TagSection.Category(
                                name = "CategoryOne",
                                children = listOf("TagOne", "TagTwo", "TagThree")
                                    .mapIndexed { index, tag ->
                                        MediaTagCollection(id = index, name = tag)
                                    }
                                    .map(TagSection::Tag)
                                    .associateBy { it.name }
                                    .toSortedMap()
                            ),
                            "Category" to TagSection.Category(
                                name = "Category",
                                children = mapOf(
                                    "Two" to TagSection.Category(
                                        name = "Two",
                                        children = listOf("TagFour", "TagFive", "TagSix")
                                            .mapIndexed { index, tag ->
                                                MediaTagCollection(id = index, name = tag)
                                            }
                                            .map(TagSection::Tag)
                                            .associateBy { it.name }
                                            .toSortedMap())
                                ).toSortedMap(),
                            ),
                        )
                    },
                    statuses = { StatusEntry.statuses() },
                    formats = { FormatEntry.formats() },
                )
            }
        }

        interface SortOption {
            @get:StringRes
            val textRes: Int
        }
    }

    data class StatusEntry(
        override val value: MediaStatus,
        override val state: IncludeExcludeState = IncludeExcludeState.DEFAULT,
    ) : MediaFilterEntry<MediaStatus> {
        companion object {
            fun statuses() = listOf(
                MediaStatus.FINISHED,
                MediaStatus.RELEASING,
                MediaStatus.NOT_YET_RELEASED,
                MediaStatus.CANCELLED,
                MediaStatus.HIATUS,
            ).map(::StatusEntry)
        }

        val textRes = value.toTextRes()
    }

    data class FormatEntry(
        override val value: MediaFormat,
        override val state: IncludeExcludeState = IncludeExcludeState.DEFAULT,
    ) : MediaFilterEntry<MediaFormat> {
        companion object {
            fun formats() = listOf(
                MediaFormat.TV,
                MediaFormat.TV_SHORT,
                MediaFormat.MOVIE,
                MediaFormat.SPECIAL,
                MediaFormat.OVA,
                MediaFormat.ONA,
                MediaFormat.MUSIC,
                // MANGA, NOVEL, and ONE_SHOT excluded since not anime
            ).map(::FormatEntry)
        }

        val textRes = value.toTextRes()
    }

    data class GenreEntry(
        override val value: String,
        override val state: IncludeExcludeState = IncludeExcludeState.DEFAULT
    ) : MediaFilterEntry<String> {
        val isAdult = value == "Hentai"

        override val leadingIconVector = MediaUtils.tagLeadingIcon(isAdult = isAdult)

        override val leadingIconContentDescription =
            MediaUtils.tagLeadingIconContentDescription(isAdult = isAdult)
    }

    sealed interface TagSection {
        val name: String

        fun filter(predicate: (Tag) -> Boolean): TagSection? = when (this) {
            is Category -> {
                children.values
                    .mapNotNull { it.filter(predicate) }
                    .associateBy { it.name }
                    .toSortedMap(String.CASE_INSENSITIVE_ORDER)
                    .takeIf { it.isNotEmpty() }
                    ?.let { copy(children = it) }
            }
            is Tag -> takeIf { predicate(it) }
        }

        data class Category(
            override val name: String,
            val children: SortedMap<String, TagSection>,
            val expanded: Boolean = false,
            val hasAnySelected: Boolean = false,
        ) : TagSection {

            fun flatten(): List<Tag> = children.values.flatMap {
                when (it) {
                    is Category -> it.flatten()
                    is Tag -> listOf(it)
                }
            }

            class Builder(private val name: String) {
                private var children = mutableMapOf<String, Any>()

                fun getOrPutCategory(name: String): Builder {
                    // Prefix to ensure tags don't conflict via name with actual child tags
                    val key = "category_$name"
                    return when (val existingSection = children[key]) {
                        is Builder -> existingSection
                        is Tag -> Builder(name)
                            .apply { children[key] = existingSection }
                            .also { children[key] = it }
                        else -> Builder(name)
                            .also { children[key] = it }
                    }
                }

                fun addChild(it: MediaTagCollection) {
                    children[it.name] = Tag(
                        id = it.id.toString(),
                        name = it.name,
                        isAdult = it.isAdult,
                        description = it.description,
                        value = it,
                    )
                }

                fun build(): Category = Category(
                    name = name,
                    children = children.mapValues { (_, value) ->
                        when (value) {
                            is Builder -> value.build()
                            is Tag -> value
                            else -> throw IllegalStateException("Unexpected value $value")
                        }
                    }.toSortedMap(String.CASE_INSENSITIVE_ORDER)
                )
            }
        }

        data class Tag(
            val id: String,
            override val name: String,
            val description: String?,
            val isAdult: Boolean?,
            override val value: MediaTagCollection,
            override val state: IncludeExcludeState = IncludeExcludeState.DEFAULT,
        ) : MediaFilterEntry<MediaTagCollection>, TagSection {
            val containerColor = MediaUtils.calculateTagColor(value.id)

            override val leadingIconVector = MediaUtils.tagLeadingIcon(
                isAdult = isAdult,
                isGeneralSpoiler = value.isGeneralSpoiler,
            )

            override val leadingIconContentDescription =
                MediaUtils.tagLeadingIconContentDescription(
                    isAdult = isAdult,
                    isGeneralSpoiler = value.isGeneralSpoiler,
                )

            constructor(tag: MediaTagCollection) : this(
                id = tag.id.toString(),
                name = tag.name,
                isAdult = tag.isAdult,
                description = tag.description,
                value = tag,
            )
        }
    }
}