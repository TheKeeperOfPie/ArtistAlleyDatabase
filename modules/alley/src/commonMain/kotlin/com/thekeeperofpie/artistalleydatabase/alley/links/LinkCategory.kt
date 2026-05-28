package com.thekeeperofpie.artistalleydatabase.alley.links

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_other
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_portfolios
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_socials
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_stores
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_support
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LinkCategory
import org.jetbrains.compose.resources.StringResource

val LinkCategory.textRes: StringResource
    get() = when (this) {
        LinkCategory.PORTFOLIOS -> Res.string.alley_link_type_category_portfolios
        LinkCategory.SOCIALS -> Res.string.alley_link_type_category_socials
        LinkCategory.STORES -> Res.string.alley_link_type_category_stores
        LinkCategory.SUPPORT -> Res.string.alley_link_type_category_support
        LinkCategory.COMMISSIONS -> Res.string.alley_link_type_category_commissions
        LinkCategory.OTHER -> Res.string.alley_link_type_category_other
    }
