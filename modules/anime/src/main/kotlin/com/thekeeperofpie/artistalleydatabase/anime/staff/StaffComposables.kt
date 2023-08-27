@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

fun LazyListScope.staffSection(
    screenKey: String,
    @StringRes titleRes: Int,
    staffList: LazyPagingItems<DetailsStaff>,
    roleLines: Int = 1,
    onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
) {
    if (staffList.itemCount == 0) return
    item("staffHeader-$titleRes") {
        val navigationCallback = LocalNavigationCallback.current
        DetailsSectionHeader(
            stringResource(titleRes),
            onClickViewAll = onClickViewAll?.let { { it(navigationCallback) } },
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
        )
    }

    item("$titleRes-section") {
        val navigationCallback = LocalNavigationCallback.current
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                count = staffList.itemCount,
                key = staffList.itemKey { it.idWithRole },
                contentType = staffList.itemContentType { "staff" },
            ) {
                val staff = staffList[it]
                var imageWidthToHeightRatio by remember { MutableSingle(1f) }
                val colorCalculationState = LocalColorCalculationState.current
                StaffSmallCard(
                    screenKey = screenKey,
                    id = EntryId("anime_staff", staff?.id.orEmpty()),
                    image = staff?.image,
                    onClick = {
                        if (staff != null) {
                            navigationCallback.onStaffClick(
                                staff.staff,
                                null,
                                imageWidthToHeightRatio,
                                colorCalculationState.getColors(staff.id).first,
                            )
                        }
                    },
                    onImageSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() }
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
    }
}


@Composable
fun StaffSmallCard(
    screenKey: String,
    id: EntryId,
    image: String?,
    onClick: () -> Unit,
    innerImage: String? = null,
    onImageSuccess: (AsyncImagePainter.State.Success) -> Unit = {},
    width: Dp = 100.dp,
    content: @Composable (textColor: Color) -> Unit,
) {
    val defaultTextColor = MaterialTheme.typography.bodyMedium.color
    val colorCalculationState = LocalColorCalculationState.current
    val colors = colorCalculationState.getColors(id.scopedId)

    val animationProgress by animateIntAsState(
        if (colors.first.isUnspecified) 0 else 255,
        label = "Staff card color fade in",
    )

    val containerColor = when {
        colors.first.isUnspecified || animationProgress == 0 ->
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

    val textColor = when {
        colors.second.isUnspecified || animationProgress == 0 -> defaultTextColor
        animationProgress == 255 -> colors.second
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.second.toArgb(),
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
            screenKey = screenKey,
            staffId = id.valueId,
            image = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .crossfade(true)
                .allowHardware(colorCalculationState.hasColor(id.scopedId))
                .size(
                    width = density.run { width.roundToPx() },
                    height = density.run { (width * 1.5f).roundToPx() },
                )
                .build(),
            contentScale = ContentScale.Crop,
            onSuccess = {
                onImageSuccess(it)
                ComposeColorUtils.calculatePalette(
                    id = id.scopedId,
                    success = it,
                    colorCalculationState = colorCalculationState,
                    heightStartThreshold = 3 / 4f,
                    // Only capture left 3/5ths to ignore
                    // part covered by voice actor
                    widthEndThreshold = if (innerImage == null) 1f else 3 / 5f,
                    selectMaxPopulation = true,
                )
            },
            modifier = Modifier
                .size(width = width, height = width * 1.5f)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                    )
                )
        )

        content(textColor)
    }
}
