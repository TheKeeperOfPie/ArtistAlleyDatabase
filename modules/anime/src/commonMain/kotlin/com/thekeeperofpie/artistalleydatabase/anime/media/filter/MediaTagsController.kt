package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.MediaTagsQuery
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@SingletonScope
@Inject
class MediaTagsController(scope: ApplicationScope, aniListApi: AuthedAniListApi) {

    private val refresh = MutableStateFlow(-1L)

    val tags = refresh.mapLatest {
        aniListApi.tags().mediaTagCollection
            ?.filterNotNull()
            ?.let(::buildTagSections)
            .orEmpty()
    }
        .catch { emit(emptyMap()) }
        .flowOn(CustomDispatchers.IO)
        .shareIn(scope, SharingStarted.Lazily, replay = 1)

    fun refresh() {
        refresh.value = Clock.System.now().toEpochMilliseconds()
    }

    /**
     * Categories are provided from the API in the form of "Parent-Child". This un-flattens the
     * tag list into a tree of sections, separated by the "-" dash.
     */
    private fun buildTagSections(
        tags: List<MediaTagsQuery.Data.MediaTagCollection>,
    ): Map<String, TagSection> {
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
}
