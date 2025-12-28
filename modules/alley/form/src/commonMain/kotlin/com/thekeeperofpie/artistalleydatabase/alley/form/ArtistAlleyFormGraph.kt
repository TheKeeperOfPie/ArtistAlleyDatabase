package com.thekeeperofpie.artistalleydatabase.alley.form

import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyComponent
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
interface ArtistAlleyFormGraph : ArtistAlleyComponent {
    val appFileSystem: AppFileSystem
    val artistFormViewModelFactory: ArtistFormViewModel.Factory
}
