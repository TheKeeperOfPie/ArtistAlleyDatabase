package com.thekeeperofpie.artistalleydatabase.notification

object NotificationChannels {

    val EXPORT = NotificationChannel("export")
    val IMPORT = NotificationChannel("import")
    val SYNC = NotificationChannel("sync")
    val INFO = NotificationChannel("info")
}

@JvmInline
value class NotificationChannel(val channel: String)
