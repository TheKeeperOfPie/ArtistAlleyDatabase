package com.thekeeperofpie.artistalleydatabase.server

import com.anilist.HomeMangaQuery
import com.anilist.type.MediaType
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class PageQueryTest {

    @Test
    fun homeMangaNotEmpty() {
        val response = RequestUtils.executeQuery(HomeMangaQuery(thisYearStart = "20230000"))
        assertThat(response.trending?.media).isNotEmpty()
        assertThat(response.lastAdded?.media).isNotEmpty()
        assertThat(response.topThisYear?.media).isNotEmpty()
        val all = response.trending?.media.orEmpty() +
                response.lastAdded?.media.orEmpty() +
                response.topThisYear?.media.orEmpty()
        assertThat(all.all { it?.type == MediaType.MANGA }).isTrue()
    }
}
