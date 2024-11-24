package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.tatarka.inject.annotations.Inject

@Inject
class CustomDispatchers(
    val io: CoroutineDispatcher = Dispatchers.IO,
    val main: CoroutineDispatcher = Dispatchers.Main,
    val default: CoroutineDispatcher = Dispatchers.Default,
) {
    constructor(dispatcher: CoroutineDispatcher): this(
        io = dispatcher,
        main = dispatcher,
        default = dispatcher,
    )

    // TODO: Legacy, replace with injected instance
    @Deprecated("Use injected instance")
    companion object {
        val Main get() = Dispatchers.Main
        val IO get() = Dispatchers.IO
        val Default get() = Dispatchers.Default
    }
}
