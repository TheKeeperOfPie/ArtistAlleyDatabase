package com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user

import kotlin.jvm.JvmInline

@JvmInline
value class UserId(val obfuscatedId: String)

@JvmInline
value class UserArtistId(val encryptedId: String)
