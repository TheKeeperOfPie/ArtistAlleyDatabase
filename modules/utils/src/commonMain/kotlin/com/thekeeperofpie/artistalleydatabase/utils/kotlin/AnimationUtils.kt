package com.thekeeperofpie.artistalleydatabase.utils.kotlin

object AnimationUtils {

    fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + progress * (end - start)
    }
}
