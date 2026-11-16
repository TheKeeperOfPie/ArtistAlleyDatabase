package com.thekeeperofpie.artistalleydatabase.alley.user

interface AlleyUserSettings {
    var pendingUserAuthCode: String?
    var lastSyncGeneration: Long?
}
