package com.thekeeperofpie.artistalleydatabase.anime.staff

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_staff_media_label
import artistalleydatabase.modules.anime.generated.resources.anime_staff_overview_label
import artistalleydatabase.modules.anime.generated.resources.anime_staff_staff_label
import org.jetbrains.compose.resources.StringResource

enum class StaffTab(val textRes: StringResource) {
    OVERVIEW(Res.string.anime_staff_overview_label),
    MEDIA(Res.string.anime_staff_media_label),
    STAFF(Res.string.anime_staff_staff_label),
}
