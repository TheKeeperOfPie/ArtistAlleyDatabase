package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.browser.window
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@Inject
actual class AlleyEditRemoteDatabase(
    private val ktorClient: HttpClient,
) {
    actual suspend fun loadFunction(): String = withContext(PlatformDispatchers.IO) {
        ktorClient.get(window.origin + "/database").bodyAsText()
    }
}
