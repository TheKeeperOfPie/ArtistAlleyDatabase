package com.thekeeperofpie.artistalleydatabase.utils

object PendingIntentRequestCodes {

    val EXPORT_MAIN_ACTIVITY_OPEN = PendingIntentRequestCode(1000)
    val IMPORT_MAIN_ACTIVITY_OPEN = PendingIntentRequestCode(2000)
    val SYNC_MAIN_ACTIVITY_OPEN = PendingIntentRequestCode(3000)
    val INFO_CRASH_MAIN_ACTIVITY_OPEN = PendingIntentRequestCode(4000)
}

@JvmInline
value class PendingIntentRequestCode(val code: Int)
