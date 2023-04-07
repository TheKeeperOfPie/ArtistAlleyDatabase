package com.thekeeperofpie.artistalleydatabase.art.persistence

import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import kotlinx.coroutines.flow.MutableStateFlow

interface ArtSettings {

    val artEntryTemplate: MutableStateFlow<ArtEntry?>
}