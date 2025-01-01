package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_characters_header
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeader
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems

@OptIn(ExperimentalMaterial3Api::class)
object StaffCharactersScreen {

    @Composable
    operator fun <CharacterEntry : Any> invoke(
        staffId: String,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        sortFilterState: SortFilterState<*>,
        onRefresh: () -> Unit,
        characters: LazyPagingItems<CharacterEntry>,
        characterItemKey: (CharacterEntry) -> String,
        characterRow: @Composable (
            CharacterEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        upIconOption: UpIconOption?,
        headerValues: StaffHeaderValues,
        sharedTransitionKey: SharedTransitionKey?,
        onFavoriteChanged: (Boolean) -> Unit,
    ) {
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold2(
                state = sortFilterState,
                topBar = {
                    CollapsingToolbar(
                        maxHeight = 356.dp,
                        pinnedHeight = 120.dp,
                        scrollBehavior = scrollBehavior,
                    ) {
                        StaffHeader(
                            staffId = staffId,
                            upIconOption = upIconOption,
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
                    itemHeaderText = Res.string.anime_staff_characters_header,
                    itemKey = characterItemKey,
                    items = characters,
                    item = {
                        SharedTransitionKeyScope(
                            "anime_staff_character_media_card",
                            it?.let(characterItemKey),
                        ) {
                            characterRow(it, onClickListEdit)
                        }
                    },
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    modifier = Modifier.padding(it)
                )
            }
        }
    }
}
