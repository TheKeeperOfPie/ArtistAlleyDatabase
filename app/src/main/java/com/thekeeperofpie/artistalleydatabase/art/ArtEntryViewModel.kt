package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.CharactersSearchQuery
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.squareup.moshi.Moshi
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.AniListIdEntry
import com.thekeeperofpie.artistalleydatabase.utils.nullable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date

abstract class ArtEntryViewModel(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDao,
    protected val aniListApi: AniListApi,
) : ViewModel() {

    companion object {
        private val TAG = ArtEntryViewModel::class.java.name
    }

    private val moshi = Moshi.Builder().build()
    private val aniListIdEntryAdapter = moshi.adapter(AniListIdEntry::class.java)

    private val artistSection = ArtEntrySection.MultiText(
        R.string.art_entry_artists_header_zero,
        R.string.art_entry_artists_header_one,
        R.string.art_entry_artists_header_many,
        locked = false,
    )
    private val seriesSection = ArtEntrySection.MultiText(
        R.string.art_entry_series_header_zero,
        R.string.art_entry_series_header_one,
        R.string.art_entry_series_header_many,
        locked = false,
    )
    private val characterSection = ArtEntrySection.MultiText(
        R.string.art_entry_characters_header_zero,
        R.string.art_entry_characters_header_one,
        R.string.art_entry_characters_header_many,
        locked = false,
    )
    private val tagSection = ArtEntrySection.MultiText(
        R.string.art_entry_tags_header_zero,
        R.string.art_entry_tags_header_one,
        R.string.art_entry_tags_header_many,
        locked = false,
    )

    private val printSizeSection = PrintSizeDropdown()

    private val sourceSection = SourceDropdown(locked = false)

    private val notesSection = ArtEntrySection.LongText(
        headerRes = R.string.art_entry_notes_header,
        locked = false
    )

    val sections = listOf(
        seriesSection,
        characterSection,
        sourceSection,
        artistSection,
        tagSection,
        printSizeSection,
        notesSection,
    )

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun onImageSizeResult(width: Int, height: Int) {
        printSizeSection.onSizeChange(width, height)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(artistSection, artEntryDao::queryArtists)
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(seriesSection, artEntryDao::querySeries) { query ->
                aniListCall({ aniListApi.searchSeries(query) }) {
                    it.Page.media.mapNotNull { medium ->
                        val title = medium?.title?.romaji ?: return@mapNotNull null
                        ArtEntrySection.MultiText.Entry(
                            entryText = title,
                            serializedValue = "aniList: ${medium.id}"
                        )
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(characterSection, artEntryDao::queryCharacters) { query ->
                aniListCall({ aniListApi.searchCharacters(query) }) {
                    it.Page.characters.map(::characterDisplayName)
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(tagSection, artEntryDao::queryTags)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun subscribeMultiTextSection(
        section: ArtEntrySection.MultiText,
        localCall: suspend (String) -> List<String>,
        networkCall: suspend (query: String) -> Flow<List<ArtEntrySection.MultiText.Entry>> = {
            flowOf(emptyList())
        },
    ) {
        section.valueUpdates()
            .flatMapLatest { query ->
                val database = flow {
                    emit(null)
                    emit(localCall(query).map {
                        ArtEntrySection.MultiText.Entry(
                            entryText = it,
                            serializedValue = it,
                        )
                    })
                }

                val aniList = if (query.isBlank()) flowOf(emptyList()) else networkCall(query)

                database.combine(aniList) { local, network -> network + local.orEmpty() }
                    .map { it.distinctBy(ArtEntrySection.MultiText.Entry::titleText) }
            }
            .collectLatest {
                withContext(Dispatchers.Main) {
                    section.predictions = it
                }
            }
    }

    private fun <DataType : Operation.Data, ResponseType : ApolloResponse<DataType>> aniListCall(
        apiCall: () -> Flow<ResponseType>,
        transform: suspend (DataType) -> List<ArtEntrySection.MultiText.Entry?>,
    ) = apiCall()
        .nullable()
        .catch { Log.e(TAG, "Failed to search", it); emit(null) }
        .mapNotNull { it?.data }
        .map(transform)
        .map { it.filterNotNull() }
        .onStart { emit(emptyList()) }

    private fun characterDisplayName(
        character: CharactersSearchQuery.Character?
    ): ArtEntrySection.MultiText.Entry? {
        val name = character?.name ?: return null
        val canonicalName = when {
            name.last == null -> name.first
            name.first == null -> name.last
            else -> "${name.last} ${name.first}"
        }.takeUnless(String?::isNullOrBlank) ?: return null

        val displayName = canonicalName + name.alternative.orEmpty()
            .filterNot(String?::isNullOrBlank)
            .joinToString(prefix = " (", separator = ", ", postfix = ")")

        val series = character.media?.nodes
            ?.map { it?.title?.romaji?.trim() }
            .orEmpty()
            .filterNotNull()

        val collapsedSeries = series.toMutableList().apply {
            removeIf { target ->
                series.any { source ->
                    source != target && target.startsWith(source)
                }
            }
        }

        val serializedValue =
            aniListIdEntryAdapter.toJson(AniListIdEntry(character.id, displayName))
        return ArtEntrySection.MultiText.Entry(
            entryText = canonicalName,
            titleText = displayName,
            subtitleText = collapsedSeries.takeUnless(Collection<*>::isEmpty)?.firstOrNull(),
            serializedValue = serializedValue
        )
    }

    protected fun initializeForm(entry: ArtEntry) {
        artistSection.contents.addAll(entry.artists.mapAniListEntries())
        artistSection.locked = entry.locks.artistsLocked

        sourceSection.initialize(entry)
        sourceSection.locked = entry.locks.sourceLocked

        seriesSection.contents.addAll(entry.series.mapAniListEntries())
        seriesSection.locked = entry.locks.seriesLocked

        characterSection.contents.addAll(entry.characters.mapAniListEntries())
        characterSection.locked = entry.locks.charactersLocked

        printSizeSection.initialize(entry.printWidth, entry.printHeight)
        printSizeSection.locked = entry.locks.printSizeLocked

        tagSection.contents.addAll(entry.tags.map { ArtEntrySection.MultiText.Entry(it) })
        tagSection.locked = entry.locks.tagsLocked

        notesSection.value = entry.notes.orEmpty()
        notesSection.locked = entry.locks.notesLocked
    }

    private fun List<String>.mapAniListEntries() = map {
        if (it.contains("{")) {
            val aniListIdEntry =
                aniListIdEntryAdapter.fromJson(it) ?: return@map ArtEntrySection.MultiText.Entry(it)
            ArtEntrySection.MultiText.Entry(
                entryText = aniListIdEntry.displayText,
                serializedValue = it
            )
        } else {
            ArtEntrySection.MultiText.Entry(it)
        }
    }

    protected suspend fun makeEntry(imageUri: Uri?, id: String): ArtEntry? {
        val outputFile = ArtEntryUtils.getImageFile(application, id)
        val error = ArtEntryUtils.writeEntryImage(application, outputFile, imageUri)
        if (error != null) {
            withContext(Dispatchers.Main) {
                errorResource = error
            }
            return null
        }
        val (imageWidth, imageHeight) = ArtEntryUtils.getImageSize(outputFile)
        val (sourceType, sourceValue) = sourceSection.finalTypeToValue()

        return ArtEntry(
            id = id,
            artists = artistSection.finalContents().map { it.serializedValue },
            sourceType = sourceType,
            sourceValue = sourceValue,
            series = seriesSection.finalContents().map { it.serializedValue },
            characters = characterSection.finalContents().map { it.serializedValue },
            tags = tagSection.finalContents().map { it.serializedValue },
            lastEditTime = Date.from(Instant.now()),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            printWidth = printSizeSection.finalWidth(),
            printHeight = printSizeSection.finalHeight(),
            notes = notesSection.value.trim(),
            locks = ArtEntry.Locks(
                artistsLocked = artistSection.locked ?: false,
                seriesLocked = seriesSection.locked ?: false,
                charactersLocked = characterSection.locked ?: false,
                sourceLocked = sourceSection.locked ?: false,
                tagsLocked = tagSection.locked ?: false,
                notesLocked = notesSection.locked ?: false,
                printSizeLocked = printSizeSection.locked ?: false,
            )
        )
    }

    suspend fun saveEntry(imageUri: Uri?, id: String) {
        val entry = makeEntry(imageUri, id) ?: return
        artEntryDao.insertEntries(entry)
    }
}