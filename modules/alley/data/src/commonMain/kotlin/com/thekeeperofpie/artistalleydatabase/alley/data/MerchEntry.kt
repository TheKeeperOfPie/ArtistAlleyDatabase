package com.thekeeperofpie.artistalleydatabase.alley.data

import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import kotlin.uuid.Uuid

fun MerchEntry.toMerchInfo() = MerchInfo(
    name = name,
    uuid = Uuid.parse(uuid),
    notes = notes,
)
