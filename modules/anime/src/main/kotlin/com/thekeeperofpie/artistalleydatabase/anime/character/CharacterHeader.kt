package com.thekeeperofpie.artistalleydatabase.anime.character

import android.os.Parcelable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.fragment.CharacterHeaderData
import com.anilist.fragment.CharacterNavigationData
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.AutoSharedElement
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Composable
fun CharacterHeader(
    screenKey: String,
    upIconOption: UpIconOption?,
    viewer: AniListViewer?,
    characterId: String,
    progress: Float,
    headerValues: CharacterHeaderValues,
    onFavoriteChanged: (Boolean) -> Unit,
    onImageWidthToHeightRatioAvailable: (Float) -> Unit = {},
) {
    AutoSharedElement(
        key = "anime_character_${characterId}_header",
        screenKey = screenKey,
    ) {
        val colorCalculationState = LocalColorCalculationState.current
        CoverAndBannerHeader(
            upIconOption = upIconOption,
            headerValues = headerValues,
            coverImageAllowHardware = colorCalculationState.allowHardware(characterId),
            progress = progress,
            color = { headerValues.color(colorCalculationState) },
            coverImageOnSuccess = {
                onImageWidthToHeightRatioAvailable(it.widthToHeightRatio())
                ComposeColorUtils.calculatePalette(characterId, it, colorCalculationState)
            },
            menuContent = {
                FavoriteIconButton(
                    favorite = headerValues.favorite,
                    onFavoriteChanged = onFavoriteChanged,
                )
            },
            fadeOutMenu = false,
            reserveMenuWidth = false,
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
                                R.string.anime_character_details_more_actions_content_description,
                            ),
                        )
                    }

                    val uriHandler = LocalUriHandler.current
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.anime_character_details_open_external)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.OpenInBrowser,
                                    contentDescription = stringResource(
                                        R.string.anime_character_details_open_external_icon_content_description
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
}

@Parcelize
@Serializable
data class CharacterHeaderParams(
    val coverImageWidthToHeightRatio: Float?,
    val name: String?,
    val subtitle: String?,
    val favorite: Boolean?,
    val coverImage: String?,
    val colorArgb: Int?,
) : Parcelable {
    constructor(
        name: String?,
        coverImageWidthToHeightRatio: Float?,
        favorite: Boolean? = null,
        characterNavigationData: CharacterNavigationData,
    ) : this(
        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
        name = name,
        subtitle = null,
        favorite = favorite,
        coverImage = characterNavigationData.image?.large,
        colorArgb = null,
    )
}

class CharacterHeaderValues(
    private val params: CharacterHeaderParams?,
    private val character: () -> CharacterHeaderData?,
    private val favoriteUpdate: () -> Boolean?,
) : DetailsHeaderValues {
    override val coverImageWidthToHeightRatio = params?.coverImageWidthToHeightRatio
    override val bannerImage = null
    override val coverImage
        get() = character()?.image?.large ?: params?.coverImage
    val favorite
        get() = favoriteUpdate() ?: character()?.isFavourite ?: params?.favorite

    @Composable
    fun name() = character()?.name?.primaryName() ?: params?.name ?: ""

    @Composable
    fun subtitle() = character()?.name?.subtitleName() ?: params?.subtitle ?: ""

    @Composable
    fun color(colorCalculationState: ColorCalculationState) =
        colorCalculationState.getColors(character()?.id?.toString()).first
            .takeOrElse { params?.colorArgb?.let(::Color) ?: Color.Unspecified }

    fun colorNonComposable(colorCalculationState: ColorCalculationState) =
        colorCalculationState.getColorsNonComposable(character()?.id?.toString()).first
            .takeOrElse { params?.colorArgb?.let(::Color) ?: Color.Unspecified }
}
