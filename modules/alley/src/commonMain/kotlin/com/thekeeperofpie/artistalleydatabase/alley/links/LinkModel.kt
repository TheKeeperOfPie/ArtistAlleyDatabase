package com.thekeeperofpie.artistalleydatabase.alley.links

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_art_station
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_big_cartel
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_bluesky
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_carrd
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_deviant_art
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_discord
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_etsy
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_facebook
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_faire
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_gallery_nucleus
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_game_jolt
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_gumroad
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_inprnt
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_instagram
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_itch_io
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_kickstarter
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_ko_fi
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_linktree
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_other
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_patreon
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_pixiv
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_redbubble
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_shopify
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_storenvy
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_substack
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_threadless
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_threads
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_tik_tok
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_tumblr
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_twitch
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_weebly
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_x
import artistalleydatabase.modules.alley.generated.resources.alley_link_label_you_tube
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import org.jetbrains.compose.resources.StringResource

val Link.Type.textRes: StringResource
    get() = when (this) {
        Link.Type.ART_STATION -> Res.string.alley_link_label_art_station
        Link.Type.BIG_CARTEL -> Res.string.alley_link_label_big_cartel
        Link.Type.BLUESKY -> Res.string.alley_link_label_bluesky
        Link.Type.CARRD -> Res.string.alley_link_label_carrd
        Link.Type.DEVIANT_ART -> Res.string.alley_link_label_deviant_art
        Link.Type.DISCORD -> Res.string.alley_link_label_discord
        Link.Type.ETSY -> Res.string.alley_link_label_etsy
        Link.Type.FACEBOOK -> Res.string.alley_link_label_facebook
        Link.Type.FAIRE -> Res.string.alley_link_label_faire
        Link.Type.GALLERY_NUCLEUS -> Res.string.alley_link_label_gallery_nucleus
        Link.Type.GAME_JOLT -> Res.string.alley_link_label_game_jolt
        Link.Type.GUMROAD -> Res.string.alley_link_label_gumroad
        Link.Type.INPRNT -> Res.string.alley_link_label_inprnt
        Link.Type.INSTAGRAM -> Res.string.alley_link_label_instagram
        Link.Type.ITCH_IO -> Res.string.alley_link_label_itch_io
        Link.Type.KICKSTARTER -> Res.string.alley_link_label_kickstarter
        Link.Type.KO_FI -> Res.string.alley_link_label_ko_fi
        Link.Type.LINKTREE -> Res.string.alley_link_label_linktree
        Link.Type.PATREON -> Res.string.alley_link_label_patreon
        Link.Type.PIXIV -> Res.string.alley_link_label_pixiv
        Link.Type.REDBUBBLE -> Res.string.alley_link_label_redbubble
        Link.Type.SHOPIFY -> Res.string.alley_link_label_shopify
        Link.Type.STORENVY -> Res.string.alley_link_label_storenvy
        Link.Type.SUBSTACK -> Res.string.alley_link_label_substack
        Link.Type.THREADLESS -> Res.string.alley_link_label_threadless
        Link.Type.THREADS -> Res.string.alley_link_label_threads
        Link.Type.TIK_TOK -> Res.string.alley_link_label_tik_tok
        Link.Type.TUMBLR -> Res.string.alley_link_label_tumblr
        Link.Type.TWITCH -> Res.string.alley_link_label_twitch
        Link.Type.WEEBLY -> Res.string.alley_link_label_weebly
        Link.Type.X -> Res.string.alley_link_label_x
        Link.Type.YOU_TUBE -> Res.string.alley_link_label_you_tube
        Link.Type.OTHER -> Res.string.alley_link_label_other
    }

data class LinkModel(
    val link: String,
    val logo: Logo?,
    val identifier: String,
) {
    companion object {
        fun parse(uri: String): LinkModel {
            val link = Link.parse(uri) ?: return LinkModel(
                link = uri,
                logo = null,
                identifier = uri.removePrefix("https://")
                    .removePrefix("www.")
                    .removeSuffix("/")
            )
            val logo = when (link.type) {
                Link.Type.ART_STATION -> Logo.ART_STATION
                Link.Type.BIG_CARTEL -> Logo.BIG_CARTEL
                Link.Type.BLUESKY -> Logo.BLUESKY
                Link.Type.CARRD -> Logo.CARRD
                Link.Type.DEVIANT_ART -> Logo.DEVIANT_ART
                Link.Type.DISCORD -> Logo.DISCORD
                Link.Type.ETSY -> Logo.ETSY
                Link.Type.FACEBOOK -> Logo.FACEBOOK
                Link.Type.FAIRE -> Logo.FAIRE
                Link.Type.GALLERY_NUCLEUS -> Logo.GALLERY_NUCLEUS
                Link.Type.GAME_JOLT -> Logo.GAME_JOLT
                Link.Type.GUMROAD -> Logo.GUMROAD
                Link.Type.INPRNT -> Logo.INPRNT
                Link.Type.INSTAGRAM -> Logo.INSTAGRAM
                Link.Type.ITCH_IO -> Logo.ITCH_IO
                Link.Type.KICKSTARTER -> Logo.KICKSTARTER
                Link.Type.KO_FI -> Logo.KO_FI
                Link.Type.LINKTREE -> Logo.LINKTREE
                Link.Type.PATREON -> Logo.PATREON
                Link.Type.PIXIV -> Logo.PIXIV
                Link.Type.REDBUBBLE -> Logo.REDBUBBLE
                Link.Type.SHOPIFY -> Logo.SHOPIFY
                Link.Type.STORENVY -> Logo.STORENVY
                Link.Type.SUBSTACK -> Logo.SUBSTACK
                Link.Type.THREADLESS -> Logo.THREADLESS
                Link.Type.THREADS -> Logo.THREADS
                Link.Type.TIK_TOK -> Logo.TIK_TOK
                Link.Type.TUMBLR -> Logo.TUMBLR
                Link.Type.TWITCH -> Logo.TWITCH
                Link.Type.WEEBLY -> Logo.WEEBLY
                Link.Type.X -> Logo.X
                Link.Type.YOU_TUBE -> Logo.YOU_TUBE
                Link.Type.OTHER -> null
            }
            return LinkModel(link = uri, logo = logo, identifier = link.identifier)
        }
    }
}
