package com.thekeeperofpie.artistalleydatabase.anilist

import com.anilist.CharactersSearchQuery
import com.anilist.MediaByIdQuery
import com.anilist.MediaSearchQuery
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.flow.map

class AniListApi {

    companion object {
        private const val SERVER_URL = "https://graphql.anilist.co/"
    }

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(SERVER_URL)
        .build()

    fun getMedia(id: Int) = apolloClient.query(MediaByIdQuery(Optional.Present(id)))
        .toFlow()
        .map { it.data?.Media }

    fun searchSeries(query: String) =
        apolloClient.query(MediaSearchQuery(Optional.Present(query))).toFlow()

    fun searchCharacters(query: String) =
        apolloClient.query(CharactersSearchQuery(
            search = Optional.Present(query),
            page = Optional.Present(0),
            perPage = Optional.Present(5),
            mediaPage = Optional.Present(0),
            mediaPerPage = Optional.Present(1),
        )).toFlow()
}