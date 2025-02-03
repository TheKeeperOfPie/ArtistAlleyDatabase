package com.thekeeperofpie.artistalleydatabase.alley.links

import androidx.compose.ui.graphics.vector.ImageVector
import com.eygraber.uri.Uri

data class LinkModel(
    val link: String,
    val icon: ImageVector?,
    val title: String,
) {
    companion object {
        fun parseLinkModel(link: String) = parseLinkIconAndTitle(link)
            ?.let { LinkModel(link = link, icon = it.first, title = it.second) }
            ?: LinkModel(link = link, icon = null, title = link.removePrefix("https://"))

        private fun parseLinkIconAndTitle(link: String): Pair<ImageVector, String>? {
            val uri = Uri.parseOrNull(link) ?: return null
            val path = uri.path?.removePrefix("/")?.removeSuffix("/") ?: return null
            val host = uri.host?.removePrefix("www.") ?: return null
            return when (host) {
                "artstation.com" -> Logos.artStation to path
                "bsky.app" -> Logos.bluesky to path.substringAfter("profile/")
                "deviantart.com" -> Logos.deviantArt to path
                "discord.com", "discord.gg" -> Logos.discord to "Discord"
                "etsy.com" -> Logos.etsy to path.substringAfter("shop/")
                "gallerynucleus.com" -> Logos.galleryNucleus to path.substringAfter("artists/")
                "gamejolt.com" -> Logos.gameJolt to path
                "inprnt.com" -> Logos.inprnt to path.substringAfter("gallery/")
                "instagram.com" -> Logos.instagram to path
                "ko-fi.com" -> Logos.koFi to path
                "linktr.ee" -> Logos.linktree to path
                "patreon.com" -> Logos.patreon to path
                "redbubble.com" -> Logos.redbubble to path.substringAfter("people/")
                "threads.net" -> Logos.threads to path
                "tiktok.com" -> Logos.tikTok to path
                "tumblr.com" -> Logos.tumblr to path.removePrefix("blog/")
                "twitch.tv" -> Logos.twitch to path
                "x.com", "twitter.com" -> Logos.x to path
                "youtube.com" -> Logos.youTube to (path.takeUnless { it.startsWith("channel/") }
                    ?: "YouTube")
                else -> when {
                    host.contains("bigcartel.com") ->
                        Logos.bigCartel to host.substringBefore(".bigcartel.com")
                    host.contains("bsky.social") ->
                        Logos.bluesky to host.substringBefore(".bsky.social")
                    host.contains("carrd.co") ->
                        Logos.carrd to host.substringBefore(".carrd.co")
                    host.contains("etsy.com") ->
                        Logos.etsy to host.substringBefore(".etsy.com")
                    host.contains("faire.com") ->
                        Logos.faire to host.substringBefore(".faire.com")
                    host.contains("gumroad.com") ->
                        Logos.gumroad to host.substringBefore(".gumroad.com")
                    host.contains("itch.io") ->
                        Logos.itchIo to host.substringBefore(".itch.io")
                    host.contains("myshopify.com") ->
                        Logos.shopify to host.substringBefore(".myshopify.com")
                    host.contains("storenvy.com") ->
                        Logos.storenvy to host.substringBefore(".storenvy.com")
                    host.contains("substack.com") ->
                        Logos.substack to host.substringBefore(".substack.com")
                    host.contains("threadless.com") ->
                        Logos.threadless to host.substringBefore(".threadless.com")
                    host.contains("tumblr.com") ->
                        Logos.tumblr to host.substringBefore(".tumblr.com")
                    host.contains("weebly.com") ->
                        Logos.weebly to host.substringBefore(".weebly.com")
                    else -> null
                }
            }
        }
    }
}
