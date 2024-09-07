package com.thekeeperofpie.artistalleydatabase.art.persistence

import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntrySyncDao
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.parseStringList
import com.thekeeperofpie.artistalleydatabase.utils_room.DatabaseSyncer
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class ArtSyncer(
    private val json: Json,
    private val artEntryDao: ArtEntrySyncDao,
    private val characterRepository: CharacterRepository,
    private val characterEntryDao: CharacterEntryDao,
    private val mediaRepository: MediaRepository,
    private val mediaEntryDao: MediaEntryDao,
) : DatabaseSyncer {

    companion object {
        private const val FETCH_PAGE_SIZE = 15
    }

    override fun getMaxProgress() = artEntryDao.getEntriesSize()

    override suspend fun sync(
        initialProgress: Int,
        maxProgress: Int,
        setProgress: suspend (progress: Int, max: Int) -> Unit
    ) {
        val characterIds = mutableListOf<String>()
        val mediaIds = mutableListOf<String>()
        repeatToLimit(artEntryDao::getCharactersAndSeries) {
            characterIds += it.map { it.charactersSerialized }
                .flatMap(json::parseStringList)
                .map { Character.parseSingle(json, it).id }
                .distinct()
                .toList()
            mediaIds += it.map { it.seriesSerialized }
                .flatMap(json::parseStringList)
                .map { Series.parseSingle(json, it).id }
                .distinct()
                .toList()
        }

        val characterIdsToFetch =
            (characterIds.distinct() - characterEntryDao.getEntriesById(characterIds).toSet())
        val mediaIdsToFetch = (mediaIds.distinct() - mediaEntryDao.getEntriesById(mediaIds).toSet())

        val maxEntryProgress = initialProgress + getMaxProgress()
        val maxRealProgress = initialProgress + characterIdsToFetch.size + mediaIdsToFetch.size
        var realProgress = initialProgress

        // TODO: This API is easily rate limited and can fail sync. Either make this backoff retry
        //  or add a note in the completion notification indicating where it stopped
        characterIdsToFetch
            .windowed(size = FETCH_PAGE_SIZE, step = FETCH_PAGE_SIZE, partialWindows = true)
            .forEach {
                characterRepository.ensureSaved(it)
                realProgress += it.size
                setProgress(
                    setProgress,
                    initialProgress,
                    maxProgress,
                    maxEntryProgress,
                    maxRealProgress,
                    realProgress
                )
            }

        mediaIdsToFetch
            .windowed(size = FETCH_PAGE_SIZE, step = FETCH_PAGE_SIZE, partialWindows = true)
            .forEach {
                mediaRepository.ensureSaved(it)
                realProgress += it.size
                setProgress(
                    setProgress,
                    initialProgress,
                    maxProgress,
                    maxEntryProgress,
                    maxRealProgress,
                    realProgress
                )
            }
    }

    private suspend fun setProgress(
        setProgress: suspend (progress: Int, max: Int) -> Unit,
        initialProgress: Int,
        maxProgress: Int,
        maxEntryProgress: Int,
        maxRealProgress: Int,
        realProgress: Int
    ) {
        val progressRatio =
            (realProgress - initialProgress) / (maxRealProgress - initialProgress).toFloat()
        setProgress(
            (progressRatio * (maxEntryProgress - initialProgress) + initialProgress).toInt(),
            maxProgress
        )
    }
}
