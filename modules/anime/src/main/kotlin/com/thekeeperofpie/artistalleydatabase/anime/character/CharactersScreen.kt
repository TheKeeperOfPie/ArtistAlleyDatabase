package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.MediaAndCharactersQuery
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

object CharactersScreen {

    private val MIN_IMAGE_HEIGHT = 100.dp
    private val IMAGE_WIDTH = 72.dp
    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_CHARACTERS.id

    @Composable
    operator fun invoke(
        viewModel: CharactersViewModel,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry?.media

        val (voiceActorLanguageDefault) = LocalLanguageOptionVoiceActor.current
        var voiceActorLanguage by rememberSaveable { mutableStateOf(voiceActorLanguageDefault) }
        HeaderAndListScreen(
            viewModel = viewModel,
            headerTextRes = R.string.anime_characters_header,
            header = {
                val mediaType = viewModel.entry?.media?.type
                val mediaId = viewModel.headerId
                MediaHeader(
                    screenKey = SCREEN_KEY,
                    upIconOption = upIconOption,
                    mediaId = mediaId,
                    mediaType = mediaType,
                    titles = entry?.titlesUnique,
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
                                        R.string.anime_media_details_more_actions_content_description,
                                    ),
                                )
                            }

                            val uriHandler = LocalUriHandler.current
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.anime_media_details_open_external)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.OpenInBrowser,
                                            contentDescription = stringResource(
                                                R.string.anime_media_details_open_external_icon_content_description
                                            )
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        uriHandler.openUri(
                                            AniListUtils.mediaUrl(
                                                // TODO: Better infer media type
                                                mediaType ?: MediaType.ANIME,
                                                mediaId
                                            ) + "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                                        )
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.anime_characters_menu_voice_actor_language)) },
                                    onClick = { showLanguageMenu = true }
                                )
                            }

                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false },
                            ) {
                                VoiceActorLanguageOption.values().forEach {
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
            },
            itemKey = { it.id },
            item = {
                CharacterCard(
                    screenKey = SCREEN_KEY,
                    character = it,
                    imageWidth = IMAGE_WIDTH,
                    minHeight = MIN_IMAGE_HEIGHT,
                    voiceActorLanguage = voiceActorLanguage,
                )
            },
        )
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
