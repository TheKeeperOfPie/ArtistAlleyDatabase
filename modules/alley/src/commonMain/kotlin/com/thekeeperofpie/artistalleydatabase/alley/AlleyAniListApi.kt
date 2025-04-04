package com.thekeeperofpie.artistalleydatabase.alley

import com.anilist.data.MediaImagesQuery
import com.apollographql.apollo3.ApolloClient
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListResponseCodeCoercingInterceptor
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject

@Inject
@SingletonScope
class AlleyAniListApi {

    private val client = ApolloClient.Builder()
        .serverUrl(AniListDataUtils.GRAPHQL_API_URL)
        .addHttpInterceptor(AniListResponseCodeCoercingInterceptor)
        .build()

    suspend fun getMediaImages(mediaIds: Collection<Int>) =
        mediaIds
            .chunked(25)
            .mapIndexed { index, mediaIds ->
                client.query(MediaImagesQuery(mediaIds)).execute().data?.page?.media
                    ?.filter { it?.id != null && it.coverImage?.medium != null }
                    ?.associate { it!!.id to it.coverImage?.medium!! }
                    .orEmpty()
            }
            .fold(emptyMap<Int, String>()) { acc, value -> acc + value }
}
