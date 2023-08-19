package com.thekeeperofpie.artistalleydatabase.anime.utils

import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.chunked
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Manually batches network requests to avoid API rate limits.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RequestBatcher<T>(
    scope: CoroutineScope,
    apiCall: suspend (List<String>) -> List<T>,
    resultToId: (T) -> String,
) {

    private val requestChannel = Channel<Request<T>>()

    init {
        scope.launch(CustomDispatchers.IO) {
            requestChannel.consumeAsFlow()
                .chunked(10, 1.seconds)
                .flatMapMerge { requests ->
                    flow {
                        val results = apiCall(requests.map { it.id })
                        requests.forEach { request ->
                            emit(request to results.find { resultToId(it) == request.id })
                        }
                    }.catch {
                        requests.map { it to null }.forEach { emit(it) }
                    }
                }
                .collectLatest { it.first.result.complete(it.second) }
        }
    }

    suspend fun fetch(id: String): T? {
        val request = Request<T>(id)
        requestChannel.send(request)
        return select {
            request.result.onAwait { it }
            onTimeout(1.minutes) { null }
        }
    }

    private data class Request<T>(
        val id: String,
        val result: CompletableDeferred<T?> = CompletableDeferred(),
    )
}
