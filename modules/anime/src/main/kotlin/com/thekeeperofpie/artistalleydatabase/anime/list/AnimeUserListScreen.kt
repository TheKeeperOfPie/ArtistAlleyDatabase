package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.UserMediaListQuery.Data.MediaListCollection.List.Entry.Media
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterOptionsBottomPanel
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeUserListScreen {

    @Composable
    operator fun <SortOption : AnimeMediaFilterController.Data.SortOption> invoke(
        onClickNav: () -> Unit = {},
        showDrawerHandle: Boolean = true,
        viewModel: ViewModel<SortOption>,
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        AnimeMediaFilterOptionsBottomPanel(
            topBar = {
                EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                    BackHandler(viewModel.query.isNotEmpty() && !WindowInsets.isImeVisible) {
                        viewModel.query = ""
                    }
                    TextField(
                        value = viewModel.query,
                        placeholder = { Text(stringResource(id = R.string.anime_user_list_search)) },
                        onValueChange = { viewModel.query = it },
                        leadingIcon = {
                            if (showDrawerHandle) {
                                NavMenuIconButton(onClickNav)
                            } else {
                                ArrowBackIconButton(onClickNav)
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.query = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(
                                        R.string.anime_search_clear
                                    ),
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            },
            filterData = viewModel::filterData,
            onTagLongClick = viewModel::onTagLongClick,
            showLoadSave = false,
            bottomNavigationState = bottomNavigationState,
        ) { scaffoldPadding ->
            val content = viewModel.content
            val refreshing = when (content) {
                ContentState.LoadingEmpty -> true
                is ContentState.Success -> content.loading
                is ContentState.Error -> false
            }

            AnimeMediaListScreen(
                refreshing = refreshing,
                onRefresh = viewModel::onRefresh,
                tagShown = viewModel::tagShown,
                onTagDismiss = { viewModel.tagShown = null },
                pullRefreshTopPadding = {
                    scrollBehavior.state.heightOffsetLimit
                        .takeUnless { it == -Float.MAX_VALUE }
                        ?.let { LocalDensity.current.run { -it.toDp() } }
                        ?: 0.dp
                },
                modifier = Modifier.nestedScroll(
                    NestedScrollSplitter(
                        bottomNavigationState?.nestedScrollConnection,
                        scrollBehavior.nestedScrollConnection,
                        consumeNone = bottomNavigationState == null,
                    )
                )
            ) { onLongPressImage ->
                when (content) {
                    is ContentState.Error -> AnimeMediaListScreen.Error(
                        content.errorRes,
                        content.exception,
                    )
                    ContentState.LoadingEmpty ->
                        // Empty column to allow pull refresh to work
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                        }
                    is ContentState.Success -> {
                        if (content.entries.isEmpty()) {
                            AnimeMediaListScreen.NoResults()
                        } else {
                            LazyColumn(
                                state = scrollStateSaver.lazyListState(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = scrollBehavior.state.heightOffsetLimit
                                        .takeUnless { it == -Float.MAX_VALUE }
                                        ?.let { LocalDensity.current.run { -it.toDp() } }
                                        ?: 0.dp,
                                    bottom = scaffoldPadding.calculateBottomPadding()
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                items(content.entries, { it.id.scopedId }) {
                                    when (it) {
                                        is Entry.Header -> Header(it)
                                        is Entry.Item -> AnimeMediaListRow(
                                            entry = it,
                                            onTagLongClick = viewModel::onTagLongClick,
                                            onLongPressImage = onLongPressImage,
                                            colorCalculationState = colorCalculationState,
                                            navigationCallback = navigationCallback,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Header(header: Entry.Header) {
        Text(
            text = header.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 24.dp, end = 24.dp, bottom = 10.dp)
        )
    }

    sealed interface Entry {

        val id: EntryId

        data class Header(val name: String, val status: MediaListStatus?) : Entry {
            override val id = EntryId("header", name)
        }

        class Item(media: Media) : Entry, AnimeMediaListRow.MediaEntry<Media>(media)
    }

    sealed interface ContentState {
        object LoadingEmpty : ContentState

        data class Success(
            val entries: List<Entry>,
            val loading: Boolean = false,
        ) : ContentState

        data class Error(
            @StringRes val errorRes: Int? = null,
            val exception: Throwable? = null
        ) : ContentState
    }

    interface ViewModel<SortOption : AnimeMediaFilterController.Data.SortOption> {
        var query: String
        val content: ContentState
        var tagShown: AnimeMediaFilterController.TagSection.Tag?
        val colorMap: MutableMap<String, Pair<Color, Color>>

        fun filterData(): AnimeMediaFilterController.Data<SortOption>
        fun onRefresh()
        fun onTagLongClick(tagId: String)
    }
}

@Preview
@Composable
private fun Preview() {
    val viewModel = hiltViewModel<AnimeUserListViewModel>().apply {
        content = AnimeUserListScreen.ContentState.Success(
            listOf(
                AnimeUserListScreen.Entry.Header("Completed", MediaListStatus.COMPLETED),
                AnimeUserListScreen.Entry.Item(
                    Media(
                        title = Media.Title(
                            userPreferred = "Ano Hi Mita Hana no Namae wo Bokutachi wa Mada Shiranai.",
                        ),
                    )
                )
            )
        )
    }
    AnimeUserListScreen(viewModel = viewModel)
}
