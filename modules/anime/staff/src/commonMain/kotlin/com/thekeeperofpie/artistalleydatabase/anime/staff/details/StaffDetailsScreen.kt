package com.thekeeperofpie.artistalleydatabase.anime.staff.details

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.anilist.data.StaffDetailsQuery
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeader
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object StaffDetailsScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        staffId: String,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        upIconOption: UpIconOption?,
        onRefresh: () -> Unit,
        headerValues: StaffHeaderValues,
        sharedTransitionKey: SharedTransitionKey?,
        entry: () -> LoadingResult<Entry>,
        mediaTimeline: StateFlow<StaffMediaTimeline>,
        staffTimeline: StateFlow<StaffTimeline<MediaEntry>>,
        characters: @Composable () -> LazyPagingItems<CharacterDetails>,
        favorite: () -> Boolean?,
        onFavoriteChanged: (Boolean) -> Unit,
        onRequestMediaYear: (Int?) -> Unit,
        onRequestStaffYear: (Int?) -> Unit,
        charactersSection: LazyGridScope.(
            titleRes: StringResource,
            viewAllRoute: () -> NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
            characters: LazyPagingItems<CharacterDetails>,
        ) -> Unit,
        characterCard: @Composable LazyItemScope.(StaffMediaTimeline.Character) -> Unit,
        mediaGridCard: @Composable (
            StaffTimeline.MediaWithRole<MediaEntry>,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        val expandedState = rememberExpandedState()
        val coverImageState = rememberCoilImageState(headerValues.coverImage)

        val snackbarHostState = remember { SnackbarHostState() }
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            Scaffold(
                modifier = Modifier.padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                            coverImageState = coverImageState,
                            onFavoriteChanged = onFavoriteChanged,
                        )
                    }
                }
            ) { scaffoldPadding ->
                Column(modifier = Modifier.padding(scaffoldPadding)) {
                    val entryLoadingResult = entry()
                    val error = entryLoadingResult.error
                    val errorText = entryLoadingResult.error?.messageText()
                    val entry = entryLoadingResult.result
                    LaunchedEffect(entry, error, errorText) {
                        if (entry != null && errorText != null) {
                            snackbarHostState.showSnackbar(
                                message = errorText,
                                withDismissAction = true,
                                duration = SnackbarDuration.Long,
                            )
                        }
                    }
                    if (entry == null) {
                        if (entryLoadingResult.loading) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(32.dp)
                                )
                            }
                        } else {
                            VerticalList.ErrorContent(
                                errorText = errorText.orEmpty(),
                                exception = error?.throwable,
                                onRetry = onRefresh,
                            )
                        }
                    } else {
                        val mediaTimeline by mediaTimeline.collectAsState()
                        val staffTimeline by staffTimeline.collectAsState()
                        val staffTabs = StaffTab.entries
                            .filter { it != StaffTab.MEDIA || mediaTimeline.yearsToCharacters.isNotEmpty() }
                            .filter { it != StaffTab.STAFF || staffTimeline.yearsToMedia.isNotEmpty() }
                        val pagerState = rememberPagerState(pageCount = { staffTabs.size })
                        val scope = rememberCoroutineScope()
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                            divider = { /* No divider, manually draw so that it's full width */ }
                        ) {
                            staffTabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                    text = {
                                        Text(
                                            text = stringResource(tab.textRes),
                                            maxLines = 1
                                        )
                                    }
                                )
                            }
                        }

                        HorizontalDivider()

                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = false,
                            pageNestedScrollConnection = scrollBehavior.nestedScrollConnection,
                        ) {
                            val characters = characters()
                            when (staffTabs[it]) {
                                StaffTab.OVERVIEW -> StaffOverviewScreen(
                                    entry = entry,
                                    coverImageState = coverImageState,
                                    expandedState = expandedState,
                                    favorite = favorite,
                                    charactersSection = { titleRes, viewAllRoute, viewAllContentDescriptionTextRes ->
                                        charactersSection(
                                            titleRes,
                                            viewAllRoute,
                                            viewAllContentDescriptionTextRes,
                                            characters,
                                        )
                                    }
                                )
                                StaffTab.MEDIA -> StaffMediaScreen(
                                    mediaTimeline = mediaTimeline,
                                    onRequestYear = onRequestMediaYear,
                                    characterCard = characterCard,
                                )
                                StaffTab.STAFF -> StaffStaffScreen(
                                    staffTimeline = staffTimeline,
                                    onRequestStaffYear = onRequestStaffYear,
                                    mediaGridCard = {
                                        mediaGridCard(it, onClickListEdit)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    data class Entry(
        override val staff: StaffDetailsQuery.Data.Staff,
        override val description: MarkdownText?,
    ) : StaffOverviewScreen.Entry

    @Composable
    private fun rememberExpandedState() = rememberSaveable(
        saver = listSaver(
            save = {
                listOf(
                    it.description,
                )
            },
            restore = {
                StaffExpandedState(
                    description = it[0],
                )
            }
        )) {
        StaffExpandedState()
    }
}
