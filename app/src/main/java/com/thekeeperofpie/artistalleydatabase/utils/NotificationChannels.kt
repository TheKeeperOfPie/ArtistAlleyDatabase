package com.thekeeperofpie.artistalleydatabase.utils

object NotificationChannels {

    val EXPORT = NotificationChannel("export")
    val IMPORT = NotificationChannel("import")
    val SYNC = NotificationChannel("sync")
}

@JvmInline
value class NotificationChannel(val channel: String)