package com.thekeeperofpie.artistalleydatabase.utils

object AnimationUtils {

    fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + progress * (end - start)
    }
}
