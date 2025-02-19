package com.thekeeperofpie.artistalleydatabase.anime.characters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_more_actions_content_description
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_open_external
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_details_open_external_icon_content_description
import com.anilist.data.fragment.CharacterHeaderData
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedBounds
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.maybeOverride
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Composable
fun CharacterHeader(
    viewer: AniListViewer?,
    upIconOption: UpIconOption?,
    characterId: String,
    progress: Float,
    headerValues: CharacterHeaderValues,
    sharedTransitionKey: SharedTransitionKey?,
    coverImageState: CoilImageState = rememberCoilImageState(headerValues.coverImage),
    onFavoriteChanged: (Boolean) -> Unit,
) {
    CoverAndBannerHeader(
        upIconOption = upIconOption,
        headerValues = headerValues,
        coverImageState = coverImageState,
        sharedTransitionKey = sharedTransitionKey,
        coverImageSharedTransitionIdentifier = "character_image",
        bannerImageSharedTransitionIdentifier = "character_banner_image",
        progress = progress,
        menuContent = if (viewer == null) null else {
            {
                FavoriteIconButton(
                    favorite = headerValues.favorite,
                    onFavoriteChanged = onFavoriteChanged,
                )
            }
        },
        fadeOutMenu = false,
        reserveMenuWidth = false,
        modifier = Modifier.sharedBounds(sharedTransitionKey, "character_header")
    ) {
        AutoResizeHeightText(
            text = headerValues.name(),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        )

        Row(verticalAlignment = Alignment.Bottom) {
            val subtitle = headerValues.subtitle()
            AnimatedVisibility(
                subtitle.isNotEmpty(),
                label = "Character details subtitle text",
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 10.dp),
            ) {
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Bottom)
                    )
                }
            }

            Box {
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(
                            Res.string.anime_character_details_more_actions_content_description,
                        ),
                    )
                }

                val uriHandler = LocalUriHandler.current
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.anime_character_details_open_external)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.OpenInBrowser,
                                contentDescription = stringResource(
                                    Res.string.anime_character_details_open_external_icon_content_description
                                )
                            )
                        },
                        onClick = {
                            showMenu = false
                            uriHandler.openUri(
                                AniListUtils.characterUrl(characterId)
                                        + "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Serializable
data class CharacterHeaderParams(
    val name: String?,
    val subtitle: String?,
    val coverImage: ImageState?,
    val favorite: Boolean?,
)

class CharacterHeaderValues(
    private val params: CharacterHeaderParams?,
    private val character: () -> CharacterHeaderData?,
    private val favoriteUpdate: () -> Boolean?,
) : DetailsHeaderValues {
    override val bannerImage = null
    override val coverImage
        get() = params?.coverImage?.maybeOverride(character()?.image?.large)
    val favorite
        get() = favoriteUpdate() ?: character()?.isFavourite ?: params?.favorite

    @Composable
    fun name() = character()?.name?.primaryName() ?: params?.name ?: ""

    @Composable
    fun subtitle() = character()?.name?.subtitleName() ?: params?.subtitle ?: ""
}
