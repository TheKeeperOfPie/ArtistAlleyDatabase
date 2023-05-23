package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anilist.AuthedUserQuery
import com.anilist.UserByIdQuery
import com.anilist.fragment.UserFavoriteMediaNode
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.descriptionSection
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
object UserOverviewScreen {

    @Composable
    operator fun invoke(
        entry: () -> AniListUserScreen.Entry?,
        viewer: AuthedUserQuery.Data.Viewer?,
        colorMap: MutableMap<String, Pair<Color, Color>>,
        navigationCallback: AnimeNavigator.NavigationCallback,
        modifier: Modifier = Modifier,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val entry = entry()
        val user = entry?.user
        var descriptionExpanded by remember { mutableStateOf(false) }
        val colorCalculationState = rememberColorCalculationState(colorMap)
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = 16.dp + (bottomNavigationState?.bottomNavBarPadding() ?: 0.dp)
            ),
            modifier = modifier.fillMaxSize()
        ) {
            if (user == null) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp)
                        )
                    }
                }
                return@LazyColumn
            }

            followingSection(
                user = user,
                viewer = viewer,
            )

            descriptionSection(
                titleTextRes = R.string.anime_user_about_label,
                htmlText = user.about?.trim(),
                expanded = { descriptionExpanded },
                onExpandedChange = { descriptionExpanded = it },
            )

            favoriteMediaSection(
                titleRes = R.string.anime_user_favorite_anime_label,
                entries = entry.anime,
                onClickEntry = navigationCallback::onMediaClick,
                colorCalculationState = colorCalculationState,
            )

            favoriteMediaSection(
                titleRes = R.string.anime_user_favorite_manga_label,
                entries = entry.manga,
                onClickEntry = navigationCallback::onMediaClick,
                colorCalculationState = colorCalculationState,
            )

            charactersSection(
                titleRes = R.string.anime_user_favorite_characters_label,
                characters = entry.characters,
                onCharacterClick = navigationCallback::onCharacterClick,
                onCharacterLongClick = navigationCallback::onCharacterLongClick,
                onStaffClick = navigationCallback::onStaffClick,
                colorCalculationState = colorCalculationState,
            )

            staffSection(
                titleRes = R.string.anime_user_favorite_staff_label,
                staff = entry.staff,
                onStaffClick = navigationCallback::onStaffClick,
                onStaffLongClick = navigationCallback::onStaffLongClick,
                colorCalculationState = colorCalculationState,
            )

            favoriteStudiosSection(
                studios = entry.studios,
                onClick = navigationCallback::onStudioClick,
            )

            previousNamesSection(
                names = user.previousNames?.filterNotNull()?.mapNotNull { it.name }.orEmpty()
            )
        }
    }

    private fun LazyListScope.followingSection(
        user: UserByIdQuery.Data.User,
        viewer: AuthedUserQuery.Data.Viewer?,
    ) {
        if (user.id == viewer?.id) return
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp)
            ) {
                FilterChip(
                    onClick = { /*TODO*/ },
                    selected = user.isFollower == true,
                    leadingIcon = {
                        Icon(
                            imageVector = if (user.isFollower == true) {
                                Icons.Filled.Check
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = stringResource(
                                if (user.isFollower == true) {
                                    R.string.anime_user_is_follower_content_description
                                } else {
                                    R.string.anime_user_is_not_follower_content_description
                                }
                            ),
                        )
                    },
                    label = { AutoHeightText(stringResource(R.string.anime_user_following_you)) },
                )

                FilterChip(
                    onClick = { /*TODO*/ },
                    selected = user.isFollowing == true,
                    leadingIcon = {
                        Icon(
                            imageVector = if (user.isFollowing == true) {
                                Icons.Filled.Check
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = stringResource(
                                if (user.isFollowing == true) {
                                    R.string.anime_user_is_following_content_description
                                } else {
                                    R.string.anime_user_is_not_following_content_description
                                }
                            ),
                        )
                    },
                    label = { AutoHeightText(stringResource(R.string.anime_user_following)) },
                )
            }
        }
    }

    private fun LazyListScope.favoriteMediaSection(
        @StringRes titleRes: Int,
        entries: List<UserFavoriteMediaNode>,
        onClickEntry: (UserFavoriteMediaNode, imageWidthToHeightRatio: Float) -> Unit,
        colorCalculationState: ColorCalculationState,
    ) {
        if (entries.isEmpty()) return
        item {
            DetailsSectionHeader(stringResource(titleRes))
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(entries, { it.id }) {
                    val id = it.id.toString()
                    val colors = colorCalculationState.colorMap[id]
                    val animationProgress by animateIntAsState(
                        if (colors == null) 0 else 255,
                        label = "Media card color fade in",
                    )

                    val containerColor = when {
                        colors == null || animationProgress == 0 ->
                            MaterialTheme.colorScheme.surface
                        animationProgress == 255 -> colors.first
                        else -> Color(
                            ColorUtils.compositeColors(
                                ColorUtils.setAlphaComponent(
                                    colors.first.toArgb(),
                                    animationProgress
                                ),
                                MaterialTheme.colorScheme.surface.toArgb()
                            )
                        )
                    }

                    var widthToHeightRatio by remember { MutableSingle(1f) }
                    ElevatedCard(
                        onClick = { onClickEntry(it, widthToHeightRatio) },
                        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                    ) {
                        ConstraintLayout {
                            val (image, title) = createRefs()
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it.coverImage?.extraLarge)
                                    .crossfade(true)
                                    .allowHardware(colorCalculationState.hasColor(id))
                                    .size(
                                        width = coil.size.Dimension.Undefined,
                                        height = coil.size.Dimension.Pixels(
                                            LocalDensity.current.run { 180.dp.roundToPx() }
                                        ),
                                    )
                                    .build(),
                                contentScale = ContentScale.FillHeight,
                                contentDescription = stringResource(R.string.anime_media_cover_image),
                                onSuccess = {
                                    widthToHeightRatio = it.widthToHeightRatio()
                                    ComposeColorUtils.calculatePalette(
                                        id = id,
                                        success = it,
                                        colorCalculationState = colorCalculationState,
                                        heightStartThreshold = 3 / 4f,
                                        selectMaxPopulation = true,
                                    )
                                },
                                modifier = Modifier
                                    .constrainAs(image) {
                                        height = Dimension.value(180.dp)
                                        width = Dimension.wrapContent
                                        linkTo(start = parent.start, end = parent.end)
                                        top.linkTo(parent.top)
                                    }
                            )

                            AutoHeightText(
                                text = it.title?.userPreferred.orEmpty(),
                                color = ComposeColorUtils.bestTextColor(containerColor)
                                    ?: Color.Unspecified,
                                maxLines = 2,
                                minLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .constrainAs(title) {
                                        linkTo(start = image.start, end = image.end)
                                        top.linkTo(image.bottom)
                                        width = Dimension.fillToConstraints
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun LazyListScope.favoriteStudiosSection(
        studios: List<AniListUserScreen.Entry.Studio>,
        onClick: (String) -> Unit,
    ) {
        if (studios.isEmpty()) return
        item {
            DetailsSectionHeader(stringResource(R.string.anime_user_favorite_studios_label))
        }

        item {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                studios.forEachIndexed { index, studio ->
                    if (index != 0) {
                        Divider()
                    }

                    Text(
                        text = studio.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick(studio.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    private fun LazyListScope.previousNamesSection(names: List<String>) {
        if (names.isEmpty()) return
        item {
            DetailsSectionHeader(stringResource(R.string.anime_user_previous_names_label))
        }

        item {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                names.forEachIndexed { index, name ->
                    if (index != 0) {
                        Divider()
                    }

                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
