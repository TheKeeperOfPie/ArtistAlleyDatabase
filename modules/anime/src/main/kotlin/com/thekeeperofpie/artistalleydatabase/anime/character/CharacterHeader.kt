package com.thekeeperofpie.artistalleydatabase.anime.character

import android.os.Bundle
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.anilist.fragment.CharacterHeaderData
import com.anilist.fragment.CharacterNavigationData
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
fun CharacterHeader(
    screenKey: String,
    upIconOption: UpIconOption?,
    characterId: String,
    progress: Float,
    headerValues: CharacterHeaderValues,
    onFavoriteChanged: (Boolean) -> Unit,
    onImageWidthToHeightRatioAvailable: (Float) -> Unit = {},
    onCoverImageSharedElementFractionChanged: ((Float) -> Unit)? = null,
) {
    SharedElement(
        key = "anime_character_${characterId}_header",
        screenKey = screenKey,
    ) {
        val colorCalculationState = LocalColorCalculationState.current
        CoverAndBannerHeader(
            screenKey = AnimeNavDestinations.CHARACTER_DETAILS.id,
            upIconOption = upIconOption,
            entryId = EntryId("anime_character", characterId),
            progress = progress,
            color = { headerValues.color(colorCalculationState) },
            coverImage = { headerValues.image },
            coverImageAllowHardware = colorCalculationState.allowHardware(characterId),
            coverImageWidthToHeightRatio = headerValues.imageWidthToHeightRatio,
            coverImageOnSuccess = {
                onImageWidthToHeightRatioAvailable(it.widthToHeightRatio())
                ComposeColorUtils.calculatePalette(characterId, it, colorCalculationState)
            },
            onCoverImageSharedElementFractionChanged = onCoverImageSharedElementFractionChanged,
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

class CharacterHeaderValues(
    arguments: Bundle,
    val imageWidthToHeightRatio: Float = arguments.getString("imageWidthToHeightRatio")
        ?.toFloatOrNull() ?: 1f,
    private val _name: String? = arguments.getString("name"),
    private val _subtitle: String? = arguments.getString("subtitle"),
    private val _favorite: Boolean? = arguments.getString("favorite")?.toBooleanStrictOrNull(),
    private val _image: String? = arguments.getString("image"),
    private val _color: Color? = arguments.getString("color")
        ?.toIntOrNull()
        ?.let(::Color),
    private val character: () -> CharacterHeaderData?,
    private val favoriteUpdate: () -> Boolean?,
) {
    companion object {
        const val routeSuffix = "&name={name}" +
                "&subtitle={subtitle}" +
                "&favorite={favorite}" +
                "&image={image}" +
                "&imageWidthToHeightRatio={imageWidthToHeightRatio}" +
                "&color={color}"

        fun routeSuffix(
            character: CharacterHeaderData?,
            languageOption: AniListLanguageOption,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = if (character == null) "" else routeSuffix(
            name = character.name?.primaryName(languageOption),
            subtitle = character.name?.subtitleName(languageOption),
            favorite = favorite,
            image = character.image?.large,
            imageWidthToHeightRatio = imageWidthToHeightRatio,
            color = color,
        )

        fun routeSuffix(
            character: CharacterNavigationData?,
            languageOption: AniListLanguageOption,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = if (character == null) "" else routeSuffix(
            name = character.name?.primaryName(languageOption),
            subtitle = character.name?.subtitleName(languageOption),
            favorite = favorite,
            image = character.image?.large,
            imageWidthToHeightRatio = imageWidthToHeightRatio,
            color = color,
        )

        private fun routeSuffix(
            name: String?,
            subtitle: String?,
            favorite: Boolean?,
            image: String?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = "&name=$name" +
                "&subtitle=$subtitle" +
                "&favorite=$favorite" +
                "&image=$image" +
                "&imageWidthToHeightRatio=$imageWidthToHeightRatio" +
                "&color=${color?.toArgb()}"

        fun navArguments() = listOf(
            "name",
            "subtitle",
            "favorite",
            "image",
            "imageWidthToHeightRatio",
            "color",
        ).map {
            navArgument(it) {
                type = NavType.StringType
                nullable = true
            }
        }
    }

    val image
        get() = character()?.image?.large ?: _image
    val favorite
        get() = favoriteUpdate() ?: character()?.isFavourite ?: _favorite

    @Composable
    fun name() = character()?.name?.primaryName() ?: _name ?: ""

    @Composable
    fun subtitle() = character()?.name?.subtitleName() ?: _subtitle ?: ""

    @Composable
    fun color(colorCalculationState: ColorCalculationState) =
        colorCalculationState.getColors(character()?.id?.toString()).first
            .takeOrElse { _color ?: Color.Unspecified }

    fun colorNonComposable(colorCalculationState: ColorCalculationState) =
        colorCalculationState.getColorsNonComposable(character()?.id?.toString()).first
            .takeOrElse { _color ?: Color.Unspecified }
}
