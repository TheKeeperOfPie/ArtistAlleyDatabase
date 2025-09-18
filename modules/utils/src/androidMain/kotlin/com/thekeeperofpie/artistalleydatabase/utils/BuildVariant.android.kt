package com.thekeeperofpie.artistalleydatabase.utils

import com.thekeeperofpie.artistalleydatabase.utils.buildconfig.BuildConfigProxy

actual fun BuildVariant.isDebug(): Boolean = BuildConfigProxy.DEBUG
