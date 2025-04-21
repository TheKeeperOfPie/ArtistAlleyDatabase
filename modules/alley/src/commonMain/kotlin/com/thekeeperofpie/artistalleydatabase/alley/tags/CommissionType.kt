package com.thekeeperofpie.artistalleydatabase.alley.tags

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_any
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_online
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_other
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_vgen
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class CommissionType(val textRes: StringResource) {
    ANY(Res.string.alley_commission_type_any),
    ON_SITE(Res.string.alley_commission_type_on_site),
    ONLINE(Res.string.alley_commission_type_online),
    VGEN(Res.string.alley_commission_type_vgen),
    OTHER(Res.string.alley_commission_type_other),
}
