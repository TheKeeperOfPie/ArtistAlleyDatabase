package com.thekeeperofpie.artistalleydatabase.art.details

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.AniListCharacter
import com.anilist.fragment.AniListMedia
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.DatabaseCharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.DatabaseSeriesEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterUtils.buildDisplayName
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.PrintSizeDropdown
import com.thekeeperofpie.artistalleydatabase.art.SourceDropdown
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.utils.nullable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ArtEntryDetailsViewModel(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDao,
    private val aniListApi: AniListApi,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
    appMoshi: AppMoshi,
) : ViewModel() {

    companion object {
        private val TAG = ArtEntryDetailsViewModel::class.java.name
    }

    private val aniListSeriesEntryAdapter = appMoshi.aniListSeriesEntryAdapter
    private val aniListCharacterEntryAdapter = appMoshi.aniListCharacterEntryAdapter

    protected val seriesSection = ArtEntrySection.MultiText(
        R.string.art_entry_series_header_zero,
        R.string.art_entry_series_header_one,
        R.string.art_entry_series_header_many,
        locked = false,
    )

    protected val characterSection = ArtEntrySection.MultiText(
        R.string.art_entry_characters_header_zero,
        R.string.art_entry_characters_header_one,
        R.string.art_entry_characters_header_many,
        locked = false,
    )

    protected val sourceSection = SourceDropdown(locked = false)

    protected val artistSection = ArtEntrySection.MultiText(
        R.string.art_entry_artists_header_zero,
        R.string.art_entry_artists_header_one,
        R.string.art_entry_artists_header_many,
        locked = false,
    )
    protected val tagSection = ArtEntrySection.MultiText(
        R.string.art_entry_tags_header_zero,
        R.string.art_entry_tags_header_one,
        R.string.art_entry_tags_header_many,
        locked = false,
    )

    protected val printSizeSection = PrintSizeDropdown()

    protected val notesSection = ArtEntrySection.LongText(
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
            subscribeMultiTextSection(
                section = artistSection,
                localCall = {
                    artEntryDao.queryArtists(it)
                        .map(ArtEntrySection.MultiText.Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                })
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(
                seriesSection,
                localCall = { query ->
                    artEntryDao.querySeries(query)
                        .map {
                            if (it.contains("{")) {
                                val entry = aniListSeriesEntryAdapter.fromJson(it)
                                if (entry == null) {
                                    flowOf(ArtEntrySection.MultiText.Entry.Custom(it))
                                } else {
                                    mediaRepository.getEntry(entry.id)
                                        .filterNotNull()
                                        .map(::seriesEntry)
                                        .onStart { emit(seriesEntry(entry)) }
                                }
                            } else {
                                flowOf(ArtEntrySection.MultiText.Entry.Custom(it))
                            }
                        }
                        .ifEmpty { listOf(flowOf(null)) }
                }, networkCall = { query ->
                    aniListCall({ aniListApi.searchSeries(query) }) {
                        it.Page.media.mapNotNull { it?.aniListMedia }.map(::seriesEntry)
                    }
                }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(
                characterSection,
                localCall = { query ->
                    artEntryDao.queryCharacters(query)
                        .map {
                            if (it.contains("{")) {
                                val entry = aniListCharacterEntryAdapter.fromJson(it)
                                if (entry == null) {
                                    flowOf(ArtEntrySection.MultiText.Entry.Custom(it))
                                } else {
                                    characterRepository.getEntry(entry.id)
                                        .filterNotNull()
                                        .flatMapLatest { character ->
                                            // TODO: Batch query?
                                            character.mediaIds
                                                ?.map { mediaRepository.getEntry(it) }
                                                ?.let { combine(it) { it.toList() } }
                                                .let { it ?: flowOf(listOf(null)) }
                                                .map { character to it.filterNotNull() }
                                        }
                                        .map { characterEntry(it.first, it.second) }
                                        .onStart { emit(characterEntry(entry)) }
                                        .filterNotNull()
                                }
                            } else {
                                flowOf(ArtEntrySection.MultiText.Entry.Custom(it))
                            }
                        }
                        .ifEmpty { listOf(flowOf(null)) }
                }, networkCall = { query ->
                    aniListCall({ aniListApi.searchCharacters(query) }) {
                        it.Page.characters.filterNotNull()
                            .map { characterEntry(it.aniListCharacter) }
                    }
                }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(
                section = tagSection,
                localCall = {
                    artEntryDao.queryTags(it)
                        .map(ArtEntrySection.MultiText.Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun subscribeMultiTextSection(
        section: ArtEntrySection.MultiText,
        localCall: suspend (String) -> List<Flow<ArtEntrySection.MultiText.Entry?>>,
        networkCall: suspend (query: String) -> Flow<List<ArtEntrySection.MultiText.Entry>> = {
            flowOf(emptyList())
        },
    ) {
        section.valueUpdates()
            .flatMapLatest { query ->
                val database = combine(localCall(query)) { it.toList() }
                val aniList = if (query.isBlank()) flowOf(emptyList()) else networkCall(query)
                combine(database, aniList) { local, network ->
                    local.filterNotNull().toMutableList().apply {
                        removeIf { source ->
                            network.any { target -> target.text.trim() == source.text.trim() }
                        }
                    } + network
                }
            }
            .collectLatest {
                withContext(Dispatchers.Main) {
                    val contents = section.contents
                    section.predictions = it.toMutableList().apply {
                        removeIf { entry -> contents.any { it.text == entry.text } }
                    }
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

    private fun seriesEntry(media: AniListMedia): ArtEntrySection.MultiText.Entry.Prefilled {
        val title = media.title?.romaji ?: media.id.toString()
        val serializedValue =
            aniListSeriesEntryAdapter.toJson(DatabaseSeriesEntry(media.id, title))
        return ArtEntrySection.MultiText.Entry.Prefilled(
            id = media.id.toString(),
            text = title,
            image = media.coverImage?.medium,
            serializedValue = serializedValue,
            searchableValue = (listOf(
                media.title?.romaji,
                media.title?.english,
                media.title?.native,
            ) + media.synonyms.orEmpty())
                .filterNot(String?::isNullOrBlank)
                .joinToString()
        )
    }

    private fun seriesEntry(entry: MediaEntry): ArtEntrySection.MultiText.Entry {
        val title = entry.title
        val nonNullTitle = title?.romaji ?: entry.id.toString()
        val serializedValue =
            aniListSeriesEntryAdapter.toJson(DatabaseSeriesEntry(entry.id, nonNullTitle))
        return ArtEntrySection.MultiText.Entry.Prefilled(
            id = entry.id.toString(),
            text = nonNullTitle,
            image = entry.image?.medium,
            serializedValue = serializedValue,
            searchableValue = (listOf(
                title?.romaji,
                title?.english,
                title?.native
            ) + entry.synonyms.orEmpty())
                .filterNot(String?::isNullOrBlank)
                .joinToString()
        )
    }

    private fun seriesEntry(entry: DatabaseSeriesEntry): ArtEntrySection.MultiText.Entry {
        val serializedValue =
            aniListSeriesEntryAdapter.toJson(DatabaseSeriesEntry(entry.id, entry.title))
        return ArtEntrySection.MultiText.Entry.Prefilled(
            id = entry.id.toString(),
            text = entry.title,
            image = null,
            serializedValue = serializedValue,
            searchableValue = entry.title
        )
    }

    private fun characterEntry(character: AniListCharacter) =
        characterEntry(
            id = character.id,
            image = character.image?.medium,
            first = character.name?.first,
            middle = character.name?.middle,
            last = character.name?.last,
            full = character.name?.full,
            native = character.name?.native,
            alternative = character.name?.alternative?.filterNotNull(),
            mediaTitle = character.media?.nodes?.firstOrNull()?.aniListMedia?.title?.romaji
        )

    private fun characterEntry(entry: DatabaseCharacterEntry) =
        characterEntry(
            id = entry.id,
            image = null,
            first = entry.name?.first,
            middle = entry.name?.middle,
            last = entry.name?.last,
            full = entry.name?.full,
            native = entry.name?.native,
            alternative = emptyList(),
            mediaTitle = null,
        )

    private fun characterEntry(entry: CharacterEntry, media: List<MediaEntry>) =
        characterEntry(
            id = entry.id,
            image = entry.image?.medium,
            first = entry.name?.first,
            middle = entry.name?.middle,
            last = entry.name?.last,
            full = entry.name?.full,
            native = entry.name?.native,
            alternative = entry.name?.alternative,
            mediaTitle = media.firstOrNull()?.title?.romaji,
        )

    private fun characterEntry(
        id: Int?,
        image: String?,
        first: String?,
        middle: String?,
        last: String?,
        full: String?,
        native: String?,
        alternative: List<String>?,
        mediaTitle: String?
    ): ArtEntrySection.MultiText.Entry? {
        val canonicalName = CharacterUtils.buildCanonicalName(
            first = first,
            middle = middle,
            last = last,
        ) ?: return null

        val displayName = buildDisplayName(canonicalName, alternative)

        val serializedValue =
            aniListCharacterEntryAdapter.toJson(
                DatabaseCharacterEntry(
                    id ?: -1, DatabaseCharacterEntry.Name(
                        first = first,
                        middle = middle,
                        last = last,
                        full = full,
                        native = native,
                    )
                )
            )
        return ArtEntrySection.MultiText.Entry.Prefilled(
            id = (id ?: -1).toString(),
            text = canonicalName,
            image = image,
            titleText = displayName,
            subtitleText = mediaTitle,
            serializedValue = serializedValue,
            searchableValue = (listOf(last, middle, first) + alternative.orEmpty())
                .filterNot(String?::isNullOrBlank)
                .joinToString()
        )
    }

    protected fun databaseToSeriesEntry(value: String) =
        value.takeIf { it.contains("{") }
            ?.let { aniListSeriesEntryAdapter.fromJson(it) }
            ?.let(::seriesEntry)
            ?: ArtEntrySection.MultiText.Entry.Custom(value)

    protected fun databaseToCharacterEntry(value: String) =
        value.takeIf { it.contains("{") }
            ?.let { aniListCharacterEntryAdapter.fromJson(it) }
            ?.let { characterEntry(it) }
            ?: ArtEntrySection.MultiText.Entry.Custom(value)

    protected fun buildModel(entry: ArtEntry): ArtEntryModel {
        val artists = entry.artists.map(ArtEntrySection.MultiText.Entry::Custom)
        val series = entry.series.map(::databaseToSeriesEntry)
        val characters = entry.characters.map(::databaseToCharacterEntry)
        val tags = entry.tags.map(ArtEntrySection.MultiText.Entry::Custom)

        return ArtEntryModel(
            entry = entry,
            artists = artists,
            series = series,
            characters = characters,
            tags = tags,
        )
    }

    protected fun initializeForm(entry: ArtEntryModel) {
        val locks = entry.locks
        artistSection.contents.addAll(entry.artists)
        artistSection.locked = locks.artistsLocked

        sourceSection.initialize(entry)
        sourceSection.locked = locks.sourceLocked

        seriesSection.contents.addAll(entry.series)
        seriesSection.locked = locks.seriesLocked

        characterSection.contents.addAll(entry.characters)
        characterSection.locked = locks.charactersLocked

        printSizeSection.initialize(entry.printWidth, entry.printHeight)
        printSizeSection.locked = locks.printSizeLocked

        tagSection.contents.addAll(entry.tags)
        tagSection.locked = locks.tagsLocked

        notesSection.value = entry.notes.orEmpty()
        notesSection.locked = locks.notesLocked

        entry.characters.filterIsInstance<ArtEntrySection.MultiText.Entry.Prefilled>()
            .forEach {
                val characterId = it.id.toInt()
                viewModelScope.launch(Dispatchers.Main) {
                    characterRepository.getEntry(characterId)
                        .filterNotNull()
                        .flatMapLatest { character ->
                            // TODO: Batch query?
                            character.mediaIds
                                ?.map { mediaRepository.getEntry(it) }
                                ?.let { combine(it) { it.toList() } }
                                .let { it ?: flowOf(listOf(null)) }
                                .map { character to it.filterNotNull() }
                        }
                        .map { characterEntry(it.first, it.second) }
                        .filterNotNull()
                        .flowOn(Dispatchers.IO)
                        .collectLatest { newEntry ->
                            characterSection.contents.replaceAll { entry ->
                                if (entry is ArtEntrySection.MultiText.Entry.Prefilled &&
                                    entry.id == characterId.toString()
                                ) newEntry else entry
                            }
                        }
                }
            }

        entry.series.filterIsInstance<ArtEntrySection.MultiText.Entry.Prefilled>()
            .forEach {
                val mediaId = it.id.toInt()
                viewModelScope.launch(Dispatchers.Main) {
                    mediaRepository.getEntry(mediaId)
                        .filterNotNull()
                        .map(::seriesEntry)
                        .flowOn(Dispatchers.IO)
                        .collectLatest { newEntry ->
                            seriesSection.contents.replaceAll { entry ->
                                if (entry is ArtEntrySection.MultiText.Entry.Prefilled &&
                                    entry.id == mediaId.toString()
                                ) newEntry else entry
                            }
                        }
                }
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
            seriesSearchable = seriesSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            characters = characterSection.finalContents().map { it.serializedValue },
            charactersSearchable = characterSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
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
        entry.series
            .filter { it.contains("{") }
            .mapNotNull(aniListSeriesEntryAdapter::fromJson)
            .map { it.id }
            .forEach(mediaRepository::ensureSaved)
        entry.characters
            .filter { it.contains("{") }
            .mapNotNull(aniListCharacterEntryAdapter::fromJson)
            .map { it.id }
            .forEach(characterRepository::ensureSaved)
        artEntryDao.insertEntries(entry)
    }
}