package com.thekeeperofpie.artistalleydatabase.alley.artist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_available_fallback_prompt
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_available_fallback_prompt_always_show
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_available_fallback_prompt_show
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image_none
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.BrokenImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AvailableFallbackPrompt(
    fallbackYear: DataYear,
    onShowFallback: () -> Unit,
    onAlwaysShowFallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.BrokenImage,
                contentDescription = stringResource(
                    Res.string.alley_artist_catalog_image_none
                )
            )
            Text(
                text = stringResource(
                    Res.string.alley_artist_catalog_available_fallback_prompt,
                    stringResource(fallbackYear.fullName),
                ),
                textAlign = TextAlign.Center,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onShowFallback) {
                    Text(
                        text = stringResource(
                            Res.string.alley_artist_catalog_available_fallback_prompt_show,
                            stringResource(fallbackYear.shortName),
                        )
                    )
                }
                Button(onClick = onAlwaysShowFallback) {
                    Text(
                        text = stringResource(
                            Res.string.alley_artist_catalog_available_fallback_prompt_always_show
                        )
                    )
                }
            }
        }
    }
}


@AlleyPreview
@Composable
private fun AvailableFallbackPromptPreview() {
    AvailableFallbackPrompt(
        fallbackYear = DataYear.ANIME_EXPO_2025,
        onShowFallback = {},
        onAlwaysShowFallback = {},
    )
}
