package com.thekeeperofpie.artistalleydatabase.android_utils.notification

object NotificationChannels {

    val EXPORT = NotificationChannel("export")
    val IMPORT = NotificationChannel("import")
    val SYNC = NotificationChannel("sync")
}

@JvmInline
value class NotificationChannel(val channel: String)