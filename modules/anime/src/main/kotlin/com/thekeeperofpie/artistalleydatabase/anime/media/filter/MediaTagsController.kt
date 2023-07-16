package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.MediaTagsQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MediaTagsController(scopedApplication: ScopedApplication, aniListApi: AuthedAniListApi) {

    val tags = MutableStateFlow(emptyMap<String, TagSection>())

    val refreshUptimeMillis = MutableStateFlow(-1L)

    init {
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            refreshUptimeMillis.mapLatest {
                aniListApi.tags().mediaTagCollection
                    ?.filterNotNull()
                    ?.let(::buildTagSections)
                    .orEmpty()
            }
                .collectLatest(tags::emit)
        }
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
