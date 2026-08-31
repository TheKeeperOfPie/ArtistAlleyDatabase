package com.thekeeperofpie.artistalleydatabase.alley.artist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_has_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_artist_new
import artistalleydatabase.modules.alley.generated.resources.alley_artist_new_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verified
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verified_content_description
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistProfileImage
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.FiberNew
import com.thekeeperofpie.artistalleydatabase.icons.filled.FormatPaint
import com.thekeeperofpie.artistalleydatabase.icons.filled.Verified
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import org.jetbrains.compose.resources.stringResource


@Composable
fun ArtistListRow(
    entry: ArtistEntryGridModel,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tagRow: (@Composable () -> Unit)? = null,
    useSharedElements: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val artist = entry.artist
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .conditionally(useSharedElements, Modifier.sharedBounds("container", artist.id))
        ) {
            val profileImage = entry.profileImage
            Spacer(Modifier.width(10.dp))
            ArtistProfileImage(
                artistId = artist.id,
                image = profileImage,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            val booth = artist.booth
            if (!booth.isNullOrBlank()) {
                Text(
                    text = booth,
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(12.dp)
                        .conditionally(
                            useSharedElements,
                            Modifier.sharedElement("booth", artist.id)
                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = artist.name,
                    color = if (entry.artist.verifiedArtist) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .conditionally(useSharedElements, Modifier.sharedElement("name", artist.id))
                        .padding(
                            start = if (artist.booth.isNullOrBlank()) 16.dp else 0.dp,
                            top = 12.dp,
                            bottom = 12.dp,
                        )
                        .weight(1f, fill = false)
                )

                if (entry.artist.newArtist) {
                    IconWithTooltip(
                        imageVector = Icons.Default.FiberNew,
                        tooltipText = stringResource(Res.string.alley_artist_new),
                        contentDescription = stringResource(Res.string.alley_artist_new_content_description),
                        modifier = Modifier.size(20.dp)
                    )
                } else if (entry.artist.verifiedArtist) {
                    IconWithTooltip(
                        imageVector = Icons.Default.Verified,
                        tooltipText = stringResource(Res.string.alley_artist_verified),
                        contentDescription = stringResource(Res.string.alley_artist_verified_content_description),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (entry.artist.commissionModels.isNotEmpty()) {
                IconWithTooltip(
                    imageVector = Icons.Default.FormatPaint,
                    tooltipText = stringResource(Res.string.alley_artist_has_commissions),
                    contentDescription = stringResource(
                        Res.string.alley_artist_commission_icon_content_description
                    ),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .size(16.dp)
                        .align(Alignment.Top)
                )
            }

            FavoriteIconButton(
                entryText = { entry.title },
                favorite = {  entry.favorite },
                onFavoriteToggle = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.Top)
                    .conditionally(useSharedElements, Modifier.sharedElement("favorite", artist.id))
            )
        }

        if (tagRow != null && entry.series.isNotEmpty()) {
            tagRow()
        }
    }
}

@Preview
@Composable
private fun ArtistListRowPreview() {
    val artist = ArtistWithUserDataProvider.values.first()
    ArtistListRow(
        entry = ArtistEntryGridModel.buildFromEntry(
            randomSeed = 0,
            showOnlyConfirmedTags = false,
            entry = artist,
            showOutdatedCatalogs = false,
        ),
        onFavoriteToggle = {},
    )
}
