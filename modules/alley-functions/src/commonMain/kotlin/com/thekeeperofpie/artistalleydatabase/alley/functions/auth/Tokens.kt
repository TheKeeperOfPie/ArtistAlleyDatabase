package com.thekeeperofpie.artistalleydatabase.alley.functions.auth

import kotlin.jvm.JvmInline

@JvmInline
value class AuthToken(val token: String)

@JvmInline
value class RefreshToken(val token: String)
