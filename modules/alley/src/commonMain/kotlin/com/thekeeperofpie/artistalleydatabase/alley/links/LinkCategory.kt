package com.thekeeperofpie.artistalleydatabase.alley.links

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_other
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_portfolios
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_socials
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_stores
import artistalleydatabase.modules.alley.generated.resources.alley_link_type_category_support
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import org.jetbrains.compose.resources.StringResource

val Link.Type?.category: LinkCategory
    get() = when (this) {
        Link.Type.ART_STATION,
        Link.Type.CARA,
        Link.Type.CARRD,
        Link.Type.DEVIANT_ART,
        Link.Type.LINKTREE,
        Link.Type.PIXIV,
        Link.Type.TOYHOUSE,
        Link.Type.WEEBLY,
            -> LinkCategory.PORTFOLIOS

        Link.Type.BLUESKY,
        Link.Type.DISCORD,
        Link.Type.FACEBOOK,
        Link.Type.GAME_JOLT,
        Link.Type.INSTAGRAM,
        Link.Type.SUBSTACK,
        Link.Type.THREADS,
        Link.Type.TIK_TOK,
        Link.Type.TUMBLR,
        Link.Type.TWITCH,
        Link.Type.X,
        Link.Type.YOU_TUBE,
            -> LinkCategory.SOCIALS

        Link.Type.BIG_CARTEL,
        Link.Type.ETSY,
        Link.Type.FAIRE,
        Link.Type.GALLERY_NUCLEUS,
        Link.Type.GUMROAD,
        Link.Type.INPRNT,
        Link.Type.ITCH_IO,
        Link.Type.REDBUBBLE,
        Link.Type.SHOPIFY,
        Link.Type.STORENVY,
        Link.Type.THREADLESS,
        Link.Type.OTHER_STORE,
            -> LinkCategory.STORES

        Link.Type.KICKSTARTER,
        Link.Type.KO_FI,
        Link.Type.PATREON,
            -> LinkCategory.SUPPORT

        Link.Type.OTHER_NON_STORE,
        Link.Type.VGEN,
        null,
            -> LinkCategory.OTHER
    }

enum class LinkCategory(val textRes: StringResource) {
    PORTFOLIOS(Res.string.alley_link_type_category_portfolios),
    SOCIALS(Res.string.alley_link_type_category_socials),
    STORES(Res.string.alley_link_type_category_stores),
    SUPPORT(Res.string.alley_link_type_category_support),
    OTHER(Res.string.alley_link_type_category_other),
}
