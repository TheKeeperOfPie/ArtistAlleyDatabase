package com.thekeeperofpie.artistalleydatabase.utils

object NotificationChannels {

    val EXPORT = NotificationChannel("export")
    val IMPORT = NotificationChannel("import")
}

@JvmInline
value class NotificationChannel(val channel: String)