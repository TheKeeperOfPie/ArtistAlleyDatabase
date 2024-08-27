package com.thekeeperofpie.artistalleydatabase.cds.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.DiscFull
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Monitor
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.cds.R
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.toUri
import kotlinx.io.files.SystemFileSystem

@OptIn(ExperimentalLayoutApi::class)
class CdEntryGridModel(
    val value: CdEntry,
    override val imageUri: Uri?,
    override val placeholderText: String,
) : EntryGridModel {

    override val id = EntryId("cd_entry", value.id)
    override val imageWidth get() = value.imageWidth
    override val imageHeight get() = value.imageHeight
    override val imageWidthToHeightRatio get() = value.imageWidthToHeightRatio

    companion object {
        fun buildFromEntry(
            appFileSystem: AppFileSystem,
            entry: CdEntry,
        ): CdEntryGridModel {
            val imageUri = EntryUtils.getImagePath(appFileSystem, entry.entryId)
                ?.takeIf(SystemFileSystem::exists)
                ?.toUri()
                ?.buildUpon()
                ?.appendQueryParameter("width", entry.imageWidth.toString())
                ?.appendQueryParameter("height", entry.imageHeight.toString())
                ?.build()

            // Placeholder text is generally only useful without an image
            val placeholderText = if (imageUri == null) {
                CdEntryUtils.buildPlaceholderText(entry)
            } else ""

            return CdEntryGridModel(
                value = entry,
                imageUri = imageUri,
                placeholderText = placeholderText,
            )
        }
    }

    @Composable
    override fun ErrorIcons(modifier: Modifier) {
        val catalogIdError = value.locks.catalogIdLocked != true
        val titleError = value.locks.titlesLocked != true
        val performersError = value.locks.performersLocked != true
        val composersError = value.locks.composersLocked != true
        val seriesError = value.locks.seriesLocked != true
        val charactersError = value.locks.charactersLocked != true
        val discsError = value.locks.discsLocked != true
        if (catalogIdError || titleError || performersError || composersError || seriesError
            || charactersError || discsError) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = modifier
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.33f),
                        shape = RoundedCornerShape(topStart = 4.dp),
                    )
                    .padding(4.dp)
            ) {
                if (catalogIdError) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(
                            R.string.cd_entry_catalogId_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (titleError) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ShortText,
                        contentDescription = stringResource(
                            R.string.cd_entry_title_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (performersError) {
                    Icon(
                        imageVector = Icons.Outlined.MicNone,
                        contentDescription = stringResource(
                            R.string.cd_entry_performers_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (composersError) {
                    Icon(
                        imageVector = Icons.Outlined.MusicNote,
                        contentDescription = stringResource(
                            R.string.cd_entry_composers_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (seriesError) {
                    Icon(
                        imageVector = Icons.Outlined.Monitor,
                        contentDescription = stringResource(
                            R.string.cd_entry_series_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (charactersError) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = stringResource(
                            R.string.cd_entry_characters_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
                if (discsError) {
                    Icon(
                        imageVector = Icons.Outlined.DiscFull,
                        contentDescription = stringResource(
                            R.string.cd_entry_discs_unlocked_indicator_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
