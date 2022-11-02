package com.thekeeperofpie.artistalleydatabase.utils

object NotificationIds {

    val EXPORT_ONGOING = NotificationId(1001)
    val EXPORT_FINISHED = NotificationId(1002)

    val IMPORT_ONGOING = NotificationId(2001)
    val IMPORT_FINISHED = NotificationId(2002)

    val SYNC_ONGOING = NotificationId(3001)
    val SYNC_FINISHED = NotificationId(3002)
}

@JvmInline
value class NotificationId(val id: Int)