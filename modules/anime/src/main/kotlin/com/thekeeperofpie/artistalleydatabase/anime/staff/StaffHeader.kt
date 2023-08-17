package com.thekeeperofpie.artistalleydatabase.anime.staff

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
import com.anilist.fragment.StaffHeaderData
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

// TODO: Collapse this with CharacterHeader?
@Composable
fun StaffHeader(
    staffId: String,
    upIconOption: UpIconOption?,
    progress: Float,
    headerValues: StaffHeaderValues,
    onFavoriteChanged: (Boolean) -> Unit,
    onImageWidthToHeightRatioAvailable: (Float) -> Unit = {},
) {
    val colorCalculationState = LocalColorCalculationState.current
    CoverAndBannerHeader(
        screenKey = AnimeNavDestinations.STAFF_DETAILS.id,
        upIconOption = upIconOption,
        entryId = EntryId("anime_staff", staffId),
        progress = progress,
        color = { headerValues.color(colorCalculationState) },
        coverImage = { headerValues.image },
        coverImageAllowHardware = colorCalculationState.hasColor(staffId),
        coverImageWidthToHeightRatio = headerValues.imageWidthToHeightRatio,
        coverImageOnSuccess = {
            onImageWidthToHeightRatioAvailable(it.widthToHeightRatio())
            ComposeColorUtils.calculatePalette(staffId, it, colorCalculationState)
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
            val subtitleText = headerValues.subtitle()
            AnimatedVisibility(
                subtitleText.isNotEmpty(),
                label = "Staff details subtitle text",
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 10.dp),
            ) {
                if (subtitleText.isNotEmpty()) {
                    Text(
                        text = subtitleText,
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
                            R.string.anime_staff_details_more_actions_content_description,
                        ),
                    )
                }

                val uriHandler = LocalUriHandler.current
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.anime_staff_details_open_external)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.OpenInBrowser,
                                contentDescription = stringResource(
                                    R.string.anime_staff_details_open_external_icon_content_description
                                )
                            )
                        },
                        onClick = {
                            showMenu = false
                            uriHandler.openUri(
                                AniListUtils.staffUrl(staffId)
                                        + "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                            )
                        }
                    )
                }
            }
        }
    }
}

class StaffHeaderValues(
    arguments: Bundle,
    val imageWidthToHeightRatio: Float = arguments.getString("imageWidthToHeightRatio")
        ?.toFloatOrNull() ?: 1f,
    private val _name: String? = arguments.getString("name"),
    private val _subtitle: String? = arguments.getString("subtitle"),
    private val _image: String? = arguments.getString("image"),
    private val _color: Color? = arguments.getString("color")
        ?.toIntOrNull()
        ?.let(::Color),
    private val _favorite: Boolean? = arguments.getString("favorite")?.toBooleanStrictOrNull(),
    private val staff: () -> StaffHeaderData?,
    private val favoriteUpdate: () -> Boolean?,
) {
    companion object {
        const val routeSuffix = "&name={name}" +
                "&subtitle={subtitle}" +
                "&image={image}" +
                "&imageWidthToHeightRatio={imageWidthToHeightRatio}" +
                "&color={color}" +
                "&favorite={favorite}"

        fun routeSuffix(
            staff: StaffHeaderData?,
            languageOption: AniListLanguageOption,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = if (staff == null) "" else routeSuffix(
            name = staff.name?.primaryName(languageOption),
            subtitle = staff.name?.subtitleName(languageOption),
            image = staff.image?.large,
            imageWidthToHeightRatio = imageWidthToHeightRatio,
            color = color,
            favorite = favorite,
        )

        fun routeSuffix(
            staff: StaffNavigationData?,
            languageOption: AniListLanguageOption,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) = if (staff == null) "" else routeSuffix(
            name = staff.name?.primaryName(languageOption),
            subtitle = staff.name?.subtitleName(languageOption),
            image = staff.image?.large,
            imageWidthToHeightRatio = imageWidthToHeightRatio,
            color = color,
            favorite = favorite,
        )

        private fun routeSuffix(
            name: String?,
            subtitle: String?,
            image: String?,
            imageWidthToHeightRatio: Float,
            color: Color?,
            favorite: Boolean?,
        ) = "&name=$name" +
                "&subtitle=$subtitle" +
                "&image=$image" +
                "&imageWidthToHeightRatio=$imageWidthToHeightRatio" +
                "&color=${color?.toArgb()}" +
                "&favorite=$favorite"

        fun navArguments() = listOf(
            "name",
            "subtitle",
            "image",
            "imageWidthToHeightRatio",
            "color",
            "favorite",
        ).map {
            navArgument(it) {
                type = NavType.StringType
                nullable = true
            }
        }
    }

    val image
        get() = staff()?.image?.large ?: _image
    val favorite
        get() = favoriteUpdate() ?: staff()?.isFavourite ?: _favorite

    @Composable
    fun name() = staff()?.name?.primaryName() ?: _name ?: ""

    @Composable
    fun subtitle() = staff()?.name?.subtitleName() ?: _subtitle ?: ""

    fun color(colorCalculationState: ColorCalculationState) =
        colorCalculationState.getColors(staff()?.id?.toString()).first
            .takeOrElse { _color ?: Color.Unspecified }
}
