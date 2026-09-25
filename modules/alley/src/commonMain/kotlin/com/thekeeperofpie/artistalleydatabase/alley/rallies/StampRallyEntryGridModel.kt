package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.entry.EntryId

class StampRallyEntryGridModel(
    val stampRallyWithUserData: StampRallyWithUserData,
) {

    val id = EntryId("artist_entry", stampRallyWithUserData.stampRally.id)

    companion object {
        fun buildFromEntry(entry: StampRallyWithUserData) =
            StampRallyEntryGridModel(
                stampRallyWithUserData = entry,
            )
    }
}
