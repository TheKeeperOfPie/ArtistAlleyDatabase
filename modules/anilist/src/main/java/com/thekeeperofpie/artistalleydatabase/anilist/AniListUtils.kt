package com.thekeeperofpie.artistalleydatabase.anilist

import android.util.Log
import com.anilist.fragment.AniListCharacter
import com.anilist.fragment.AniListMedia
import com.anilist.type.MediaType
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.http.LoggingInterceptor
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings

object AniListUtils {

    const val GRAPHQL_API_HOST = "graphql.anilist.co"
    const val GRAPHQL_API_URL = "https://$GRAPHQL_API_HOST/"
    private const val ANILIST_BASE_URL = "https://anilist.co"

    fun characterUrl(id: String) = "$ANILIST_BASE_URL/character/$id"

    fun staffUrl(id: String) = "$ANILIST_BASE_URL/staff/$id"

    fun mediaUrl(type: MediaType?, id: String) = when (type) {
        MediaType.ANIME -> animeUrl(id)
        MediaType.MANGA -> mangaUrl(id)
        else -> null
    }

    fun mediaUrl(type: MediaEntry.Type?, id: String) = when (type) {
        MediaEntry.Type.ANIME -> animeUrl(id)
        MediaEntry.Type.MANGA -> mangaUrl(id)
        else -> null
    }

    fun animeUrl(id: String) = "$ANILIST_BASE_URL/anime/$id"
    fun mangaUrl(id: String) = "$ANILIST_BASE_URL/manga/$id"

    fun studioUrl(id: String) = "$ANILIST_BASE_URL/studio/$id"

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
