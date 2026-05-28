package com.thekeeperofpie.artistalleydatabase.shared.alley.data

enum class LinkCategory {
    PORTFOLIOS,
    SOCIALS,
    STORES,
    SUPPORT,
    COMMISSIONS,
    OTHER,
}

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

        Link.Type.AO3,
        Link.Type.BLUESKY,
        Link.Type.DISCORD,
        Link.Type.FACEBOOK,
        Link.Type.GAME_JOLT,
        Link.Type.INSTAGRAM,
        Link.Type.PICARTO,
        Link.Type.SUBSTACK,
        Link.Type.THREADS,
        Link.Type.TIK_TOK,
        Link.Type.TUMBLR,
        Link.Type.TWITCH,
        Link.Type.UNVALE,
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

        Link.Type.VGEN -> LinkCategory.COMMISSIONS
        Link.Type.OTHER_NON_STORE,
        null,
            -> LinkCategory.OTHER
    }
