package com.thekeeperofpie.artistalleydatabase.alley.images

expect class ImageCache {
    suspend fun cache(urls: Collection<String>)
}
