package com.thekeeperofpie.artistalleydatabase.utils.kotlin

internal actual fun getVisibilityState(): String = getVisibilityStateJs()

@JsFun("() => window.document.visibilityState")
private external fun getVisibilityStateJs(): String
