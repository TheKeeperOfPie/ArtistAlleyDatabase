package com.thekeeperofpie.artistalleydatabase.alley.functions

import org.khronos.webgl.Uint8Array

@JsModule("@noble/curves/nist.js")
external object NobleCurves {
    @JsName("p384")
    object P384 {
        fun recoverPublicKey(signature: Uint8Array, message: Uint8Array): Uint8Array
    }
}
