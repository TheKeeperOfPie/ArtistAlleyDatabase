@file:JsModule("discord-interactions")

package com.thekeeperofpie.artistalleydatabase.alley.discord

import kotlin.js.Promise

external fun verifyKey(
    rawData: dynamic,
    signature: String,
    timestamp: String,
    clientPublicKey: String,
): Promise<Boolean>
