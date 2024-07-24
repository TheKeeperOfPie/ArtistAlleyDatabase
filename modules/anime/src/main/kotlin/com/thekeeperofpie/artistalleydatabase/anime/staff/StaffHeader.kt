@file:OptIn(ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.staff

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
import coil3.annotation.ExperimentalCoilApi
import com.anilist.fragment.StaffHeaderData
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.ImageState
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.maybeOverride
import com.thekeeperofpie.artistalleydatabase.compose.rememberCoilImageState
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// TODO: Collapse this with CharacterHeader?
@Composable
fun StaffHeader(
    staffId: String,
    upIconOption: UpIconOption?,
    progress: Float,
    headerValues: StaffHeaderValues,
    coverImageState: CoilImageState = rememberCoilImageState(headerValues.coverImage),
    onFavoriteChanged: (Boolean) -> Unit,
) {
    val colorCalculationState = LocalColorCalculationState.current
    CoverAndBannerHeader(
        upIconOption = upIconOption,
        headerValues = headerValues,
        coverImageState = coverImageState,
        coverImageAllowHardware = colorCalculationState.allowHardware(staffId),
        progress = progress,
        color = { headerValues.color(colorCalculationState) },
        coverImageOnSuccess = {
            ComposeColorUtils.calculatePalette(staffId, it.result.image, colorCalculationState)
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

@Parcelize
@Serializable
data class StaffHeaderParams(
    val name: String?,
    val subtitle: String?,
    val coverImage: ImageState?,
    val favorite: Boolean?,
    val colorArgb: Int? = null,
) : Parcelable

class StaffHeaderValues(
    private val params: StaffHeaderParams?,
    private val staff: () -> StaffHeaderData?,
    private val favoriteUpdate: () -> Boolean?,
) : DetailsHeaderValues {
    override val bannerImage = null
    override val coverImage
        get() = params?.coverImage.maybeOverride(staff()?.image?.large)
    val favorite
        get() = favoriteUpdate() ?: staff()?.isFavourite ?: params?.favorite

    @Composable
    fun name() = staff()?.name?.primaryName() ?: params?.name ?: ""

    @Composable
    fun subtitle() = staff()?.name?.subtitleName() ?: params?.subtitle ?: ""

    @Composable
    fun color(colorCalculationState: ColorCalculationState) =
        colorCalculationState.getColors(staff()?.id?.toString()).first
            .takeOrElse { params?.colorArgb?.let(::Color) ?: Color.Unspecified }
}
