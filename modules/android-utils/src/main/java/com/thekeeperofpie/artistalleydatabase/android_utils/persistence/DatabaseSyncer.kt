package com.thekeeperofpie.artistalleydatabase.android_utils.persistence

interface DatabaseSyncer {

    companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }

    fun getMaxProgress(): Int

    suspend fun sync(
        initialProgress: Int,
        maxProgress: Int,
        setProgress: (progress: Int, max: Int) -> Unit
    )

    suspend fun <T> repeatToLimit(
        query: suspend (limit: Int, offset: Int) -> List<T>,
        limit: Int = DEFAULT_PAGE_SIZE,
        block: suspend (list: Sequence<T>) -> Unit,
    ) {
        var offset = 0
        var list: List<T>
        do {
            list = query(limit, offset)
                .also { block(it.asSequence()) }
            offset += list.size
        } while (list.isNotEmpty())
    }
}