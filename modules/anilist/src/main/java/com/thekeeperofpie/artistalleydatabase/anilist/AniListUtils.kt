package com.thekeeperofpie.artistalleydatabase.anilist

import android.util.Log
import com.anilist.NotificationsQuery
import com.anilist.fragment.AniListCharacter
import com.anilist.fragment.AniListMedia
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.http.LoggingInterceptor
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
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
        val timeInJapan = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
        val year = timeInJapan.year
        val month = timeInJapan.month
        val season = when (month) {
            Month.DECEMBER, Month.JANUARY, Month.FEBRUARY -> MediaSeason.WINTER
            Month.MARCH, Month.APRIL, Month.MAY -> MediaSeason.SPRING
            Month.JUNE, Month.JULY, Month.AUGUST -> MediaSeason.SUMMER
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER -> MediaSeason.FALL
            null -> MediaSeason.WINTER
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
            MediaSeason.UNKNOWN__ -> MediaSeason.WINTER
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
            MediaSeason.UNKNOWN__ -> MediaSeason.SUMMER
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
}

internal fun ApolloClient.Builder.addLoggingInterceptors(
    tag: String,
    networkSettings: NetworkSettings,
) = apply {
    if (BuildConfig.DEBUG) {
        val level = when (networkSettings.networkLoggingLevel.value) {
            NetworkSettings.NetworkLoggingLevel.NONE -> LoggingInterceptor.Level.NONE
            NetworkSettings.NetworkLoggingLevel.BASIC -> LoggingInterceptor.Level.BASIC
            NetworkSettings.NetworkLoggingLevel.HEADERS -> LoggingInterceptor.Level.HEADERS
            NetworkSettings.NetworkLoggingLevel.BODY -> LoggingInterceptor.Level.BODY
        }
        addHttpInterceptor(LoggingInterceptor(level) { Log.d(tag, it) })
    }
}

fun NotificationsQuery.Data.Page.Notification.isAdult() = when (this) {
    // STOPSHIP: Filter all options
    is NotificationsQuery.Data.Page.ActivityMentionNotificationNotification ->
        when (activity) {
            is NotificationsQuery.Data.Page.ActivityMentionNotificationNotification.ListActivityActivity -> activity.media?.isAdult
            is NotificationsQuery.Data.Page.ActivityMentionNotificationNotification.MessageActivityActivity,
            is NotificationsQuery.Data.Page.ActivityMentionNotificationNotification.OtherActivity,
            is NotificationsQuery.Data.Page.ActivityMentionNotificationNotification.TextActivityActivity,
            null -> false
        }
    is NotificationsQuery.Data.Page.AiringNotificationNotification -> media?.isAdult
    is NotificationsQuery.Data.Page.ActivityReplyNotificationNotification ->
        when (activity) {
            is NotificationsQuery.Data.Page.ActivityReplyNotificationNotification.ListActivityActivity -> activity.media?.isAdult
            is NotificationsQuery.Data.Page.ActivityReplyNotificationNotification.MessageActivityActivity,
            is NotificationsQuery.Data.Page.ActivityReplyNotificationNotification.OtherActivity,
            is NotificationsQuery.Data.Page.ActivityReplyNotificationNotification.TextActivityActivity,
            null -> false
        }
    is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification ->
        when (activity) {
            is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification.ListActivityActivity -> activity.media?.isAdult
            is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification.MessageActivityActivity,
            is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification.OtherActivity,
            is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification.TextActivityActivity,
            null -> false
        }
    is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification ->
        when (activity) {
            is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification.ListActivityActivity -> activity.media?.isAdult
            is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification.MessageActivityActivity,
            is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification.OtherActivity,
            is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification.TextActivityActivity,
            null -> false
        }
    is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification ->
        when (activity) {
            is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification.ListActivityActivity -> activity.media?.isAdult
            is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification.MessageActivityActivity,
            is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification.OtherActivity,
            is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification.TextActivityActivity,
            null -> false
        }
    is NotificationsQuery.Data.Page.RelatedMediaAdditionNotificationNotification -> media?.isAdult
    is NotificationsQuery.Data.Page.MediaDataChangeNotificationNotification -> media?.isAdult
    is NotificationsQuery.Data.Page.MediaMergeNotificationNotification -> media?.isAdult
    is NotificationsQuery.Data.Page.MediaDeletionNotificationNotification,
    is NotificationsQuery.Data.Page.ActivityMessageNotificationNotification,
    is NotificationsQuery.Data.Page.FollowingNotificationNotification,
    is NotificationsQuery.Data.Page.OtherNotification -> false
}
