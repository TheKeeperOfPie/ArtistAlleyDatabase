package com.thekeeperofpie.artistalleydatabase.anime.notifications

import dev.zacsweers.metro.Provider

interface NotificationsComponent {
    val notificationsViewModelFactory: Provider<NotificationsViewModel.Factory>
}
