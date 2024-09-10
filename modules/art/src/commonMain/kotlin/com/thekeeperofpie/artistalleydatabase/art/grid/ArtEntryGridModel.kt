package com.thekeeperofpie.artistalleydatabase.art.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Monitor
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WrongLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_entry_artists_unlocked_indicator_content_description
import artistalleydatabase.modules.art.generated.resources.art_entry_characters_unlocked_indicator_content_description
import artistalleydatabase.modules.art.generated.resources.art_entry_series_unlocked_indicator_content_description
import artistalleydatabase.modules.art.generated.resources.art_entry_source_unlocked_indicator_content_description
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.toUri
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
class ArtEntryGridModel(
    val value: ArtEntry,
    override val imageUri: Uri?,
    override val placeholderText: String,
) : EntryGridModel {

    override val id = EntryId("art_entry", value.id)
    override val imageWidth get() = value.imageWidth
    override val imageHeight get() = value.imageHeight
    override val imageWidthToHeightRatio get() = value.imageWidthToHeightRatio

    companion object {
        fun buildFromEntry(
            appFileSystem: AppFileSystem,
            json: Json,
            entry: ArtEntry,
        ): ArtEntryGridModel {
            val imageUri = EntryUtils.getImagePath(appFileSystem, entry.entryId)
                ?.takeIf(appFileSystem::exists)
                ?.toUri()
                ?.buildUpon()
                ?.appendQueryParameter("width", entry.imageWidth.toString())
                ?.appendQueryParameter("height", entry.imageHeight.toString())
                ?.build()

            // Placeholder text is generally only useful without an image
            val placeholderText = if (imageUri == null) {
                ArtEntryUtils.buildPlaceholderText(json, entry)
            } else ""


            return ArtEntryGridModel(
                value = entry,
                imageUri = imageUri,
                placeholderText = placeholderText,
            )
        }
    }

    @Composable
    override fun ErrorIcons(modifier: Modifier) {
        val seriesError = value.locks.seriesLocked != true
        val charactersError = value.locks.charactersLocked != true
        val artistsError = value.locks.artistsLocked != true
        val sourceError = value.locks.sourceLocked != true
        if (seriesError || charactersError || artistsError || sourceError) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = modifier
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.33f),
                        shape = RoundedCornerShape(topStart = 4.dp),
                    )
                    .padding(4.dp)
            ) {
                if (seriesError) {
                    Icon(
                        imageVector = Icons.Outlined.Monitor,
                        contentDescription = stringResource(
                            Res.string.art_entry_series_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (charactersError) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = stringResource(
                            Res.string.art_entry_characters_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (artistsError) {
                    Icon(
                        imageVector = Icons.Outlined.Brush,
                        contentDescription = stringResource(
                            Res.string.art_entry_artists_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (sourceError) {
                    Icon(
                        imageVector = Icons.Outlined.WrongLocation,
                        contentDescription = stringResource(
                            Res.string.art_entry_source_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
