package com.thekeeperofpie.artistalleydatabase.alley.forum

import com.thekeeperofpie.artistalleydatabase.alley.forum.secrets.BuildKonfig

internal enum class Environment(
    val forumChannelId: String,
    val botToken: String,
    val publicArtistAlleyChannelId: String,
    val contactUserId: String,
    val verifiedArtistTagId: String
) {
    DEV(
        forumChannelId = BuildKonfig.discordDevForumChannelId,
        botToken = BuildKonfig.discordDevBotToken,
        publicArtistAlleyChannelId = BuildKonfig.discordDevPublicArtistAlleyChannelId,
        contactUserId = BuildKonfig.discordDevContactUserId,
        verifiedArtistTagId = BuildKonfig.discordDevVerifiedArtistTagId,
    ),
    PROD(
        forumChannelId = BuildKonfig.discordProdForumChannelId,
        botToken = BuildKonfig.discordProdBotToken,
        publicArtistAlleyChannelId = BuildKonfig.discordProdPublicArtistAlleyChannelId,
        contactUserId = BuildKonfig.discordProdContactUserId,
        verifiedArtistTagId = BuildKonfig.discordProdVerifiedArtistTagId,
    ),
}
