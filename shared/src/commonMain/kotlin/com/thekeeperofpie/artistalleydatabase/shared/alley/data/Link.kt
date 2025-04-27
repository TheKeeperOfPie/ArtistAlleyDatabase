package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import com.eygraber.uri.Uri

data class Link(
    val type: Type,
    val identifier: String,
) {
    companion object {
        fun parse(link: String): Link? {
            val uri = Uri.parseOrNull(link) ?: return null
            val path = uri.path?.removePrefix("/")?.removeSuffix("/")?.lowercase() ?: return null
            val host = uri.host?.removePrefix("www.")?.lowercase() ?: return null

            val exactMatch = Type.entries.find { it.domains.any(host::equals) }
            if (exactMatch != null) {
                val identifier = exactMatch.parsePath(path)
                if (identifier != null) {
                    return Link(exactMatch, identifier)
                }
            }

            val containsMatch = Type.entries.find { it.domains.any(host::contains) }
            if (containsMatch != null) {
                val identifier = containsMatch.parseHost(path)
                if (identifier != null) {
                    return Link(containsMatch, identifier)
                }
            }

            return null
        }

        fun parseFlags(links: Collection<String>): Pair<Long, Long> {
            // TODO: SQLite theoretically supports 64 bits, but it didn't work for some reason
            val entries = Type.entries
            var flagOne = 0L
            var flagTwo = 0L
            links.map { parse(it)?.type ?: Type.OTHER }
                .forEach {
                    val index = entries.indexOf(it)
                    if (index < 32) {
                        flagOne = flagOne or (1L shl index)
                    } else {
                        flagTwo = flagTwo or (1L shl (index - 32))
                    }
                }

            return flagOne to flagTwo
        }
    }

    enum class Type(
        vararg val domains: String,
        val parsePath: (path: String) -> String? = { it },
        val parseHost: (host: String) -> String? = { null },
    ) {
        ART_STATION("artstation.com"),
        BIG_CARTEL("bigcartel.com", parseHost = { it.substringBefore(".bigcartel.com") }),
        BLUESKY(
            "bsky.app", "bsky.social",
            parsePath = { it.substringAfter("profile/") },
            parseHost = { it.substringBefore(".bsky.social") },
        ),
        CARRD("carrd.co", parseHost = { it.substringBefore(".carrd.co") }),
        DEVIANT_ART("deviantart.com"),
        DISCORD("discord.com", "discord.gg", parsePath = { it.substringAfter("invite/") }),
        ETSY(
            "etsy.com",
            parsePath = { it.substringAfter("shop/") },
            parseHost = { it.substringBefore(".etsy.com") },
        ),
        FACEBOOK("facebook.com"),
        FAIRE("faire.com", parseHost = { it.substringBefore(".faire.com") }),
        GALLERY_NUCLEUS("gallerynucleus.com", parsePath = { it.substringAfter("artists/") }),
        GAME_JOLT("gamejolt.com"),
        GUMROAD("gumroad.com", parseHost = { it.substringBefore(".gumroad.com") }),
        INPRNT("inprnt.com", parsePath = { it.substringAfter("gallery/") }),
        INSTAGRAM("instagram.com"),
        ITCH_IO("itch.io", parseHost = { it.substringBefore(".itch.io") }),
        KICKSTARTER("kickstarter.com", parsePath = { it.substringAfter("profile/") }),
        KO_FI("ko-fi.com"),
        LINKTREE("linktr.ee"),
        PATREON("patreon.com"),
        PIXIV(
            "pixiv.me", "pixiv.net",
            parseHost = { it.substringAfter("users/").substringBefore("/") },
        ),
        REDBUBBLE("redbubble.com", parsePath = { it.substringAfter("people/") }),
        SHOPIFY("myshopify.com", parseHost = { it.substringBefore(".myshopify.com") }),
        STORENVY("storenvy.com", parseHost = { it.substringBefore(".storenvy.com") }),
        SUBSTACK("substack.com", parseHost = { it.substringBefore(".substack.com") }),
        THREADLESS("threadless.com", parseHost = { it.substringBefore(".threadless.com") }),
        THREADS("threads.net"),
        TIK_TOK("tiktok.com"),
        TUMBLR(
            "tumblr.com",
            parsePath = { it.removePrefix("blog/") },
            parseHost = { it.substringBefore(".tumblr.com") },
        ),
        TWITCH("twitch.tv"),
        WEEBLY("weebly.com", parseHost = { it.substringBefore(".weebly.com") }),
        X("x.com", "twitter.com"),
        YOU_TUBE("youtube.com", parsePath = {
            (it.takeUnless { it.startsWith("channel/") }
                ?.substringAfter("c/")
                ?: "YouTube")
        }),
        OTHER,
        ;
    }
}
