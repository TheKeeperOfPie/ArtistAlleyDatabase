package com.thekeeperofpie.artistalleydatabase.alley.tags

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_any
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_online
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_other
import artistalleydatabase.modules.alley.generated.resources.alley_commission_type_vgen
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CommissionType

val CommissionType.textRes
    get() = when (this) {
        CommissionType.ANY -> Res.string.alley_commission_type_any
        CommissionType.ON_SITE -> Res.string.alley_commission_type_on_site
        CommissionType.ONLINE -> Res.string.alley_commission_type_online
        CommissionType.VGEN -> Res.string.alley_commission_type_vgen
        CommissionType.OTHER -> Res.string.alley_commission_type_other
    }
