package com.thekeeperofpie.artistalleydatabase.anime.characters.media

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_medias_header
import com.anilist.data.CharacterAndMediasQuery
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterHeader
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems

// TODO: Shared transition from CharacterDetails could be better
@OptIn(ExperimentalMaterial3Api::class)
object CharacterMediasScreen {

    @Composable
    operator fun <MediaEntry : Any> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        sortFilterState: SortFilterState<*>,
        characterId: String,
        headerValues: CharacterHeaderValues,
        sharedTransitionKey: SharedTransitionKey?,
        viewer: () -> AniListViewer?,
        onRefresh: () -> Unit,
        items: LazyPagingItems<MediaEntry>,
        itemKey: (MediaEntry) -> Any,
        onFavoriteChanged: (Boolean) -> Unit,
        mediaRow: @Composable (
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
    ) {
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
            SortFilterBottomScaffold(
                state = sortFilterState,
                topBar = {
                    CollapsingToolbar(
                        maxHeight = 356.dp,
                        pinnedHeight = 120.dp,
                        scrollBehavior = scrollBehavior,
                    ) {
                        val navigationController = LocalNavigationController.current
                        CharacterHeader(
                            viewer = viewer(),
                            upIconOption = UpIconOption.Back(navigationController),
                            characterId = characterId,
                            progress = it,
                            headerValues = headerValues,
                            sharedTransitionKey = sharedTransitionKey,
                            onFavoriteChanged = onFavoriteChanged,
                        )
                    }
                },
                modifier = Modifier.padding(padding)
            ) {
                val gridState = rememberLazyGridState()
                sortFilterState.ImmediateScrollResetEffect(gridState)
                VerticalList(
                    gridState = gridState,
                    onRefresh = onRefresh,
                    itemHeaderText = Res.string.anime_character_medias_header,
                    itemKey = itemKey,
                    items = items,
                    item = {
                        mediaRow(
                            it,
                            onClickListEdit,
                            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        )
                    },
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    modifier = Modifier.padding(it)
                )
            }
        }
    }

    data class Entry(
        val character: CharacterAndMediasQuery.Data.Character,
    )
}
