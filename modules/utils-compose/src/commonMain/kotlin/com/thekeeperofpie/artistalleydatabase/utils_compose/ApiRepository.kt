package com.thekeeperofpie.artistalleydatabase.utils_compose

import co.touchlab.kermit.Logger
import com.hoc081098.flowext.concatWith
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.mapEager
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.distinctWithBuffer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

abstract class ApiRepository<DataType>(protected val scope: ApplicationScope) {

    private val fetchFlow = MutableStateFlow("")

    init {
        scope.launch(PlatformDispatchers.IO) {
            @Suppress("OPT_IN_USAGE")
            fetchFlow
                .drop(1) // Ignore initial value
                .distinctWithBuffer(10)
                .mapEager {
                    try {
                        fetch(it)
                    } catch (e: Exception) {
                        Logger.d("ApiRepository") { "Error fetching $it" }
                        null
                    }
                }
                .filterNotNull()
                .collect(::insertCachedEntry)
        }
    }

    abstract suspend fun fetch(id: String): DataType?

    abstract suspend fun getLocal(id: String): Flow<DataType?>

    abstract suspend fun insertCachedEntry(value: DataType)

    @Suppress("OPT_IN_USAGE")
    suspend fun getEntry(id: String) = getLocal(id)
        .take(1)
        .flatMapLatest {
            if (it == null) {
                flowFromSuspend { fetch(id) }
                    .catch {}
                    .filterNotNull()
                    .onEach(::insertCachedEntry)
                    .concatWith(getLocal(id))
            } else {
                getLocal(id)
            }
        }

    abstract suspend fun ensureSaved(ids: List<String>): Pair<StringResource, Exception?>?
}
