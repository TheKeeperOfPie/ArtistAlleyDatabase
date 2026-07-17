package com.thekeeperofpie.artistalleydatabase.alley.edit.lastviewed

import androidx.compose.ui.graphics.Color
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LastViewedEvent
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LastViewedPage
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import kotlin.time.Instant

internal data class ActiveUserData(val users: List<ActiveUser>) {
    constructor(
        page: LastViewedPage,
        usersToVisits: Map<String, List<LastViewedEvent.Sync.PageVisit>>,
    ) : this(
        usersToVisits.entries
            .mapNotNull {
                val visits = it.value.filter { it.page == page }
                if (visits.isEmpty()) return@mapNotNull null
                val backgroundColor = ComposeColorUtils.derivedColor(it.key)
                ActiveUser(
                    identifier = it.key,
                    backgroundColor = backgroundColor,
                    textColor = ComposeColorUtils.bestTextColor(backgroundColor),
                    timestamp = visits.maxOf { it.lastUpdate },
                )
            }
            .sortedByDescending { it.timestamp }
    )

    data class ActiveUser(
        val identifier: String,
        val backgroundColor: Color,
        val textColor: Color,
        val timestamp: Instant,
    )
}
