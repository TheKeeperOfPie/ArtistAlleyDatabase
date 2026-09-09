package com.thekeeperofpie.artistalleydatabase.alley.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import kotlin.random.Random

@Composable
fun SearchItemCard(
    ignored: Boolean,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ThemeAwareElevatedCard(
        onClick = onClick,
        onLongClick = { onIgnoredToggle(!ignored) },
        modifier = modifier.alpha(if (ignored) 0.38f else 1f)
    ) {
        content()
    }
}

@Composable
fun SearchItemImage(
    images: List<ImageWithDimensions>,
    ignored: Boolean,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    imagePager: @Composable () -> Unit,
    noImageContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onIgnoredToggle(!ignored) }
            )
            .background(color = MaterialTheme.colorScheme.surface)
            .border(
                width = Dp.Hairline, color = if (images.isEmpty()) {
                    MaterialTheme.colorScheme.surfaceBright
                } else {
                    MaterialTheme.colorScheme.surfaceDim
                }
            )
            .alpha(if (ignored) 0.38f else 1f)
    ) {
        if (images.isEmpty() || ignored) {
            noImageContent()
        } else {
            imagePager()
        }
    }
}

@Composable
internal fun rememberSearchPagerState(
    entryId: String,
    images: List<CatalogImage>,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
): PagerState {
    val pageCount = if (images.isEmpty()) {
        0
    } else if (images.size == 1) {
        1
    } else {
        images.size + 1
    }
    return rememberPagerState(
        initialPage = if (showGridByDefault || images.isEmpty()) {
            0
        } else if (showRandomCatalogImage) {
            (1..images.size).random(Random(LocalStableRandomSeed.current + entryId.hashCode()))
        } else {
            1
        },
        pageCount = { pageCount },
    )
}
