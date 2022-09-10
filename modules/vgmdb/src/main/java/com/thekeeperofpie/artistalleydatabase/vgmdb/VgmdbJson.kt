package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import kotlinx.serialization.json.Json
import javax.inject.Inject

class VgmdbJson @Inject constructor(override val json: Json) : AppJson() {
}