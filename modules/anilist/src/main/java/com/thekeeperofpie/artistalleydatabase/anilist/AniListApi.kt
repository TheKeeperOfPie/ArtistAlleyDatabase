package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.annotation.Size
import com.anilist.CharacterByIdQuery
import com.anilist.CharacterByIds10Query
import com.anilist.CharacterByIds15Query
import com.anilist.CharacterByIds5Query
import com.anilist.CharactersSearchQuery
import com.anilist.MediaByIdQuery
import com.anilist.MediaByIds10Query
import com.anilist.MediaByIds15Query
import com.anilist.MediaByIds5Query
import com.anilist.MediaSearchQuery
import com.anilist.MediaWithCharactersQuery
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.thekeeperofpie.artistalleydatabase.android_utils.splitAtIndex
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
        .map { it.data?.Media?.aniListMedia }

    suspend fun getMedias(ids: List<Int>) = getMultiple(
        ids = ids,
        get1 = ::getMedia1,
        get5 = ::getMedias5,
        get10 = ::getMedias10,
        get15 = ::getMedias15,
    )

    fun getCharacter(id: String) =
        apolloClient.query(CharacterByIdQuery(Optional.Present(id.toInt())))
            .toFlow()
            .map { it.data?.Character?.aniListCharacter }

    suspend fun getCharacters(ids: List<Int>) = getMultiple(
        ids = ids,
        get1 = ::getCharacter1,
        get5 = ::getCharacters5,
        get10 = ::getCharacters10,
        get15 = ::getCharacters15,
    )

    private suspend fun <T> getMultiple(
        ids: List<Int>,
        get1: suspend (Int) -> T?,
        get5: suspend (List<Int>) -> List<T>,
        get10: suspend (List<Int>) -> List<T>,
        get15: suspend (List<Int>) -> List<T>,
    ) = when {
        ids.isEmpty() -> emptyList()
        ids.size == 15 -> get15(ids)
        else -> {
            val results = mutableListOf<T>()
            var remainingIds = ids
            while (remainingIds.isNotEmpty()) {
                val newRemaining: List<Int>
                results += when {
                    remainingIds.size >= 15 -> {
                        val (first, second) = remainingIds.splitAtIndex(15)
                        newRemaining = second
                        get15(first)
                    }

                    remainingIds.size >= 10 -> {
                        val (first, second) = remainingIds.splitAtIndex(10)
                        newRemaining = second
                        get10(first)
                    }

                    remainingIds.size >= 5 -> {
                        val (first, second) = remainingIds.splitAtIndex(5)
                        newRemaining = second
                        get5(first)
                    }

                    else -> {
                        newRemaining = emptyList()
                        remainingIds.mapNotNull { get1(it) }
                    }
                }
                remainingIds = newRemaining
            }
            results
        }
    }

    private suspend fun getMedia1(id: Int) =
        apolloClient.query(MediaByIdQuery(Optional.Present(id)))
            .execute().data?.Media?.aniListMedia

    private suspend fun getMedias5(@Size(min = 5, max = 5) ids: List<Int>) = apolloClient.query(
        MediaByIds5Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
            id4 = Optional.Present(ids[4]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0?.aniListMedia,
            it.media1?.aniListMedia,
            it.media2?.aniListMedia,
            it.media3?.aniListMedia,
            it.media4?.aniListMedia,
        )
    }.orEmpty()

    private suspend fun getMedias10(@Size(min = 10, max = 10) ids: List<Int>) = apolloClient.query(
        MediaByIds10Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
            id4 = Optional.Present(ids[4]),
            id5 = Optional.Present(ids[5]),
            id6 = Optional.Present(ids[6]),
            id7 = Optional.Present(ids[7]),
            id8 = Optional.Present(ids[8]),
            id9 = Optional.Present(ids[9]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0?.aniListMedia,
            it.media1?.aniListMedia,
            it.media2?.aniListMedia,
            it.media3?.aniListMedia,
            it.media4?.aniListMedia,
            it.media5?.aniListMedia,
            it.media6?.aniListMedia,
            it.media7?.aniListMedia,
            it.media8?.aniListMedia,
            it.media9?.aniListMedia,
        )
    }.orEmpty()

    private suspend fun getMedias15(@Size(min = 15, max = 15) ids: List<Int>) = apolloClient.query(
        MediaByIds15Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
            id4 = Optional.Present(ids[4]),
            id5 = Optional.Present(ids[5]),
            id6 = Optional.Present(ids[6]),
            id7 = Optional.Present(ids[7]),
            id8 = Optional.Present(ids[8]),
            id9 = Optional.Present(ids[9]),
            id10 = Optional.Present(ids[10]),
            id11 = Optional.Present(ids[11]),
            id12 = Optional.Present(ids[12]),
            id13 = Optional.Present(ids[13]),
            id14 = Optional.Present(ids[14]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.media0?.aniListMedia,
            it.media1?.aniListMedia,
            it.media2?.aniListMedia,
            it.media3?.aniListMedia,
            it.media4?.aniListMedia,
            it.media5?.aniListMedia,
            it.media6?.aniListMedia,
            it.media7?.aniListMedia,
            it.media8?.aniListMedia,
            it.media9?.aniListMedia,
            it.media10?.aniListMedia,
            it.media11?.aniListMedia,
            it.media12?.aniListMedia,
            it.media13?.aniListMedia,
            it.media14?.aniListMedia,
        )
    }.orEmpty()

    private suspend fun getCharacter1(id: Int) =
        apolloClient.query(CharacterByIdQuery(Optional.Present(id)))
            .execute().data?.Character?.aniListCharacter

    private suspend fun getCharacters5(@Size(min = 5, max = 5) ids: List<Int>) = apolloClient.query(
        CharacterByIds5Query(
            id0 = Optional.Present(ids[0]),
            id1 = Optional.Present(ids[1]),
            id2 = Optional.Present(ids[2]),
            id3 = Optional.Present(ids[3]),
            id4 = Optional.Present(ids[4]),
        )
    ).execute().data?.let {
        listOfNotNull(
            it.character0?.aniListCharacter,
            it.character1?.aniListCharacter,
            it.character2?.aniListCharacter,
            it.character3?.aniListCharacter,
            it.character4?.aniListCharacter,
        )
    }.orEmpty()

    private suspend fun getCharacters10(@Size(min = 10, max = 10) ids: List<Int>) =
        apolloClient.query(
            CharacterByIds10Query(
                id0 = Optional.Present(ids[0]),
                id1 = Optional.Present(ids[1]),
                id2 = Optional.Present(ids[2]),
                id3 = Optional.Present(ids[3]),
                id4 = Optional.Present(ids[4]),
                id5 = Optional.Present(ids[5]),
                id6 = Optional.Present(ids[6]),
                id7 = Optional.Present(ids[7]),
                id8 = Optional.Present(ids[8]),
                id9 = Optional.Present(ids[9]),
            )
        ).execute().data?.let {
            listOfNotNull(
                it.character0?.aniListCharacter,
                it.character1?.aniListCharacter,
                it.character2?.aniListCharacter,
                it.character3?.aniListCharacter,
                it.character4?.aniListCharacter,
                it.character5?.aniListCharacter,
                it.character6?.aniListCharacter,
                it.character7?.aniListCharacter,
                it.character8?.aniListCharacter,
                it.character9?.aniListCharacter,
            )
        }.orEmpty()

    private suspend fun getCharacters15(@Size(min = 15, max = 15) ids: List<Int>) =
        apolloClient.query(
            CharacterByIds15Query(
                id0 = Optional.Present(ids[0]),
                id1 = Optional.Present(ids[1]),
                id2 = Optional.Present(ids[2]),
                id3 = Optional.Present(ids[3]),
                id4 = Optional.Present(ids[4]),
                id5 = Optional.Present(ids[5]),
                id6 = Optional.Present(ids[6]),
                id7 = Optional.Present(ids[7]),
                id8 = Optional.Present(ids[8]),
                id9 = Optional.Present(ids[9]),
                id10 = Optional.Present(ids[10]),
                id11 = Optional.Present(ids[11]),
                id12 = Optional.Present(ids[12]),
                id13 = Optional.Present(ids[13]),
                id14 = Optional.Present(ids[14]),
            )
        ).execute().data?.let {
            listOfNotNull(
                it.character0?.aniListCharacter,
                it.character1?.aniListCharacter,
                it.character2?.aniListCharacter,
                it.character3?.aniListCharacter,
                it.character4?.aniListCharacter,
                it.character5?.aniListCharacter,
                it.character6?.aniListCharacter,
                it.character7?.aniListCharacter,
                it.character8?.aniListCharacter,
                it.character9?.aniListCharacter,
                it.character10?.aniListCharacter,
                it.character11?.aniListCharacter,
                it.character12?.aniListCharacter,
                it.character13?.aniListCharacter,
                it.character14?.aniListCharacter,
            )
        }.orEmpty()

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