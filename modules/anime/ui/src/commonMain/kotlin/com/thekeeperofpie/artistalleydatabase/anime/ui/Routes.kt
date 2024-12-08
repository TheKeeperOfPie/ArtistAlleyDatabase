package com.thekeeperofpie.artistalleydatabase.anime.ui

import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination

// TODO: Move these elsewhere

typealias UserRoute = (
    id: String,
    SharedTransitionKey?,
    name: String,
    ImageState?,
) -> NavDestination

typealias StaffDetailsRoute = (
    staffId: String,
    SharedTransitionKey?,
    staffName: String?,
    staffSubtitle: String?,
    ImageState?,
    favorite: Boolean?,
) -> NavDestination
