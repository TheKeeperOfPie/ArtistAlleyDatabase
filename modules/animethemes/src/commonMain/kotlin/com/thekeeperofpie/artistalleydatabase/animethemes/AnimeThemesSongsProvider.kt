package com.thekeeperofpie.artistalleydatabase.animethemes

import com.anilist.data.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongEntry
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsProvider
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class AnimeThemesSongsProvider(private val animeThemesApi: AnimeThemesApi) : AnimeSongsProvider {

    override suspend fun getSongs(media: MediaDetailsQuery.Data.Media): List<AnimeSongEntry> {
        val anime = animeThemesApi.getAnime(media.id.toString())
        return anime?.animethemes
            ?.flatMap { animeTheme ->
                animeTheme.animeThemeEntries.map {
                    val video = it.videos.firstOrNull()
                    AnimeSongEntry(
                        id = it.id,
                        type = when (animeTheme.type) {
                            AnimeTheme.Type.Opening -> AnimeSongEntry.Type.OP
                            AnimeTheme.Type.Ending -> AnimeSongEntry.Type.ED
                            null -> null
                        },
                        title = animeTheme.song?.title.orEmpty(),
                        spoiler = it.spoiler,
                        artists = animeTheme.song?.artists
                            ?.map { buildArtist(media, it) }
                            .orEmpty(),
                        episodes = it.episodes,
                        videoUrl = video?.link,
                        audioUrl = video?.audio?.link,
                        link = AnimeThemesUtils.buildWebsiteLink(
                            anime,
                            animeTheme,
                            it
                        ),
                    )
                }
            }
            .orEmpty()
    }

    // TODO: Something better than exact string matching
    private fun buildArtist(
        media: MediaDetailsQuery.Data.Media,
        artist: AnimeTheme.Song.Artist,
    ): AnimeSongEntry.Artist {
        val edges = media.characters?.edges?.filterNotNull()
        val edge = edges?.find {
            val characterName = it.node.name ?: return@find false
            characterName.full == artist.character ||
                    (characterName.alternative?.any { it == artist.character } == true)
        }

        val voiceActor = edges
            ?.flatMap { it.voiceActors?.filterNotNull().orEmpty() }
            ?.firstOrNull {
                it.name?.full == artist.name ||
                        (it.name?.alternative?.any { it == artist.name } == true)
            }

        // If character search failed, but voiceActor succeeded, try to find the character again
        val characterEdge = edge ?: voiceActor?.let {
            edges.find {
                it.voiceActors?.contains(voiceActor) == true
            }
        }

        val character = characterEdge?.node?.let {
            AnimeSongEntry.Artist.Character(
                aniListId = it.id.toString(),
                image = it.image?.large,
                name = it.name,
                fallbackName = artist.character,
            )
        }

        val artistImage = voiceActor?.image?.large
            ?: (artist.images.firstOrNull {
                it.facet == AnimeTheme.Song.Artist.Images.Facet.SmallCover
            } ?: artist.images.firstOrNull())?.link

        val aniListId = voiceActor?.id?.toString()
        return AnimeSongEntry.Artist(
            id = artist.id,
            aniListId = aniListId,
            animeThemesSlug = artist.slug,
            name = artist.name,
            image = artistImage,
            asCharacter = !artist.character.isNullOrBlank(),
            character = character,
            link = if (aniListId != null) {
                AniListUtils.staffUrl(aniListId)
            } else {
                AnimeThemesUtils.artistUrl(artist.slug)
            }
        )
    }
}
