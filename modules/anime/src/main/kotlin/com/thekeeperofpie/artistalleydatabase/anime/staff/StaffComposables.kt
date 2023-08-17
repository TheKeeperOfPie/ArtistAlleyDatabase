package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

fun LazyListScope.staffSection(
    screenKey: String,
    @StringRes titleRes: Int,
    staffList: LazyPagingItems<DetailsStaff>,
    roleLines: Int = 1,
) {
    if (staffList.itemCount == 0) return
    item("$titleRes-header") {
        DetailsSectionHeader(stringResource(titleRes))
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
                CharacterSmallCard(
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
