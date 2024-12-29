package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.data.MediaTagsQuery
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaTagSection
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@SingletonScope
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
                tags.values.mapNotNull { it.filter { it.isAdult == false } }
                    .associateBy { it.name }
                    .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            }
        }
        .flowOn(CustomDispatchers.IO)
        .stateIn(scope, SharingStarted.Lazily, emptyMap())

    fun refresh() = refresh.refresh()

    /**
     * Categories are provided from the API in the form of "Parent-Child". This un-flattens the
     * tag list into a tree of sections, separated by the "-" dash.
     */
    private fun buildTagSections(
        tags: List<MediaTagsQuery.Data.MediaTagCollection>,
    ): Map<String, MediaTagSection> {
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

            var currentCategory: MediaTagSection.Category.Builder? = null
            categories?.forEach {
                currentCategory = if (currentCategory == null) {
                    sections.getOrPut(it) { MediaTagSection.Category.Builder(it) }
                            as MediaTagSection.Category.Builder
                } else {
                    (currentCategory as MediaTagSection.Category.Builder).getOrPutCategory(it)
                }
            }

            if (currentCategory == null) {
                sections[it.name] = MediaTagSection.Tag(it)
            } else {
                currentCategory!!.addChild(it)
            }
        }

        return sections.mapValues { (_, value) ->
            when (value) {
                is MediaTagSection.Category.Builder -> value.build()
                is MediaTagSection.Tag -> value
                else -> throw IllegalStateException("Unexpected value $value")
            }
        }
    }
}
