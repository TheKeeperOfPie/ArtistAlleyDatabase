package com.thekeeperofpie.artistalleydatabase.anime.notifications

interface NotificationsComponent {
    val notificationsViewModelFactory: () -> NotificationsViewModel.Factory
}
