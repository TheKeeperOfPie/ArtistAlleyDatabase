package com.thekeeperofpie.artistalleydatabase.anime.media

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.MediaTagsQuery.Data.MediaTagCollection
import com.anilist.type.MediaFormat
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.SortedMap
import kotlin.reflect.KClass

class AnimeMediaFilterController<T>(
    sortEnumClass: KClass<T>,
    private val aniListApi: AuthedAniListApi,
) where T : AnimeMediaFilterController.Data.SortOption, T : Enum<*> {

    companion object {
        private const val TAG = "AnimeMediaFilterController"
    }

    val sortOptions = MutableStateFlow(SortEntry.options(sortEnumClass))
    val sortAscending = MutableStateFlow(false)

    val genres = MutableStateFlow(emptyList<GenreEntry>())
    val tagsByCategory = MutableStateFlow(emptyMap<String, TagSection>())
    val statuses = MutableStateFlow(StatusEntry.statuses())
    val formats = MutableStateFlow(FormatEntry.formats())
    val showAdult = MutableStateFlow(false)

    val onListOptions = MutableStateFlow(OnListOption.options())
    val averageScoreRange = MutableStateFlow(RangeData(100, hardMax = true))
    val episodesRange = MutableStateFlow(RangeData(151))
    val sources = MutableStateFlow(SourceEntry.sources())

    private val airingDate = MutableStateFlow(AiringDate.Basic() to AiringDate.Advanced())
    private val airingDateIsAdvanced = MutableStateFlow(false)

    private lateinit var initialParams: InitialParams

    @OptIn(ExperimentalCoroutinesApi::class)
    fun initialize(
        viewModel: ViewModel,
        refreshUpdates: StateFlow<*>,
        params: InitialParams = InitialParams(),
    ) {
        if (::initialParams.isInitialized) return
        initialParams = params

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
                                ?.run {
                                    if (initialParams.tagId == null) return@run this
                                    toMutableMap().apply {
                                        replaceAll { _, section ->
                                            section.replace {
                                                it.takeUnless { it.id == initialParams.tagId }
                                                    ?: it.copy(
                                                        state = IncludeExcludeState.INCLUDE,
                                                        clickable = false
                                                    )
                                            }
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            Log.d(TAG, "Error loading tags", e)
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

    fun airingDate() = combine(airingDate, airingDateIsAdvanced, ::Pair)
        .map { (airingDatePair, advanced) ->
            if (advanced) airingDatePair.second else airingDatePair.first
        }

    /**
     * Categories are provided from the API in the form of "Parent-Child". This un-flattens the
     * tag list into a tree of sections, separated by the "-" dash.
     */
    private fun buildTagSections(tags: List<MediaTagCollection>): Map<String, TagSection> {
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
        }
    }

    private fun onSortClicked(option: T) {
        sortOptions.value = sortOptions.value.toMutableList()
            .apply {
                replaceAll {
                    if (it.value == option) {
                        val newState = if (it.state != IncludeExcludeState.INCLUDE) {
                            IncludeExcludeState.INCLUDE
                        } else {
                            IncludeExcludeState.DEFAULT
                        }
                        it.copy(state = newState)
                    } else it.copy(state = IncludeExcludeState.DEFAULT)
                }
            }
    }

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

    private fun onTagClicked(tagId: String) {
        if (tagId == initialParams.tagId) return
        tagsByCategory.value = tagsByCategory.value
            .mapValues { (_, value) ->
                value.replace {
                    it.takeUnless { it.id == tagId }
                        ?: it.copy(state = it.state.next())
                }
            }
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

    private fun onSeasonChanged(season: MediaSeason?) {
        val value = airingDate.value
        airingDate.value = value.copy(first = value.first.copy(season = season))
    }

    private fun onSeasonYearChanged(seasonYear: String) {
        val value = airingDate.value
        airingDate.value = value.copy(first = value.first.copy(seasonYear = seasonYear))
    }

    private fun onAiringDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
        }

        val value = airingDate.value
        airingDate.value = value.copy(
            second = if (start) {
                value.second.copy(startDate = selectedDate)
            } else {
                value.second.copy(endDate = selectedDate)
            }
        )
    }

    private fun onOnListClicked(option: OnListOption) {
        onListOptions.value = onListOptions.value.toMutableList()
            .apply {
                replaceAll {
                    if (it.value == option.value) {
                        val newState = if (it.state != IncludeExcludeState.INCLUDE) {
                            IncludeExcludeState.INCLUDE
                        } else {
                            IncludeExcludeState.DEFAULT
                        }
                        it.copy(state = newState)
                    } else it.copy(state = IncludeExcludeState.DEFAULT)
                }
            }
    }

    private fun onAverageScoreChanged(start: String, end: String) {
        averageScoreRange.value = averageScoreRange.value.copy(startString = start, endString = end)
    }

    private fun onEpisodesChanged(start: String, end: String) {
        episodesRange.value = episodesRange.value.copy(
            startString = start,
            endString = end.takeIf { it != "150" }.orEmpty(),
        )
    }

    private fun onSourceClicked(source: MediaSource) {
        sources.value = sources.value.toMutableList()
            .apply {
                replaceAll {
                    if (it.value == source) {
                        val newState = if (it.state != IncludeExcludeState.INCLUDE) {
                            IncludeExcludeState.INCLUDE
                        } else {
                            IncludeExcludeState.DEFAULT
                        }
                        it.copy(state = newState)
                    } else it
                }
            }
    }

    fun data() = Data(
        sortOptions = { sortOptions.collectAsState().value },
        onSortClicked = ::onSortClicked,
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
        airingDate = {
            airingDate.collectAsState().value.let {
                if (airingDateIsAdvanced.collectAsState().value) it.second else it.first
            }
        },
        onAiringDateIsAdvancedToggled = { airingDateIsAdvanced.value = it },
        onAiringDateChange = ::onAiringDateChange,
        onSeasonChanged = ::onSeasonChanged,
        onSeasonYearChanged = ::onSeasonYearChanged,
        onListEnabled = { initialParams.onListEnabled },
        onListOptions = { onListOptions.collectAsState().value },
        onOnListClicked = ::onOnListClicked,
        averageScoreRange = { averageScoreRange.collectAsState().value },
        onAverageScoreChanged = ::onAverageScoreChanged,
        episodesRange = { episodesRange.collectAsState().value },
        onEpisodesChanged = ::onEpisodesChanged,
        sources = { sources.collectAsState().value },
        onSourceClicked = ::onSourceClicked,
        showAdult = { showAdult.collectAsState().value },
        onShowAdultToggled = { showAdult.value = it },
    )

    data class InitialParams(
        val onListEnabled: Boolean = true,
        val tagId: String? = null,
    )

    class Data<SortOption : Data.SortOption>(
        val sortOptions: @Composable () -> List<SortEntry<SortOption>>,
        val onSortClicked: (SortOption) -> Unit = {},
        val sortAscending: @Composable () -> Boolean = { false },
        val onSortAscendingChanged: (Boolean) -> Unit = {},
        val statuses: @Composable () -> List<StatusEntry> = { emptyList() },
        val onStatusClicked: (MediaStatus) -> Unit = {},
        val formats: @Composable () -> List<FormatEntry> = { emptyList() },
        val onFormatClicked: (MediaFormat) -> Unit = {},
        val genres: @Composable () -> List<GenreEntry> = { emptyList() },
        val onGenreClicked: (String) -> Unit = {},
        val tags: @Composable () -> Map<String, TagSection> = { emptyMap() },
        val onTagClicked: (String) -> Unit = {},
        val airingDate: @Composable () -> AiringDate = { AiringDate.Basic() },
        val onSeasonChanged: (MediaSeason?) -> Unit = {},
        val onSeasonYearChanged: (String) -> Unit = {},
        val onAiringDateIsAdvancedToggled: (Boolean) -> Unit = {},
        val onAiringDateChange: (start: Boolean, selectedMillis: Long?) -> Unit = { _, _ -> },
        val onListEnabled: () -> Boolean = { true },
        val onListOptions: @Composable () -> List<OnListOption> = { OnListOption.options() },
        val onOnListClicked: (OnListOption) -> Unit = {},
        val averageScoreRange: @Composable () -> RangeData = { RangeData(100) },
        val onAverageScoreChanged: (start: String, end: String) -> Unit = { _, _ -> },
        val episodesRange: @Composable () -> RangeData = { RangeData(151) },
        val onEpisodesChanged: (start: String, end: String) -> Unit = { _, _ -> },
        val sources: @Composable () -> List<SourceEntry> = { emptyList() },
        val onSourceClicked: (MediaSource) -> Unit = {},
        val showAdult: @Composable () -> Boolean = { false },
        val onShowAdultToggled: (Boolean) -> Unit = {},
    ) {
        companion object {
            inline fun <reified T> forPreview(): Data<T>
                    where T : SortOption, T : Enum<*> {
                val enumConstants = T::class.java.enumConstants!!.toList()
                return Data(
                    sortOptions = { enumConstants.map(::SortEntry) },
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

    data class SortEntry<T : Data.SortOption>(
        override val value: T,
        override val state: IncludeExcludeState = IncludeExcludeState.DEFAULT,
    ) : MediaFilterEntry<T> {
        companion object {
            fun <T : Data.SortOption> options(enumClass: KClass<T>) =
                enumClass.java.enumConstants!!.map(::SortEntry)
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

        fun replace(block: (Tag) -> Tag): TagSection = when (this) {
            is Category -> {
                copy(children = children.mapValues { (_, value) -> value.replace(block) }
                    .toSortedMap(String.CASE_INSENSITIVE_ORDER))
            }
            is Tag -> block(this)
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
            val clickable: Boolean = true,
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

    data class OnListOption(
        override val value: Boolean,
        override val state: IncludeExcludeState = IncludeExcludeState.DEFAULT,
    ) : MediaFilterEntry<Boolean?> {
        companion object {
            fun options() = listOf(true, false).map(::OnListOption)
        }

        val textRes = when (value) {
            true -> R.string.anime_media_filter_on_list_on_list
            false -> R.string.anime_media_filter_on_list_not_on_list
        }
    }

    sealed interface AiringDate {

        data class Basic(
            val season: MediaSeason? = null,
            val seasonYear: String = "",
        ) : AiringDate

        data class Advanced(
            val startDate: LocalDate? = null,
            val endDate: LocalDate? = null,
        ) : AiringDate
    }

    data class RangeData(
        val maxValue: Int = 100,
        val hardMax: Boolean = false,
        val startString: String = "0",
        val endString: String = if (hardMax) maxValue.toString() else "",
    ) {
        val startInt = startString.toIntOrNull()?.takeIf { it > 0 }?.let {
            if (hardMax) it.coerceAtMost(maxValue) else it
        }
        val endInt = endString.toIntOrNull()?.takeIf { it > 0 }?.let {
            if (hardMax) it.coerceAtMost(maxValue) else it
        }

        val apiStart = startInt?.takeIf { it > 0 }
        val apiEnd = endInt?.takeIf { it != maxValue || !hardMax }?.let { it + 1 }

        val summaryText = if (startInt != null && endInt != null) {
            if (startInt == endInt) {
                startInt.toString()
            } else if (endInt == maxValue && hardMax) {
                "≥ $startInt"
            } else {
                "$startString - $endString"
            }
        } else if (startInt != null) {
            "≥ $startInt"
        } else if (endInt != null) {
            if (hardMax && endInt == maxValue) null else "≤ $endInt"
        } else null

        val value = if (startInt != null && endInt != null) {
            startInt.coerceAtMost(maxValue).toFloat()..endInt.coerceAtMost(maxValue).toFloat()
        } else if (startInt != null) {
            startInt.coerceAtMost(maxValue).toFloat()..maxValue.toFloat()
        } else if (endInt != null) {
            0f..endInt.toFloat()
        } else {
            0f..maxValue.toFloat()
        }

        val valueRange = 0f..maxValue.toFloat()
    }

    data class SourceEntry(
        override val value: MediaSource,
        override val state: IncludeExcludeState = IncludeExcludeState.DEFAULT,
    ) : MediaFilterEntry<MediaSource> {
        companion object {
            fun sources() = listOf(
                MediaSource.ORIGINAL,
                MediaSource.ANIME,
                MediaSource.COMIC,
                MediaSource.DOUJINSHI,
                MediaSource.GAME,
                MediaSource.LIGHT_NOVEL,
                MediaSource.LIVE_ACTION,
                MediaSource.MANGA,
                MediaSource.MULTIMEDIA_PROJECT,
                MediaSource.NOVEL,
                MediaSource.OTHER,
                MediaSource.PICTURE_BOOK,
                MediaSource.VIDEO_GAME,
                MediaSource.VISUAL_NOVEL,
                MediaSource.WEB_NOVEL,
            ).map(::SourceEntry)
        }

        @StringRes
        val textRes = value.toTextRes()
    }
}