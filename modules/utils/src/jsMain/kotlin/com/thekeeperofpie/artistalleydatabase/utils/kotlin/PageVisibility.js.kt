package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.browser.document

internal actual fun getVisibilityState(): String = document.asDynamic().visibilityState as String
