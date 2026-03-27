package com.thekeeperofpie.artistalleydatabase.alley.discord

import com.thekeeperofpie.artistalleydatabase.cloudflare.D1Database
import com.thekeeperofpie.artistalleydatabase.cloudflare.KeyValueStore

external interface Env {
    val DISCORD_BOT_APP_ID: String
    val DISCORD_BOT_CLIENT_SECRET: String
    val DISCORD_BOT_PUBLIC_KEY: String
    val DISCORD_BOT_TOKEN: String
    val DISCORD_BOT_REDIRECT_URL: String
    val DISCORD_BOT_VERIFY_URL: String
    val DISCORD_GUILD_ID: String
    val DISCORD_ARTIST_ROLE_ID: String
    val DISCORD_ARTIST_CHANNEL_ID: String
    val ENCRYPTION_KEY: String
    val ARTIST_ALLEY_BOT_KV: KeyValueStore
    val ARTIST_ALLEY_DB: D1Database
    val ARTIST_ALLEY_FORM_DB: D1Database
    val ARTIST_ALLEY_URL: String
}
