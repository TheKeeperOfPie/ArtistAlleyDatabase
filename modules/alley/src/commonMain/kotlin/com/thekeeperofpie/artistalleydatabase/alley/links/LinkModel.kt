package com.thekeeperofpie.artistalleydatabase.alley.links

import com.eygraber.uri.Uri

data class LinkModel(
    val link: String,
    val logo: Logo?,
    val title: String,
) {
    companion object {
        fun parse(link: String) = parseLinkLogoAndTitle(link)
            ?.let { LinkModel(link = link, logo = it.first, title = it.second) }
            ?: LinkModel(link = link, logo = null, title = link.removePrefix("https://"))

        private fun parseLinkLogoAndTitle(link: String): Pair<Logo, String>? {
            val uri = Uri.parseOrNull(link) ?: return null
            val path = uri.path?.removePrefix("/")?.removeSuffix("/") ?: return null
            val host = uri.host?.removePrefix("www.") ?: return null
            return when (host) {
                "artstation.com" -> Logo.ART_STATION to path
                "bsky.app" -> Logo.BLUESKY to path.substringAfter("profile/")
                "deviantart.com" -> Logo.DEVIANT_ART to path
                "discord.com", "discord.gg" -> Logo.DISCORD to "Discord"
                "etsy.com" -> Logo.ETSY to path.substringAfter("shop/")
                "gallerynucleus.com" -> Logo.GALLERY_NUCLEUS to path.substringAfter("artists/")
                "gamejolt.com" -> Logo.GAME_JOLT to path
                "inprnt.com" -> Logo.INPRNT to path.substringAfter("gallery/")
                "instagram.com" -> Logo.INSTAGRAM to path
                "ko-fi.com" -> Logo.KO_FI to path
                "linktr.ee" -> Logo.LINKTREE to path
                "patreon.com" -> Logo.PATREON to path
                "redbubble.com" -> Logo.REDBUBBLE to path.substringAfter("people/")
                "threads.net" -> Logo.THREADS to path
                "tiktok.com" -> Logo.TIK_TOK to path
                "tumblr.com" -> Logo.TUMBLR to path.removePrefix("blog/")
                "twitch.tv" -> Logo.TWITCH to path
                "x.com", "twitter.com" -> Logo.X to path
                "youtube.com" -> Logo.YOU_TUBE to (path.takeUnless { it.startsWith("channel/") }
                    ?: "YouTube")
                else -> when {
                    host.contains("bigcartel.com") ->
                        Logo.BIG_CARTEL to host.substringBefore(".bigcartel.com")
                    host.contains("bsky.social") ->
                        Logo.BLUESKY to host.substringBefore(".bsky.social")
                    host.contains("carrd.co") ->
                        Logo.CARRD to host.substringBefore(".carrd.co")
                    host.contains("etsy.com") ->
                        Logo.ETSY to host.substringBefore(".etsy.com")
                    host.contains("faire.com") ->
                        Logo.FAIRE to host.substringBefore(".faire.com")
                    host.contains("gumroad.com") ->
                        Logo.GUMROAD to host.substringBefore(".gumroad.com")
                    host.contains("itch.io") ->
                        Logo.ITCH_IO to host.substringBefore(".itch.io")
                    host.contains("myshopify.com") ->
                        Logo.SHOPIFY to host.substringBefore(".myshopify.com")
                    host.contains("storenvy.com") ->
                        Logo.STORENVY to host.substringBefore(".storenvy.com")
                    host.contains("substack.com") ->
                        Logo.SUBSTACK to host.substringBefore(".substack.com")
                    host.contains("threadless.com") ->
                        Logo.THREADLESS to host.substringBefore(".threadless.com")
                    host.contains("tumblr.com") ->
                        Logo.TUMBLR to host.substringBefore(".tumblr.com")
                    host.contains("weebly.com") ->
                        Logo.WEEBLY to host.substringBefore(".weebly.com")
                    else -> null
                }
            }
        }
    }
}
