package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.request.crossfade
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.PagingErrorItem
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun LazyGridScope.staffSection(
    titleRes: StringResource?,
    staffList: LazyPagingItems<StaffDetails>,
    roleLines: Int = 1,
    viewAllRoute: NavDestination? = null,
    viewAllContentDescriptionTextRes: StringResource? = null,
) {
    if (staffList.itemCount == 0) return
    if (titleRes != null) {
        item(
            key = "staffHeader-$titleRes",
            span = GridUtils.maxSpanFunction,
            contentType = "detailsSectionHeader",
        ) {
            val navHostController = LocalNavHostController.current
            DetailsSectionHeader(
                stringResource(titleRes),
                onClickViewAll = viewAllRoute?.let { { navHostController.navigate(viewAllRoute) } },
                viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            )
        }
    }

    item(
        key = "$titleRes-section",
        span = GridUtils.maxSpanFunction,
        contentType = "staffSection",
    ) {
        StaffListRow(staffList = { staffList }, roleLines = roleLines)
    }
}

@Composable
fun StaffListRow(
    staffList: @Composable () -> LazyPagingItems<StaffDetails>,
    roleLines: Int = 1,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    @Suppress("NAME_SHADOWING")
    val staffList = staffList()
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
                val navHostController = LocalNavHostController.current
                StaffSmallCard(
                    sharedTransitionKey = sharedTransitionKey,
                    imageState = coverImageState,
                    onClick = {
                        if (staff != null) {
                            navHostController.navigate(
                                StaffDestinations.StaffDetails(
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
                            style = MaterialTheme.typography.bodySmall
                                .copy(lineBreak = LineBreak.Heading),
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
                        style = MaterialTheme.typography.bodyMedium
                            .copy(lineBreak = LineBreak.Heading),
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

    val animationProgress by animateFloatAsState(
        if (colors.containerColor.isUnspecified) 0f else 1f,
        label = "Staff card color fade in",
    )

    val containerColor = when {
        colors.containerColor.isUnspecified || animationProgress == 0f ->
            MaterialTheme.colorScheme.surface
        animationProgress == 1f -> colors.containerColor
        else -> colors.containerColor.copy(alpha = animationProgress)
            .compositeOver(MaterialTheme.colorScheme.surface)
    }

    val textColor = when {
        colors.textColor.isUnspecified || animationProgress == 0f -> defaultTextColor
        animationProgress == 1f -> colors.textColor
        else -> colors.textColor.copy(alpha = animationProgress)
            .compositeOver(defaultTextColor)
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
