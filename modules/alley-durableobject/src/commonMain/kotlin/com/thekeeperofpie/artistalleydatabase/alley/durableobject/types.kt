package com.thekeeperofpie.artistalleydatabase.alley.durableobject

import com.thekeeperofpie.artistalleydatabase.cloudflare.DurableObjectNamespace

external interface Env {
    val LAST_VIEWED_SERVER: DurableObjectNamespace
}
