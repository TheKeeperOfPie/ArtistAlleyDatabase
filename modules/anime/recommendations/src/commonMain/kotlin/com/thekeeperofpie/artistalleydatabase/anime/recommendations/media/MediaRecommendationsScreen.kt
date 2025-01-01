package com.thekeeperofpie.artistalleydatabase.anime.recommendations.media

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.recommendations.generated.resources.Res
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendations_header
import com.anilist.data.MediaAndRecommendationsQuery
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems

@OptIn(ExperimentalMaterial3Api::class)
object MediaRecommendationsScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        sortFilterState: SortFilterState<*>,
        onRefresh: () -> Unit,
        items: LazyPagingItems<MediaRecommendationEntry<MediaEntry>>,
        mediaHeader: @Composable (progress: Float) -> Unit,
        mediaRow: @Composable (MediaRecommendationEntry<MediaEntry>?, Modifier) -> Unit,
        modifier: Modifier,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        SortFilterBottomScaffold2(
            state = sortFilterState,
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    mediaHeader(it)
                }
            },
            modifier = modifier
        ) {
            val gridState = rememberLazyGridState()
            sortFilterState.ImmediateScrollResetEffect(gridState)
            VerticalList(
                gridState = gridState,
                onRefresh = onRefresh,
                itemHeaderText = Res.string.anime_recommendations_header,
                itemKey = { it.recommendation.id },
                items = items,
                item = {
                    mediaRow(
                        it,
                        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                },
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                modifier = Modifier.padding(it)
            )
        }
    }

    data class Entry(
        val media: MediaAndRecommendationsQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}
