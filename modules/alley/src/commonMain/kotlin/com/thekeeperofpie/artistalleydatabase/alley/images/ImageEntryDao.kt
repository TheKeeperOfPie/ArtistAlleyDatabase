package com.thekeeperofpie.artistalleydatabase.alley.images

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ImageQueries
import com.thekeeperofpie.artistalleydatabase.alley.user.ImageEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ImageEntryDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val imageDao: suspend () -> ImageQueries = { database().imageQueries },
) {
    suspend fun getAllImages() =
        imageDao()
            .getAllImages()
            .awaitAsList()

    suspend fun getImages(ids: Collection<String>, type: ImageType) =
        imageDao()
            .getImageEntries(ids, type.name)
            .awaitAsList()

    suspend fun insertImageEntries(entries: List<ImageEntry>) {
        imageDao().run {
            transaction {
                entries.forEach {
                    insertImageEntry(it)
                }
            }
        }
    }

    suspend fun queryUrls(urls: Collection<String>) =
        imageDao().queryUrls(urls)
            .awaitAsList()
            .associate { it.url to Instant.fromEpochSeconds(it.createdAtSecondsUtc) }
}
