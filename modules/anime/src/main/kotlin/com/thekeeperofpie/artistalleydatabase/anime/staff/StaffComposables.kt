@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.annotation.ExperimentalCoilApi
import coil3.request.crossfade
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.PagingErrorItem
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement

fun LazyListScope.staffSection(
    @StringRes titleRes: Int?,
    staffList: LazyPagingItems<DetailsStaff>,
    roleLines: Int = 1,
    onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
) {
    if (staffList.itemCount == 0) return
    if (titleRes != null) {
        item(key = "staffHeader-$titleRes") {
            val navigationCallback = LocalNavigationCallback.current
            DetailsSectionHeader(
                stringResource(titleRes),
                onClickViewAll = onClickViewAll?.let { { it(navigationCallback) } },
                viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            )
        }
    }

    item(key = "$titleRes-section") {
        StaffListRow(staffList = { staffList }, roleLines = roleLines)
    }
}

@Composable
fun StaffListRow(
    staffList: @Composable () -> LazyPagingItems<DetailsStaff>,
    roleLines: Int = 1,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    @Suppress("NAME_SHADOWING")
    val staffList = staffList()
    val navigationCallback = LocalNavigationCallback.current
    LazyRow(
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            count = staffList.itemCount,
            key = staffList.itemKey { it.idWithRole },
            contentType = staffList.itemContentType { "staff" },
        ) {
            val staff = staffList[it]
            // Staff can be the same entity but different roles, so wrap with different keys
            SharedTransitionKeyScope("staff_card", staff?.idWithRole) {
                val coverImageState = rememberCoilImageState(
                    staff?.image,
                    heightStartThreshold = 3 / 4f,
                    selectMaxPopulation = true,
                )
                val staffName = staff?.staff?.name?.primaryName()
                val staffSubtitle = staff?.staff?.name?.subtitleName()
                val sharedTransitionKey = staff?.staff?.id?.toString()
                    ?.let { SharedTransitionKey.makeKeyForId(it) }
                StaffSmallCard(
                    sharedTransitionKey = sharedTransitionKey,
                    imageState = coverImageState,
                    onClick = {
                        if (staff != null) {
                            navigationCallback.navigate(
                                AnimeDestination.StaffDetails(
                                    staffId = staff.staff.id.toString(),
                                    sharedTransitionKey = sharedTransitionKey,
                                    headerParams = StaffHeaderParams(
                                        name = staffName,
                                        subtitle = staffSubtitle,
                                        coverImage = coverImageState.toImageState(),
                                        favorite = null,
                                    )
                                )
                            )
                        }
                    },
                ) { textColor ->
                    staff?.role?.let {
                        AutoHeightText(
                            text = it,
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall.copy(
                                lineBreak = LineBreak(
                                    strategy = LineBreak.Strategy.Simple,
                                    strictness = LineBreak.Strictness.Strict,
                                    wordBreak = LineBreak.WordBreak.Default,
                                )
                            ),
                            minLines = roleLines,
                            maxLines = roleLines,
                            minTextSizeSp = 8f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                        )
                    }

                    AutoHeightText(
                        text = staff?.name?.primaryName().orEmpty(),
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineBreak = LineBreak(
                                strategy = LineBreak.Strategy.Balanced,
                                strictness = LineBreak.Strictness.Strict,
                                wordBreak = LineBreak.WordBreak.Default,
                            )
                        ),
                        minTextSizeSp = 8f,
                        minLines = 2,
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (staffList.loadState.hasError) {
            item("error") {
                PagingErrorItem(staffList)
            }
        }
    }
}


@Composable
fun StaffSmallCard(
    sharedTransitionKey: SharedTransitionKey?,
    imageState: CoilImageState,
    onClick: () -> Unit,
    width: Dp = 100.dp,
    content: @Composable (textColor: Color) -> Unit,
) {
    val defaultTextColor = MaterialTheme.typography.bodyMedium.color
    val colors = imageState.colors

    val animationProgress by animateIntAsState(
        if (colors.containerColor.isUnspecified) 0 else 255,
        label = "Staff card color fade in",
    )

    val containerColor = when {
        colors.containerColor.isUnspecified || animationProgress == 0 ->
            MaterialTheme.colorScheme.surface
        animationProgress == 255 -> colors.containerColor
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.containerColor.toArgb(),
                    animationProgress
                ),
                MaterialTheme.colorScheme.surface.toArgb()
            )
        )
    }

    val textColor = when {
        colors.textColor.isUnspecified || animationProgress == 0 -> defaultTextColor
        animationProgress == 255 -> colors.textColor
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.textColor.toArgb(),
                    animationProgress
                ),
                defaultTextColor.toArgb()
            )
        )
    }

    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        modifier = Modifier
            .width(width)
            .padding(bottom = 2.dp),
    ) {
        val density = LocalDensity.current
        StaffCoverImage(
            imageState = imageState,
            image = imageState.request()
                .crossfade(true)
                .size(
                    width = density.run { width.roundToPx() },
                    height = density.run { (width * 1.5f).roundToPx() },
                )
                .build(),
            modifier = Modifier
                .size(width = width, height = width * 1.5f)
                .sharedElement(sharedTransitionKey, "staff_image")
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                    )
                ),
            contentScale = ContentScale.Crop
        )

        content(textColor)
    }
}
