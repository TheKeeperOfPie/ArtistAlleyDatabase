package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.data.MediaTagsQuery
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaTagEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.TagEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
@Inject
class MediaTagsController(scope: ApplicationScope, aniListApi: AuthedAniListApi, settings: MediaDataSettings) {

    private val refresh = RefreshFlow()

    val tags = refresh.updates
        .mapLatest {
            aniListApi.tags().mediaTagCollection
                ?.filterNotNull()
                ?.let(::buildTagSections)
                .orEmpty()
        }
        .catch { emit(emptyMap()) }
        .flatMapLatest { tags ->
            settings.showAdult.map { showAdult ->
                if (showAdult) return@map tags
                tags.values.mapNotNull { it.filter { (it as MediaTagEntry.Tag).isAdult == false } }
                    .associateBy { (it as MediaTagEntry).name }
                    .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            }
                .map { it.entries.map { it.toPair() } } // TODO: Remove conversion
        }
        .flowOn(CustomDispatchers.IO)
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    fun refresh() = refresh.refresh()

    /**
     * Categories are provided from the API in the form of "Parent-Child". This un-flattens the
     * tag list into a tree of sections, separated by the "-" dash.
     */
    private fun buildTagSections(
        tags: List<MediaTagsQuery.Data.MediaTagCollection>,
    ): Map<String, TagEntry> {
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

            var currentCategory: MediaTagEntry.Category.Builder? = null
            categories?.forEach {
                currentCategory = currentCategory?.getOrPutCategory(it)
                    ?: sections.getOrPut(it) { MediaTagEntry.Category.Builder(it) }
                            as MediaTagEntry.Category.Builder
            }

            if (currentCategory == null) {
                sections[it.name] = MediaTagEntry.Tag(it)
            } else {
                currentCategory.addChild(it)
            }
        }

        return sections.mapValues { (_, value) ->
            when (value) {
                is MediaTagEntry.Category.Builder -> value.build()
                is MediaTagEntry.Tag -> value
                else -> throw IllegalStateException("Unexpected value $value")
            }
        }
    }
}
