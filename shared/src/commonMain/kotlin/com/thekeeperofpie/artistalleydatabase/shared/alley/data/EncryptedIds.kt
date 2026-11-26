package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

// TODO: Not actually encrypted yet, will be done in follow-up
@JvmInline
@Serializable
value class EncryptedArtistId(val id: String)
