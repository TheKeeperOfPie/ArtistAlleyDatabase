package com.thekeeperofpie.artistalleydatabase.anime.staff

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.fragment.StaffHeaderData
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedBounds
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.maybeOverride
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import kotlinx.serialization.Serializable

// TODO: Collapse this with CharacterHeader?
@Composable
fun StaffHeader(
    staffId: String,
    upIconOption: UpIconOption?,
    progress: Float,
    headerValues: StaffHeaderValues,
    sharedTransitionKey: SharedTransitionKey?,
    coverImageState: CoilImageState = rememberCoilImageState(headerValues.coverImage),
    onFavoriteChanged: (Boolean) -> Unit,
) {
    CoverAndBannerHeader(
        upIconOption = upIconOption,
        headerValues = headerValues,
        coverImageState = coverImageState,
        sharedTransitionKey = sharedTransitionKey,
        coverImageSharedTransitionIdentifier = "staff_image",
        bannerImageSharedTransitionIdentifier = "staff_banner_image",
        progress = progress,
        menuContent = {
            FavoriteIconButton(
                favorite = headerValues.favorite,
                onFavoriteChanged = onFavoriteChanged,
            )
        },
        fadeOutMenu = false,
        reserveMenuWidth = false,
        modifier = Modifier.sharedBounds(sharedTransitionKey, "staff_header")
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

@Serializable
data class StaffHeaderParams(
    val name: String?,
    val subtitle: String?,
    val coverImage: ImageState?,
    val favorite: Boolean?,
)

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
}
