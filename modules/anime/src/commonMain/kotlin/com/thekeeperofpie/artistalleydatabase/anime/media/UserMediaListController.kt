package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.Immutable
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_user_media_list_error_loading_cache
import co.touchlab.kermit.Logger
import com.anilist.data.UserMediaListQuery
import com.anilist.data.ViewerMediaListQuery
import com.anilist.data.fragment.AniListDate
import com.anilist.data.fragment.GeneralMediaTag
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.fragment.UserMediaListMedia
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaSource
import com.anilist.data.type.MediaStatus
import com.anilist.data.type.MediaType
import com.anilist.data.type.ScoreFormat
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDateSerializer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.BuildVariant
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.resolve
import com.thekeeperofpie.artistalleydatabase.utils.isDebug
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.foldPreviousResult
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
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import me.tatarka.inject.annotations.Inject

/**
 * User's MediaListCollection API is very broken, it makes sense to query a known working complete
 * value and just split it off into the various functionality in the app.
 *
 * This also allows for easier caching/offline syncing from a central location, although that's not
 * implemented.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)
@SingletonScope
@Inject
class UserMediaListController(
    private val appFileSystem: AppFileSystem,
    private val scope: ApplicationScope,
    private val aniListApi: AuthedAniListApi,
    private val ignoreController: IgnoreController,
    private val statusController: MediaListStatusController,
    private val settings: AnimeSettings,
    private val json: Json,
) {
    companion object {
        private const val TAG = "UserMediaListController"
    }

    private val refreshAnime = MutableStateFlow(-1L)
    private val refreshManga = MutableStateFlow(-1L)

    private val includeDescription = MutableStateFlow(false)

    private val cacheDir = appFileSystem.cachePath("user_media")
    private val cacheMutex = Mutex()

    private var anime: Flow<LoadingResult<List<ListEntry>>>
    private var manga: Flow<LoadingResult<List<ListEntry>>>

    init {
        anime = loadCacheAndNetwork(
            loadMediaFromCache(MediaType.ANIME),
            loadMediaFromNetwork(refreshAnime, MediaType.ANIME).foldPreviousResult(),
        ).shareIn(scope, SharingStarted.Lazily, replay = 1)
        manga = loadCacheAndNetwork(
            loadMediaFromCache(MediaType.MANGA),
            loadMediaFromNetwork(refreshManga, MediaType.MANGA).foldPreviousResult(),
        ).shareIn(scope, SharingStarted.Lazily, replay = 1)
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
                val cachePath = cacheDir.resolve(
                    if (mediaType == MediaType.ANIME) {
                        "anime.json"
                    } else {
                        "manga.json"
                    }
                )

                if (!appFileSystem.exists(cachePath)) {
                    LoadingResult.empty()
                } else {
                    appFileSystem.openEncryptedSource(cachePath)
                        .use { json.decodeFromSource<List<ListEntry>>(it) }
                        .let { LoadingResult.success(it) }
                }
            }
        }
    }.startWith(item = LoadingResult.loading())
        .catch {
            Logger.e(TAG, it) { "Error loading from user media list cache" }
            emit(LoadingResult.error(Res.string.anime_user_media_list_error_loading_cache, it))
        }

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
                    settings.mediaFilteringData(),
                ) { statuses, _, filteringData ->
                    applyStatus(
                        statuses = statuses,
                        mediaFilteringData = filteringData,
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
        scope.launch(CustomDispatchers.IO) {
            try {
                cacheMutex.withLock {
                    appFileSystem.createDirectories(cacheDir)
                    val cacheFile = cacheDir.resolve(
                        if (mediaType == MediaType.ANIME) {
                            "anime.json"
                        } else {
                            "manga.json"
                        }
                    )

                    // TODO: Atomicity
                    appFileSystem.delete(cacheFile)

                    appFileSystem.openEncryptedSink(cacheFile)
                        .use { json.encodeToSink(lists, it) }
                }
            } catch (t: Throwable) {
                if (BuildVariant.isDebug()) {
                    throw IllegalStateException("Error serializing cache", t)
                }
            }
        }
    }

    private suspend fun applyStatus(
        statuses: Map<String, MediaListStatusController.Update>,
        mediaFilteringData: MediaFilteringData,
        result: LoadingResult<List<ListEntry>>,
    ) = result.transformResult {
        it.map {
            it.copy(entries = it.entries.mapNotNull {
                applyMediaFiltering(
                    statuses = statuses,
                    ignoreController = ignoreController,
                    filteringData = mediaFilteringData,
                    entry = it,
                    filterableData = it.mediaFilterable,
                    copy = { copy(mediaFilterable = it) }
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
            refreshAnime
        } else {
            refreshManga
        }.value = Clock.System.now().toEpochMilliseconds()
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
                ?.map(::MediaEntry)
                .orEmpty()
        )
    }

    @Immutable
    @Serializable
    data class MediaEntry(
        val media: Media,
        val mediaFilterable: MediaFilterableData = MediaFilterableData(
            mediaId = media.id.toString(),
            isAdult = media.isAdult,
            mediaListStatus = media.mediaListEntry?.status?.toMediaListStatus(),
            progress = media.mediaListEntry?.progress,
            progressVolumes = media.mediaListEntry?.progressVolumes,
            scoreRaw = media.mediaListEntry?.score,
            ignored = false,
            showLessImportantTags = false,
            showSpoilerTags = false,
        ),
        val authorData: AuthorData? = null,
    ) {
        constructor(
            entry: UserMediaListQuery.Data.MediaListCollection.List.Entry,
        ) : this(
            media = Media(entry.media),
            authorData = AuthorData(
                status = entry.status,
                rawScore = entry.score,
                progress = entry.progress,
                createdAt = entry.createdAt,
                updatedAt = entry.updatedAt,
                startedAt = entry.startedAt,
                completedAt = entry.completedAt,
            ),
        )

        @Serializable
        data class AuthorData(
            val status: MediaListStatus?,
            val rawScore: Double?,
            val progress: Int?,
            val createdAt: Int?,
            val updatedAt: Int?,
            val startedAt: AniListDate?,
            val completedAt: AniListDate?,
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
                @Serializable(AniListDateSerializer::class)
                val startedAt: AniListDate? = null,
                @Serializable(AniListDateSerializer::class)
                val completedAt: AniListDate? = null,
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
                    startedAt = mediaListEntry.startedAt,
                    completedAt = mediaListEntry.completedAt,
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
            ) : MediaPreviewWithDescription.StartDate, UserMediaListMedia.EndDate, AniListDate {
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

                override val __typename = "Default"
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
