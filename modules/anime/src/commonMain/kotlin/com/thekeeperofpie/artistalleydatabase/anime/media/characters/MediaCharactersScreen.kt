package com.thekeeperofpie.artistalleydatabase.anime.media.characters

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_characters_header
import artistalleydatabase.modules.anime.generated.resources.anime_characters_menu_voice_actor_language
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_more_actions_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_open_external
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_open_external_icon_content_description
import com.anilist.data.MediaAndCharactersQuery
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterCard
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object MediaCharactersScreen {

    private val MIN_IMAGE_HEIGHT = 100.dp
    private val IMAGE_WIDTH = 72.dp

    @Composable
    operator fun invoke(
        viewModel: MediaCharactersViewModel,
        sortFilterState: SortFilterState<*>,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry.result?.media

        val (voiceActorLanguageDefault) = LocalLanguageOptionVoiceActor.current
        var voiceActorLanguage by rememberSaveable { mutableStateOf(voiceActorLanguageDefault) }
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
                    val mediaType = media?.type
                    val mediaId = viewModel.mediaId
                    MediaHeader(
                        viewer = viewModel.viewer.collectAsState().value,
                        upIconOption = upIconOption,
                        mediaId = mediaId,
                        mediaType = mediaType,
                        titles = entry.result?.titlesUnique,
                        episodes = media?.episodes,
                        format = media?.format,
                        averageScore = media?.averageScore,
                        popularity = media?.popularity,
                        progress = it,
                        headerValues = headerValues,
                        onFavoriteChanged = {
                            viewModel.favoritesToggleHelper.set(
                                headerValues.type.toFavoriteType(),
                                mediaId,
                                it,
                            )
                        },
                        enableCoverImageSharedElement = false,
                        menuContent = {
                            Box {
                                var showMenu by remember { mutableStateOf(false) }
                                var showLanguageMenu by remember { mutableStateOf(false) }
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = stringResource(
                                            Res.string.anime_media_details_more_actions_content_description,
                                        ),
                                    )
                                }

                                val uriHandler = LocalUriHandler.current
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.anime_media_details_open_external)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.OpenInBrowser,
                                                contentDescription = stringResource(
                                                    Res.string.anime_media_details_open_external_icon_content_description
                                                )
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            uriHandler.openUri(
                                                AniListDataUtils.mediaUrl(
                                                    // TODO: Better infer media type
                                                    mediaType ?: MediaType.ANIME,
                                                    mediaId
                                                ) + "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                                            )
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.anime_characters_menu_voice_actor_language)) },
                                        onClick = { showLanguageMenu = true }
                                    )
                                }

                                DropdownMenu(
                                    expanded = showLanguageMenu,
                                    onDismissRequest = { showLanguageMenu = false },
                                ) {
                                    VoiceActorLanguageOption.entries.forEach {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(it.textRes)) },
                                            onClick = {
                                                voiceActorLanguage = it
                                                showMenu = false
                                                showLanguageMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            },
        ) {
            val gridState = rememberLazyGridState()
            sortFilterState.ImmediateScrollResetEffect(gridState)
            VerticalList(
                gridState = gridState,
                onRefresh = viewModel::refresh,
                itemHeaderText = Res.string.anime_characters_header,
                itemKey = { it.id },
                items = viewModel.items.collectAsLazyPagingItems(),
                item = {
                    CharacterCard(
                        imageWidth = IMAGE_WIDTH,
                        minHeight = MIN_IMAGE_HEIGHT,
                        character = it,
                        voiceActorLanguage = voiceActorLanguage,
                        staffDetailsRoute = StaffDestinations.StaffDetails.route,
                    )
                },
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                modifier = Modifier.padding(it)
            )
        }
    }

    data class Entry(
        val media: MediaAndCharactersQuery.Data.Media,
    ) {
        val titlesUnique = media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}
