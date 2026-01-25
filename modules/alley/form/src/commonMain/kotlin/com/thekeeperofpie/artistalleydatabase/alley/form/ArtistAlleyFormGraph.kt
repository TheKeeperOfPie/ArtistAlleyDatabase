package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
interface ArtistAlleyFormGraph : ArtistAlleyGraph {
    val appFileSystem: AppFileSystem
    val artistFormViewModelFactory: ArtistFormViewModel.Factory
}
