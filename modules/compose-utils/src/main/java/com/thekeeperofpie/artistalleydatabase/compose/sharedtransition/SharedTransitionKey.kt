package com.thekeeperofpie.artistalleydatabase.compose.sharedtransition

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class SharedTransitionKey private constructor(val key: String) {

    companion object {
        @Composable
        fun makeKeyForId(id: String) = SharedTransitionKey("${LocalSharedTransitionPrefixKeys.current}-$id")

        fun deserialize(value: String) = SharedTransitionKey(value)
    }
}
