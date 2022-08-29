package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.android_utils.ImageUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date

abstract class CdEntryDetailsViewModel(
    protected val application: Application,
    protected val cdEntryDao: CdEntryDetailsDao,
    private val aniListApi: AniListApi,
    private val aniListJson: AniListJson,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    // TODO: Enforce single value
    protected val catalogIdSection = EntrySection.MultiText(
        R.string.cd_entry_catalog_header,
        R.string.cd_entry_catalog_header,
        R.string.cd_entry_catalog_header,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val titleSection = EntrySection.MultiText(
        R.string.cd_entry_title_header_zero,
        R.string.cd_entry_title_header_one,
        R.string.cd_entry_title_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val artistSection = EntrySection.MultiText(
        R.string.cd_entry_artists_header_zero,
        R.string.cd_entry_artists_header_one,
        R.string.cd_entry_artists_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val seriesSection = EntrySection.MultiText(
        R.string.cd_entry_title_header_zero,
        R.string.cd_entry_title_header_one,
        R.string.cd_entry_title_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val characterSection = EntrySection.MultiText(
        R.string.cd_entry_characters_header_zero,
        R.string.cd_entry_characters_header_one,
        R.string.cd_entry_characters_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val tagSection = EntrySection.MultiText(
        R.string.cd_entry_tags_header_zero,
        R.string.cd_entry_tags_header_one,
        R.string.cd_entry_tags_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val notesSection = EntrySection.LongText(
        headerRes = R.string.cd_entry_notes_header,
        lockState = EntrySection.LockState.UNLOCKED
    )

    val sections = listOf(
        catalogIdSection,
        titleSection,
        artistSection,
        seriesSection,
        characterSection,
        tagSection,
        notesSection
    )

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    protected suspend fun makeEntry(imageUri: Uri?, id: String): CdEntry? {
        val outputFile = CdEntryUtils.getImageFile(application, id)
        val error = ImageUtils.writeEntryImage(application, outputFile, imageUri)
        if (error != null) {
            withContext(Dispatchers.Main) {
                errorResource = error
            }
            return null
        }
        val (imageWidth, imageHeight) = ImageUtils.getImageSize(outputFile)

        return CdEntry(
            id = id,
            catalogId = catalogIdSection.finalContents().firstOrNull()?.serializedValue,
            titles = titleSection.finalContents().map { it.serializedValue },
            artists = artistSection.finalContents().map { it.serializedValue },
            series = seriesSection.finalContents().map { it.serializedValue },
            seriesSearchable = seriesSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            characters = characterSection.finalContents().map { it.serializedValue },
            charactersSearchable = characterSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            tags = tagSection.finalContents().map { it.serializedValue },
            lastEditTime = Date.from(Instant.now()),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            notes = notesSection.value.trim(),
            locks = CdEntry.Locks(
                catalogIdLocked = catalogIdSection.lockState?.toSerializedValue(),
                titleLocked = titleSection.lockState?.toSerializedValue(),
                artistsLocked = artistSection.lockState?.toSerializedValue(),
                seriesLocked = seriesSection.lockState?.toSerializedValue(),
                charactersLocked = characterSection.lockState?.toSerializedValue(),
                tagsLocked = tagSection.lockState?.toSerializedValue(),
                notesLocked = notesSection.lockState?.toSerializedValue(),
            )
        )
    }

    suspend fun saveEntry(imageUri: Uri?, id: String) {
        val entry = makeEntry(imageUri, id) ?: return
        entry.series
            .map { aniListJson.parseSeriesColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(mediaRepository::ensureSaved)
        entry.characters
            .map { aniListJson.parseCharacterColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(characterRepository::ensureSaved)
        cdEntryDao.insertEntries(entry)
    }
}