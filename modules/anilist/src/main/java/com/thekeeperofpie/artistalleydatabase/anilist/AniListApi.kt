package com.thekeeperofpie.artistalleydatabase.anilist

import com.anilist.CharacterByIdQuery
import com.anilist.CharactersSearchQuery
import com.anilist.MediaByIdQuery
import com.anilist.MediaSearchQuery
import com.anilist.MediaWithCharactersQuery
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class AniListApi {

    companion object {
        private const val SERVER_URL = "https://graphql.anilist.co/"
    }

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(SERVER_URL)
        .build()

    fun getMedia(id: String) = apolloClient.query(MediaByIdQuery(Optional.Present(id.toInt())))
        .toFlow()
        .map { it.data?.Media }

    fun getCharacter(id: String) =
        apolloClient.query(CharacterByIdQuery(Optional.Present(id.toInt())))
            .toFlow()
            .map { it.data?.Character }

    fun searchSeries(query: String) =
        apolloClient.query(
            MediaSearchQuery(
                search = Optional.Present(query),
                page = Optional.Present(0),
                perPage = Optional.Present(10),
            )
        ).toFlow()

    fun searchCharacters(query: String) =
        apolloClient.query(
            CharactersSearchQuery(
                search = Optional.Present(query),
                page = Optional.Present(0),
                perPage = Optional.Present(10),
                mediaPage = Optional.Present(0),
                mediaPerPage = Optional.Present(1),
            )
        ).toFlow()

    fun charactersByMedia(mediaId: String) =
        apolloClient.query(
            MediaWithCharactersQuery(
                mediaId = Optional.Present(mediaId.toInt()),
                page = Optional.Present(0),
                perPage = Optional.Present(25),
                mediaPage = Optional.Absent,
                mediaPerPage = Optional.Absent,
            )
        ).toFlow()
            .mapNotNull {
                it.data?.Media?.characters?.nodes
                    ?.mapNotNull { it?.aniListCharacter }
            }
}