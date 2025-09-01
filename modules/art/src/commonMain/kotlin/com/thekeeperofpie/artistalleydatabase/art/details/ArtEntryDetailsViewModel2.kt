package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter2
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import me.tatarka.inject.annotations.Inject
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Inject
class ArtEntryDetailsViewModel2(
    private val artEntryDao: ArtEntryDetailsDao,
    internal val autocompleter2: AniListAutocompleter2,
) : ViewModel() {

    suspend fun series(query: String) =
        autocompleter2.series(query, artEntryDao::querySeries)
}
