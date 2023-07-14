package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSmallCard
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

fun LazyListScope.staffSection(
    screenKey: String,
    @StringRes titleRes: Int,
    staff: List<DetailsStaff>,
    onStaffClick: (StaffNavigationData, imageWidthToHeightRatio: Float, color: Color?) -> Unit,
    onStaffLongClick: (String) -> Unit,
    colorCalculationState: ColorCalculationState,
    roleLines: Int = 1,
) {
    if (staff.isEmpty()) return
    item {
        DetailsSectionHeader(stringResource(titleRes))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(staff, { it.id }) {
                var imageWidthToHeightRatio by remember { MutableSingle(1f) }
                CharacterSmallCard(
                    screenKey = screenKey,
                    id = EntryId("anime_staff", it.id),
                    image = it.image,
                    colorCalculationState = colorCalculationState,
                    onClick = {
                        onStaffClick(
                            it.staff,
                            imageWidthToHeightRatio,
                            colorCalculationState.getColors(it.id).first,
                        )
                    },
                    onImageSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() }
                ) { textColor ->
                    it.role?.let {
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

                    it.name?.let {
                        AutoHeightText(
                            text = it,
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
}
