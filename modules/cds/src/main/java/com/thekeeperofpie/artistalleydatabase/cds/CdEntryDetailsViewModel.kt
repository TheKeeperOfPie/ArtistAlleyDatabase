package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.ImageUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbParser
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date

abstract class CdEntryDetailsViewModel(
    protected val application: Application,
    protected val cdEntryDao: CdEntryDetailsDao,
    private val aniListApi: AniListApi,
    private val aniListJson: AniListJson,
    private val vgmdbJson: VgmdbJson,
    protected val dataConverter: CdEntryDataConverter,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "CdEntryDetailsViewModel"
    }

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

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.valueUpdates()
                .filter { it.length > 4 }
                .mapLatest {
                    try {
                        VgmdbParser().search(it)
                            ?.albums
                            ?.take(5)
                            ?.mapNotNull {
                                try {
                                    VgmdbParser().parseAlbum(it.id)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error fetching album $it", e)
                                    null
                                }
                            }
                            ?.map(dataConverter::catalogEntry)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error searching $it", e)
                        null
                    }
                }
                .mapNotNull { it }
                .flowOn(Dispatchers.IO)
                .collect { catalogIdSection.predictions = it }
        }

        viewModelScope.launch(Dispatchers.Main) {
            catalogIdSection.predictionChosen
                .filterIsInstance<Entry.Prefilled<AlbumEntry>>()
                .map { dataConverter.titleEntry(it.value) }
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    titleSection.addContent(it)
                }
        }
    }

    protected fun buildModel(entry: CdEntry): CdEntryModel {
        val titles = entry.titles.map(Entry::Custom)
        val artists = entry.artists.map(Entry::Custom)
        val series = entry.series.map(Entry::Custom)
        val characters = entry.characters.map(Entry::Custom)
        val tags = entry.tags.map(Entry::Custom)

        return CdEntryModel(
            entry = entry,
            titles = titles,
            artists = artists,
            series = series,
            characters = characters,
            tags = tags,
        )
    }

    protected fun initializeForm(entry: CdEntryModel) {
        catalogIdSection.setContents(listOf(Entry.Custom(entry.catalogId.orEmpty())))

        titleSection.setContents(entry.titles)
        titleSection.lockState = entry.titlesLocked

        artistSection.setContents(entry.artists)
        artistSection.lockState = entry.artistsLocked

        seriesSection.setContents(entry.series)
        seriesSection.lockState = entry.seriesLocked

        characterSection.setContents(entry.characters)
        characterSection.lockState = entry.charactersLocked

        tagSection.setContents(entry.tags)
        tagSection.lockState = entry.tagsLocked

        notesSection.value = entry.notes.orEmpty()
        notesSection.lockState = entry.notesLocked
    }

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
                titlesLocked = titleSection.lockState?.toSerializedValue(),
                artistsLocked = artistSection.lockState?.toSerializedValue(),
                seriesLocked = seriesSection.lockState?.toSerializedValue(),
                charactersLocked = characterSection.lockState?.toSerializedValue(),
                tagsLocked = tagSection.lockState?.toSerializedValue(),
                notesLocked = notesSection.lockState?.toSerializedValue(),
            )
        )
    }

    suspend fun saveEntry(imageUri: Uri?, id: String): Boolean {
        val entry = makeEntry(imageUri, id) ?: return false
        entry.series
            .map { aniListJson.parseSeriesColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(mediaRepository::ensureSaved)
        entry.characters
            .map { aniListJson.parseCharacterColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(characterRepository::ensureSaved)
        cdEntryDao.insertEntries(entry)
        return true
    }
}