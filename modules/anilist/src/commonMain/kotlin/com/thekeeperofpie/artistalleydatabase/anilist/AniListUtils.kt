package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import com.anilist.data.fragment.AniListCharacter
import com.anilist.data.fragment.AniListMedia
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaType
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.http.LoggingInterceptor
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.utils.BuildVariant
import com.thekeeperofpie.artistalleydatabase.utils.isDebug
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.jacobras.humanreadable.HumanReadable
import kotlin.math.absoluteValue

object AniListUtils {

    const val GRAPHQL_API_HOST = "graphql.anilist.co"
    const val GRAPHQL_API_URL = "https://$GRAPHQL_API_HOST/"
    const val ANILIST_BASE_URL = "https://anilist.co"

    const val COVER_IMAGE_WIDTH_TO_HEIGHT_RATIO = 0.707f

    fun characterUrl(id: String) = "$ANILIST_BASE_URL/character/$id"

    fun staffUrl(id: String) = "$ANILIST_BASE_URL/staff/$id"

    fun mediaUrl(type: MediaType, id: String) = when (type) {
        MediaType.ANIME -> animeUrl(id)
        MediaType.MANGA -> mangaUrl(id)
        MediaType.UNKNOWN__ -> animeUrl(id)
    }

    fun mediaUrl(type: MediaEntry.Type?, id: String) = when (type) {
        MediaEntry.Type.ANIME -> animeUrl(id)
        MediaEntry.Type.MANGA -> mangaUrl(id)
        else -> null
    }

    fun animeUrl(id: String) = "$ANILIST_BASE_URL/anime/$id"
    fun mangaUrl(id: String) = "$ANILIST_BASE_URL/manga/$id"

    fun studioUrl(id: String) = "$ANILIST_BASE_URL/studio/$id"

    fun reviewUrl(id: String) = "$ANILIST_BASE_URL/review/$id"

    fun activityUrl(id: String) = "$ANILIST_BASE_URL/activity/$id"

    fun forumThreadUrl(id: String) = "$ANILIST_BASE_URL/forum/thread/$id"
    fun forumThreadCommentUrl(threadId: String, commentId: String) =
        "$ANILIST_BASE_URL/forum/thread/$threadId/comment/$commentId"

    fun mediaId(entry: Entry) = when (val value = (entry as? Entry.Prefilled<*>)?.value) {
        is AniListMedia -> value.id.toString()
        is MediaEntry -> value.id
        is MediaColumnEntry -> value.id
        else -> null
    }

    fun characterId(entry: Entry) = when (val value = (entry as? Entry.Prefilled<*>)?.value) {
        is AniListCharacter -> value.id.toString()
        is CharacterEntry -> value.id
        is CharacterColumnEntry -> value.id
        else -> null
    }

    fun getCurrentSeasonYear(): Pair<MediaSeason, Int> {
        val timeInJapan = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Tokyo"))
        val year = timeInJapan.year
        val month = timeInJapan.month
        val season = when (month) {
            Month.DECEMBER, Month.JANUARY, Month.FEBRUARY -> MediaSeason.WINTER
            Month.MARCH, Month.APRIL, Month.MAY -> MediaSeason.SPRING
            Month.JUNE, Month.JULY, Month.AUGUST -> MediaSeason.SUMMER
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER -> MediaSeason.FALL
        }

        // If it's December, the current season is Winter of the next year
        return if (month == Month.DECEMBER) {
            season to (year + 1)
        } else {
            season to year
        }
    }

    fun getNextSeasonYear(currentSeasonYear: Pair<MediaSeason, Int> = getCurrentSeasonYear()): Pair<MediaSeason, Int> {
        val (thisSeason, year) = currentSeasonYear
        val nextSeason = when (thisSeason) {
            MediaSeason.WINTER -> MediaSeason.SPRING
            MediaSeason.SPRING -> MediaSeason.SUMMER
            MediaSeason.SUMMER -> MediaSeason.FALL
            MediaSeason.FALL,
            MediaSeason.UNKNOWN__,
            -> MediaSeason.WINTER
        }

        return if (thisSeason == MediaSeason.FALL) {
            nextSeason to (year + 1)
        } else {
            nextSeason to year
        }
    }

    fun getPreviousSeasonYear(currentSeasonYear: Pair<MediaSeason, Int> = getCurrentSeasonYear()): Pair<MediaSeason, Int> {
        val (thisSeason, year) = currentSeasonYear
        val previousSeason = when (thisSeason) {
            MediaSeason.WINTER -> MediaSeason.FALL
            MediaSeason.SPRING -> MediaSeason.WINTER
            MediaSeason.SUMMER -> MediaSeason.SPRING
            MediaSeason.FALL,
            MediaSeason.UNKNOWN__,
            -> MediaSeason.SUMMER
        }

        return if (thisSeason == MediaSeason.WINTER) {
            previousSeason to (year - 1)
        } else {
            previousSeason to year
        }
    }

    fun calculateSeasonYearWithOffset(
        seasonYear: Pair<MediaSeason, Int>,
        offset: Int,
    ): Pair<MediaSeason, Int> {
        val (season, year) = seasonYear
        val newYear = year - offset / 4
        var newSeasonYear = season to newYear
        val remainder = offset % 4
        repeat(remainder.absoluteValue) {
            newSeasonYear = if (remainder < 0) {
                getNextSeasonYear(newSeasonYear)
            } else {
                getPreviousSeasonYear(newSeasonYear)
            }
        }
        return newSeasonYear
    }

    fun relativeTimestamp(timestamp: Int) =
        HumanReadable.timeAgo(Instant.fromEpochSeconds(timestamp.toLong()))

    @Composable
    fun <T : Any> selectVoiceActor(
        map: Map<out String?, T>?,
        voiceActorLanguage: VoiceActorLanguageOption = LocalLanguageOptionVoiceActor.current.first,
    ): T? {
        map ?: return null
        val (_, showFallback) = LocalLanguageOptionVoiceActor.current
        return map[voiceActorLanguage.apiValue]
            ?: (if (showFallback) map.entries.firstOrNull()?.value else null)
    }
}

fun ApolloClient.Builder.addLoggingInterceptors(
    tag: String,
    networkSettings: NetworkSettings,
) = apply {
    if (BuildVariant.isDebug()) {
        val level = when (networkSettings.networkLoggingLevel.value) {
            NetworkSettings.NetworkLoggingLevel.NONE -> LoggingInterceptor.Level.NONE
            NetworkSettings.NetworkLoggingLevel.BASIC -> LoggingInterceptor.Level.BASIC
            NetworkSettings.NetworkLoggingLevel.HEADERS -> LoggingInterceptor.Level.HEADERS
            NetworkSettings.NetworkLoggingLevel.BODY -> LoggingInterceptor.Level.BODY
        }
        addHttpInterceptor(LoggingInterceptor(level) { Logger.d(tag) { it } })
    }
}

fun LocalDate.toAniListFuzzyDateInt(): Int? {
    val monthString = monthNumber.toString().padStart(2, '0')
    val dayOfMonthString = dayOfMonth.toString().padStart(2, '0')
    return "$year$monthString$dayOfMonthString".toIntOrNull()
}
