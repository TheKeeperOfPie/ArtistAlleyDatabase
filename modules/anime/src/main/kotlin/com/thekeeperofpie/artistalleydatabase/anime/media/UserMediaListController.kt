package com.thekeeperofpie.artistalleydatabase.anime.media

import android.os.SystemClock
import androidx.compose.runtime.Immutable
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.anilist.UserMediaListQuery
import com.anilist.ViewerMediaListQuery
import com.anilist.fragment.GeneralMediaTag
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.fragment.UserMediaListMedia
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.hoc081098.flowext.combine
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.foldPreviousResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.BuildConfig
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

/**
 * User's MediaListCollection API is very broken, it makes sense to query a known working complete
 * value and just split it off into the various functionality in the app.
 *
 * This also allows for easier caching/offline syncing from a central location, although that's not
 * implemented.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)
class UserMediaListController(
    private val scopedApplication: ScopedApplication,
    private val aniListApi: AuthedAniListApi,
    private val ignoreController: IgnoreController,
    private val statusController: MediaListStatusController,
    private val settings: AnimeSettings,
    private val appJson: AppJson,
    private val masterKey: MasterKey,
) {
    private val refreshAnime = MutableStateFlow(-1L)
    private val refreshManga = MutableStateFlow(-1L)

    private val includeDescription = MutableStateFlow(false)

    private val cacheDir = scopedApplication.app.cacheDir.resolve("user_media")
    private val cacheMutex = Mutex()

    private var anime: Flow<LoadingResult<List<ListEntry>>>
    private var manga: Flow<LoadingResult<List<ListEntry>>>

    init {
        anime = loadCacheAndNetwork(
            loadMediaFromCache(MediaType.ANIME),
            loadMediaFromNetwork(refreshAnime, MediaType.ANIME).foldPreviousResult(),
        ).shareIn(scopedApplication.scope, SharingStarted.Lazily, replay = 1)
        manga = loadCacheAndNetwork(
            loadMediaFromCache(MediaType.MANGA),
            loadMediaFromNetwork(refreshManga, MediaType.MANGA).foldPreviousResult(),
        ).shareIn(scopedApplication.scope, SharingStarted.Lazily, replay = 1)
    }

    private fun <T> loadCacheAndNetwork(
        cache: Flow<LoadingResult<T>>,
        network: Flow<LoadingResult<T>>,
    ) = combine(cache, network) { cacheResult, networkResult ->
        val cacheHasErrors = cacheResult.error != null
        val networkHasErrors = networkResult.error != null
        val result = if (networkResult.success && !networkResult.loading) {
            networkResult.result
        } else {
            cacheResult.result
        }
        LoadingResult(
            loading = networkResult.loading,
            success = !networkHasErrors,
            result = result,
            error = if (networkHasErrors) {
                networkResult.error
            } else if (result == null && cacheHasErrors) {
                cacheResult.error
            } else {
                null
            },
        )
    }

    private fun loadMediaFromCache(mediaType: MediaType) = flowFromSuspend {
        withContext(CustomDispatchers.IO) {
            cacheMutex.withLock {
                val cacheFile = cacheDir.resolve(
                    if (mediaType == MediaType.ANIME) {
                        "anime.json"
                    } else {
                        "manga.json"
                    }
                )

                if (!cacheFile.exists()) {
                    LoadingResult.empty()
                } else {
                    EncryptedFile.Builder(
                        scopedApplication.app,
                        cacheFile,
                        masterKey,
                        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                    ).build()
                        .openFileInput()
                        .use { appJson.json.decodeFromStream<List<ListEntry>>(it) }
                        .let { LoadingResult.success(it) }
                }
            }
        }
    }.startWith(item = LoadingResult.loading())
        .catch { emit(LoadingResult.error(R.string.anime_user_media_list_error_loading_cache, it)) }

    private fun loadMediaFromNetwork(refresh: StateFlow<Long>, mediaType: MediaType) =
        combine(aniListApi.authedUser, includeDescription, refresh, ::Triple)
            .flatMapLatest { (viewer, includeDescription) ->
                if (viewer == null) return@flatMapLatest flowOf(LoadingResult.empty())
                aniListApi.viewerMediaList(
                    userId = viewer.id,
                    type = mediaType,
                    includeDescription = includeDescription
                ).map {
                    it.transformResult {
                        it.lists?.filterNotNull()?.map(::ListEntry)
                            .orEmpty()
                    }
                }
            }
            .runningFold(
                LoadingResult<List<ListEntry>>(
                    loading = true,
                    success = true
                )
            ) { accumulator, value ->
                value.transformIf(value.loading && value.result == null) {
                    copy(result = accumulator.result)
                }
            }
            .flatMapLatest { entry ->
                combine(
                    statusController.allChanges(),
                    ignoreController.updates(),
                    settings.showAdult,
                    settings.showIgnored,
                    settings.showLessImportantTags,
                    settings.showSpoilerTags,
                ) { statuses, _, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                    applyStatus(
                        statuses = statuses,
                        showAdult = showAdult,
                        showIgnored = showIgnored,
                        showLessImportantTags = showLessImportantTags,
                        showSpoilerTags = showSpoilerTags,
                        result = entry,
                    )
                }
            }
            .flowOn(CustomDispatchers.IO)
            .onEach { writeCache(it, mediaType) }

    private fun writeCache(
        result: LoadingResult<List<ListEntry>>,
        mediaType: MediaType,
    ) {
        if (!result.success || result.result == null) return
        val lists = result.result.orEmpty()
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            try {
                cacheMutex.withLock {
                    if (!cacheDir.exists()) {
                        cacheDir.mkdir()
                    }
                    val cacheFile = cacheDir.resolve(
                        if (mediaType == MediaType.ANIME) {
                            "anime.json"
                        } else {
                            "manga.json"
                        }
                    )

                    // TODO: Atomicity
                    if (cacheFile.exists()) {
                        cacheFile.delete()
                    }

                    EncryptedFile.Builder(
                        scopedApplication.app,
                        cacheFile,
                        masterKey,
                        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                    ).build()
                        .openFileOutput()
                        .use {
                        appJson.json.encodeToStream(lists, it)
                    }
                }
            } catch (t: Throwable) {
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException("Error serializing cache", t)
                }
            }
        }
    }

    private suspend fun applyStatus(
        statuses: Map<String, MediaListStatusController.Update>,
        showAdult: Boolean,
        showIgnored: Boolean,
        showLessImportantTags: Boolean,
        showSpoilerTags: Boolean,
        result: LoadingResult<List<ListEntry>>,
    ) = result.transformResult {
        it.map {
            it.copy(entries = it.entries.mapNotNull {
                applyMediaFiltering(
                    statuses = statuses,
                    ignoreController = ignoreController,
                    showAdult = showAdult,
                    showIgnored = showIgnored,
                    showLessImportantTags = showLessImportantTags,
                    showSpoilerTags = showSpoilerTags,
                    entry = it,
                    transform = { it },
                    media = it.media,
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        MediaEntry(
                            media = media,
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            scoreRaw = scoreRaw,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    }
                )
            })
        }
    }

    fun anime(includeDescription: Boolean): Flow<LoadingResult<List<ListEntry>>> {
        if (includeDescription) {
            this.includeDescription.value = true
        }
        return anime
    }

    fun manga(includeDescription: Boolean): Flow<LoadingResult<List<ListEntry>>> {
        if (includeDescription) {
            this.includeDescription.value = true
        }
        return manga
    }

    fun refresh(mediaType: MediaType) {
        if (mediaType == MediaType.ANIME) {
            refreshAnime.value = SystemClock.uptimeMillis()
        } else {
            refreshManga.value = SystemClock.uptimeMillis()
        }
    }

    @Serializable
    data class ListEntry(
        val name: String,
        val status: MediaListStatus?,

        // TODO: This can be moved up a level
        val scoreFormat: ScoreFormat?,
        val entries: List<MediaEntry>,
    ) {
        constructor(list: ViewerMediaListQuery.Data.MediaListCollection.List) : this(
            name = list.name.orEmpty(),
            status = list.status,
            scoreFormat = null,
            entries = list.entries?.filterNotNull()
                ?.map { MediaEntry(media = MediaEntry.Media(it.media), authorData = null) }
                .orEmpty()
        )

        constructor(
            scoreFormat: ScoreFormat?,
            list: UserMediaListQuery.Data.MediaListCollection.List,
        ) : this(
            name = list.name.orEmpty(),
            status = list.status,
            scoreFormat = scoreFormat,
            entries = list.entries?.filterNotNull()
                ?.map { MediaEntry(list.status, it) }
                .orEmpty()
        )
    }

    @Immutable
    @Serializable
    data class MediaEntry(
        val media: Media,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = media.mediaListEntry?.progress,
        override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
        override val scoreRaw: Double? = media.mediaListEntry?.score,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
        val authorData: AuthorData? = null,
    ) : MediaStatusAware {
        constructor(
            status: MediaListStatus?,
            entry: UserMediaListQuery.Data.MediaListCollection.List.Entry,
        ) : this(
            media = Media(entry.media),
            authorData = AuthorData(
                status = status,
                rawScore = entry.score,
                progress = entry.progress,
            ),
        )

        @Serializable
        data class AuthorData(
            val status: MediaListStatus?,
            val rawScore: Double?,
            val progress: Int?,
        )

        @Serializable
        data class Media(
            override val __typename: String = "Default",
            override val id: Int,
            override val title: Title? = null,
            override val coverImage: CoverImage? = null,
            override val type: MediaType? = null,
            override val isAdult: Boolean? = null,
            override val bannerImage: String? = null,
            override val format: MediaFormat? = null,
            override val status: MediaStatus? = null,
            override val season: MediaSeason? = null,
            override val seasonYear: Int? = null,
            override val episodes: Int? = null,
            override val averageScore: Int? = null,
            override val popularity: Int? = null,
            override val nextAiringEpisode: NextAiringEpisode? = null,
            override val isFavourite: Boolean = false,
            override val chapters: Int? = null,
            override val volumes: Int? = null,
            override val mediaListEntry: MediaListEntry? = null,
            override val tags: List<Tag?>? = null,
            override val genres: List<String?>? = null,
            override val source: MediaSource? = null,
            override val startDate: Date? = null,
            override val externalLinks: List<ExternalLink?>? = null,
            override val description: String? = null,
            val synonyms: List<String?>? = null,
            val endDate: Date? = null,
            val updatedAt: Int? = null,
        ) : MediaPreviewWithDescription {
            constructor(media: UserMediaListMedia) : this(
                __typename = media.__typename,
                id = media.id,
                title = media.title?.let(::Title),
                coverImage = media.coverImage?.let(::CoverImage),
                type = media.type,
                isAdult = media.isAdult,
                bannerImage = media.bannerImage,
                format = media.format,
                status = media.status,
                season = media.season,
                seasonYear = media.seasonYear,
                episodes = media.episodes,
                averageScore = media.averageScore,
                popularity = media.popularity,
                nextAiringEpisode = media.nextAiringEpisode?.let(::NextAiringEpisode),
                isFavourite = media.isFavourite,
                chapters = media.chapters,
                volumes = media.volumes,
                mediaListEntry = media.mediaListEntry?.let(::MediaListEntry),
                tags = media.tags?.filterNotNull()?.map(::Tag),
                genres = media.genres,
                source = media.source,
                startDate = media.startDate?.let(::Date),
                externalLinks = media.externalLinks?.filterNotNull()?.map(::ExternalLink),
                description = media.description,
                synonyms = media.synonyms,
                endDate = media.endDate?.let(::Date),
                updatedAt = media.updatedAt,
            )

            @Serializable
            data class Title(
                override val __typename: String = "Default",
                override val userPreferred: String? = null,
                override val romaji: String? = null,
                override val english: String? = null,
                override val native: String? = null,
            ) : MediaPreviewWithDescription.Title {
                constructor(title: MediaPreviewWithDescription.Title) : this(
                    __typename = title.__typename,
                    userPreferred = title.userPreferred,
                    romaji = title.romaji,
                    english = title.english,
                    native = title.native,
                )
            }

            @Serializable
            data class CoverImage(
                override val extraLarge: String? = null,
                override val color: String? = null,
            ) : MediaPreviewWithDescription.CoverImage {
                constructor(coverImage: MediaPreviewWithDescription.CoverImage) : this(
                    extraLarge = coverImage.extraLarge,
                    color = coverImage.color,
                )
            }

            @Serializable
            data class NextAiringEpisode(
                override val episode: Int = 0,
                override val airingAt: Int = 0,
            ) : MediaPreviewWithDescription.NextAiringEpisode {
                constructor(nextAiringEpisode: MediaPreviewWithDescription.NextAiringEpisode) : this(
                    episode = nextAiringEpisode.episode,
                    airingAt = nextAiringEpisode.airingAt,
                )
            }

            @Serializable
            data class MediaListEntry(
                override val id: Int,
                override val status: MediaListStatus? = null,
                override val progressVolumes: Int? = null,
                override val progress: Int? = null,
                override val score: Double? = null,
                val priority: Int? = null,
                val createdAt: Int? = null,
                val updatedAt: Int? = null,
            ) : MediaPreviewWithDescription.MediaListEntry {
                constructor(mediaListEntry: UserMediaListMedia.MediaListEntry) : this(
                    id = mediaListEntry.id,
                    status = mediaListEntry.status,
                    progressVolumes = mediaListEntry.progressVolumes,
                    progress = mediaListEntry.progress,
                    score = mediaListEntry.score,
                    priority = mediaListEntry.priority,
                    createdAt = mediaListEntry.createdAt,
                    updatedAt = mediaListEntry.updatedAt,
                )
            }

            @Serializable
            data class Tag(
                override val __typename: String,
                override val id: Int,
                override val name: String,
                override val category: String? = null,
                override val isAdult: Boolean? = null,
                override val isGeneralSpoiler: Boolean? = null,
                override val isMediaSpoiler: Boolean? = null,
                override val rank: Int? = null,
            ) : GeneralMediaTag, MediaPreviewWithDescription.Tag {
                constructor(tag: UserMediaListMedia.Tag) : this(
                    __typename = tag.__typename,
                    id = tag.id,
                    name = tag.name,
                    category = tag.category,
                    isAdult = tag.isAdult,
                    isGeneralSpoiler = tag.isGeneralSpoiler,
                    isMediaSpoiler = tag.isMediaSpoiler,
                    rank = tag.rank,
                )
            }

            @Serializable
            data class Date(
                override val year: Int? = null,
                override val month: Int? = null,
                override val day: Int? = null,
            ) : MediaPreviewWithDescription.StartDate, UserMediaListMedia.EndDate {
                constructor(
                    startDate: UserMediaListMedia.StartDate,
                ) : this(
                    year = startDate.year,
                    month = startDate.month,
                    day = startDate.day,
                )

                constructor(
                    endDate: UserMediaListMedia.EndDate,
                ) : this(
                    year = endDate.year,
                    month = endDate.month,
                    day = endDate.day,
                )
            }

            @Serializable
            data class ExternalLink(
                override val siteId: Int? = null,
            ) : MediaPreviewWithDescription.ExternalLink {
                constructor(link: MediaPreviewWithDescription.ExternalLink) : this(
                    siteId = link.siteId,
                )
            }
        }
    }
}
