package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.ignore.generated.resources.Res
import artistalleydatabase.modules.anime.ignore.generated.resources.anime_ignore_header
import artistalleydatabase.modules.anime.ignore.generated.resources.anime_ignore_not_enabled_button
import artistalleydatabase.modules.anime.ignore.generated.resources.anime_ignore_not_enabled_prompt
import artistalleydatabase.modules.anime.ignore.generated.resources.anime_ignore_tab_anime
import artistalleydatabase.modules.anime.ignore.generated.resources.anime_ignore_tab_manga
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_icon_content_description
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.media.data.generated.resources.Res as MediaDataRes

@OptIn(ExperimentalMaterial3Api::class)
object AnimeIgnoreScreen {

    @Composable
    operator fun <MediaEntry : Any> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        state: State<MediaEntry>,
        upIconOption: UpIconOption?,
        onRefresh: () -> Unit,
        itemKey: (MediaEntry) -> String,
        mediaViewOptionRow: @Composable (
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            Scaffold(
                topBar = { TopBar(state, upIconOption, scrollBehavior) },
                modifier = Modifier.padding(padding)
            ) { scaffoldPadding ->
                val enabled by state.enabled.collectAsState()
                if (!enabled) {
                    NotEnabledPrompt(state, Modifier.Companion.padding(scaffoldPadding))
                } else {
                    val mediaViewOption by state.mediaViewOption.collectAsStateWithLifecycle()
                    val columns = mediaViewOption.widthAdaptiveCells
                    val content = state.content.collectAsLazyPagingItems()
                    VerticalList(
                        itemHeaderText = null,
                        items = content,
                        itemKey = itemKey,
                        onRefresh = onRefresh,
                        columns = columns,
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 32.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                        modifier = Modifier.Companion.padding(scaffoldPadding)
                    ) {
                        mediaViewOptionRow(it, onClickListEdit)
                    }
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        state: State<*>,
        upIconOption: UpIconOption?,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(Res.string.anime_ignore_header)) },
                    navigationIcon = { upIconOption?.let { UpIconButton(upIconOption) } },
                    actions = {
                        var mediaViewOption by state.mediaViewOption.collectAsMutableStateWithLifecycle()
                        val nextMediaViewOption = MediaViewOption.entries
                            .let { it[(it.indexOf(mediaViewOption) + 1) % it.size] }
                        IconButton(onClick = {
                            mediaViewOption = nextMediaViewOption
                        }) {
                            Icon(
                                imageVector = nextMediaViewOption.icon,
                                contentDescription = stringResource(
                                    MediaDataRes.string.anime_media_view_option_icon_content_description
                                ),
                            )
                        }
                    },
                )

                var selectedType by state.selectedType.collectAsMutableStateWithLifecycle()
                val selectedIsAnime = selectedType == MediaType.ANIME
                TabRow(
                    selectedTabIndex = if (selectedIsAnime) 0 else 1,
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .align(Alignment.Companion.CenterHorizontally)
                ) {
                    Tab(
                        selected = selectedIsAnime,
                        onClick = { selectedType = MediaType.ANIME },
                        text = { Text(stringResource(Res.string.anime_ignore_tab_anime)) },
                    )
                    Tab(
                        selected = !selectedIsAnime,
                        onClick = { selectedType = MediaType.MANGA },
                        text = { Text(stringResource(Res.string.anime_ignore_tab_manga)) },
                    )
                }
            }
        }
    }

    @Composable
    private fun NotEnabledPrompt(state: State<*>, modifier: Modifier) {
        Column(
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.anime_ignore_not_enabled_prompt),
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.padding(32.dp)
            )
            Button(onClick = { state.enabled.value = true }) {
                Text(stringResource(Res.string.anime_ignore_not_enabled_button))
            }
        }
    }

    @Stable
    class State<MediaEntry : Any>(
        val mediaViewOption: MutableStateFlow<MediaViewOption>,
        val selectedType: MutableStateFlow<MediaType>,
        val enabled: MutableStateFlow<Boolean>,
        val content: MutableStateFlow<PagingData<MediaEntry>>,
    )
}
