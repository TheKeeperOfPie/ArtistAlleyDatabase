package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.navigationevent.NavigationEventInfo

data class NavigationRoute(val route: String) : NavigationEventInfo()

data class NavigationRouteHistory(
    val current: NavigationRoute,
    val back: List<NavigationRoute>,
    val forward: List<NavigationRoute>,
) {
    override fun toString() =
        "NavigationRouteHistory(\n" +
                "\tcurrent = $current,\n" +
                "\tback = ${back.joinToString("\n")},\n" +
                "\tforward = ${forward.joinToString("\n")}\n" +
                ")"
}
