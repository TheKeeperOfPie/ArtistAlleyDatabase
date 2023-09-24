package com.thekeeperofpie.artistalleydatabase.server

import com.anilist.server.api.model.types.Media
import com.anilist.server.api.model.types.MediaCoverImage
import com.anilist.server.api.model.types.MediaFormat
import com.anilist.server.api.model.types.MediaSeason
import com.anilist.server.api.model.types.MediaStatus
import com.anilist.server.api.model.types.MediaTag
import com.anilist.server.api.model.types.MediaTitle
import com.anilist.server.api.model.types.MediaType
import kotlin.math.absoluteValue
import kotlin.random.Random

object MockMedia {

    val allMedia = mutableMapOf<Int, Media>()

    fun generate(arguments: Map<String, Any>): Media {
        val id = Random.nextInt().absoluteValue
        val type = (arguments["type"] as? String)?.let(MediaType::valueOf) ?: MediaType.ANIME
        val media = Media(
            id = { id },
            bannerImage = { MockImages.bannerImage(id) },
            coverImage = {
                MediaCoverImage(
                    extraLarge = { MockImages.coverImage(id) },
                    color = { MockImages.coverImageColor(id) },
                )
            },
            title = {
                MediaTitle(
                    userPreferred = { "${id}_userPreferredTitle" },
                    romaji = { "${id}_romajiTitle" },
                    english = { "${id}_englishTitle" },
                    native = { "${id}_nativeTitle" },
                )
            },
            type = { type },
            format = { if (type == MediaType.ANIME) MediaFormat.TV else MediaFormat.MANGA },
            status = { MediaStatus.entries.random() },
            season = { MediaSeason.entries.random() },
            seasonYear = { Random.nextInt(0, 3000) },
            description = { "${id}_description" },
            averageScore = { Random.nextInt(0, 100) },
            popularity = { Random.nextInt(0, 100) },
            episodes = { if (type == MediaType.ANIME) Random.nextInt(1, 150) else null },
            chapters = { if (type == MediaType.MANGA) Random.nextInt(1, 150) else null },
            volumes = { if (type == MediaType.MANGA) Random.nextInt(1, 50) else null },
            nextAiringEpisode = { null },
            tags = { mockTags(id) },
            mediaListEntry = {
                // TODO: Handle authed
                null
            },
            isAdult = { arguments["isAdult"] as? Boolean },
        )
        allMedia[media.id] = media
        return media
    }

    // TODO: Real mock tags
    private fun mockTags(id: Int) = emptyList<MediaTag>()
}
