package com.thekeeperofpie.artistalleydatabase.alley.discord

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.Connection
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionPatchResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionRequest
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.DiscordInteractionResponse
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionCallbackType
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionRequestData
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.InteractionType
import com.thekeeperofpie.artistalleydatabase.alley.discord.models.MessageComponent
import com.thekeeperofpie.artistalleydatabase.alley.discord.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.form.data.ArtistFormPublicKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URL
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.uuid.Uuid

@OptIn(ExperimentalStdlibApi::class)
internal object BotBackend {

    suspend fun handleInteraction(
        request: Request,
        env: Env,
        json: Json,
        api: DiscordApi,
    ): Response {
        val method = request.method
        if (method != "POST") {
            return Response("Invalid method $method", ResponseInit(status = 405))
        }
        val signature = request.headers.get("X-Signature-Ed25519")
        val timestamp = request.headers.get("X-Signature-Timestamp")
        if (signature == null || timestamp == null) {
            return Responses.response401
        }
        // TODO: Verify timestamp is within acceptable window
        val body = request.text().await()
        val verified = verifyKey(body, signature, timestamp, env.DISCORD_BOT_PUBLIC_KEY).await()
        if (!verified) {
            return Responses.response401
        }

        val interaction = json.decodeFromString<DiscordInteractionRequest>(body)
        val interactionType = interaction.type
        if (interactionType == InteractionType.PING) {
            return jsonResponse(
                DiscordInteractionResponse(type = InteractionCallbackType.PONG)
            )
        } else if (interactionType == null ||
            interaction.member == null ||
            interaction.guildId != env.DISCORD_GUILD_ID
        ) {
            return Responses.response404
        }

        api.deferResponse(interaction)

        val command = (interaction.data as? InteractionRequestData.SlashCommand)
            ?.options?.singleOrNull()
            ?: return Responses.response404

        val response = when (command.name) {
            "verify" -> {
                val options = command.options
                if (options.isNullOrEmpty()) return Responses.response404
                val dataYear =
                    DataYear.deserialize(options.first { it.name == "convention" }.value!!)!!
                val boothValue = options.first { it.name == "booth" }.value
                    ?.takeIf { it.length <= 3 }
                    ?: return Responses.response404
                val boothLetter = boothValue.first().takeIf { it.isLetter() }
                    ?: return Responses.response404
                val boothNumber = boothValue.drop(1).toIntOrNull()
                    ?: return Responses.response404
                val booth = "$boothLetter${boothNumber.toString().padStart(2, '0')}"

                val userId = interaction.member.user.id
                env.ARTIST_ALLEY_BOT_KV.put(userId, interaction.token).await()
                DiscordInteractionPatchResponse(
                    // TODO: Use UI string for name
                    content = """
                            ## Verify your Artist Profile 
                            Click below to check your Discord connections. We’ll use this to match a social connection (i.e. Bluesky) to your ${dataYear.serializedName} artist page at ${env.ARTIST_ALLEY_URL}
                            ### Privacy
                            This should only ask for the "connections" permission, and hidden accounts will still work. No data will be stored. Immediately after verification, the bot will revoke this permission from itself.
                        """.trimIndent(),
                    flags = MessageFlags(MessageFlag.EPHEMERAL),
                    components = listOf(
                        MessageComponent.ActionRow(
                            MessageComponent.Button(
                                style = MessageComponent.Button.Style.LINK,
                                label = "Verify",
                                url = buildOAuthUrl(
                                    env = env,
                                    userId = userId,
                                    dataYear = dataYear,
                                    booth = booth,
                                ),
                            )
                        )
                    )
                )
            }
            else -> return Responses.response404
        }

        api.patchInteractionResponse(interaction.token, response)

        return Responses.response202
    }

    suspend fun verifyArtist(request: Request, env: Env, api: DiscordApi): Response {
        val params = URL(request.url).searchParams
        val code = params.get("code")
        val state = params.get("state")
        if (code.isNullOrBlank() || state.isNullOrBlank()) return Responses.response401

        val oAuthState = OAuthState.decode(
            AlleyCryptography.symmetricDecrypt(
                env.ENCRYPTION_KEY,
                state
            )
        ) ?: return Responses.response401

        val interactionToken = env.ARTIST_ALLEY_BOT_KV.get(oAuthState.userId).await()
        env.ARTIST_ALLEY_BOT_KV.delete(oAuthState.userId).await()
        if (interactionToken.isNullOrBlank()) return Responses.response401

        val authResponse = api.getAuth(code)
        val connections = try {
            api.getConnections(authResponse.accessToken)
        } finally {
            try {
                api.revokeToken(authResponse.accessToken, isRefreshToken = false)
            } finally {
                api.revokeToken(authResponse.accessToken, isRefreshToken = true)
            }
        }

        val artistEntry = Databases.editDatabase(env)
            .artistEntryAnimeExpo2026Queries
            .getArtistByBooth(oAuthState.booth)
            .awaitAsOneOrNull()
        if (artistEntry == null) {
            api.patchInteractionResponse(
                interactionToken = interactionToken,
                response = failureResponse(env, oAuthState.booth),
            )
            return Responses.responseReturnToDiscord
        }

        val linkTypeToIdentifier = artistEntry.socialLinks.mapNotNull(Link::parse)
            .associate { it.type to it.identifier }
        val verified = connections.any {
            val type = when (it.type) {
                Connection.Type.BLUESKY -> Link.Type.BLUESKY
                Connection.Type.FACEBOOK -> Link.Type.FACEBOOK
                Connection.Type.INSTAGRAM -> Link.Type.INSTAGRAM
                Connection.Type.TIK_TOK -> Link.Type.TIK_TOK
                Connection.Type.TWITCH -> Link.Type.TWITCH
                Connection.Type.YOU_TUBE -> Link.Type.YOU_TUBE
                Connection.Type.X -> Link.Type.X
                null -> return@any false
            }
            val identifier = linkTypeToIdentifier[type]?.ifBlank { null }
                ?: return@any false
            it.name.equals(identifier, ignoreCase = true)
        }

        if (verified) {
            val keys = AlleyCryptography.generate()
            Databases.formDatabase(env)
                .alleyFormPublicKeyQueries
                .insertPublicKey(
                    ArtistFormPublicKey(
                        artistId = Uuid.parse(artistEntry.id),
                        publicKey = keys.publicKey,
                    )
                )
            val accessUrl = AlleyDataUtils.formLink(BuildKonfig.formUrl, keys.privateKey)
            try {
                api.grantRole(oAuthState.userId)
            } catch (_: Throwable) {
                // Ignore if role doesn't grant, not important enough to fail on
            }
            api.patchInteractionResponse(
                interactionToken = interactionToken,
                response = DiscordInteractionPatchResponse(
                    content = """
                            ## Thank you… anime artist
                            Your URL is $accessUrl
                            
                            Bookmark it so you can edit your data in the future; previous links are now invalid. **Do not share** it with anyone. If you ever lose the link, use the bot to re-verify.
                            ### You feel an odd compulsion to draw Miku…
                            The doors to <#${env.DISCORD_ARTIST_CHANNEL_ID}> have opened for you
                        """.trimIndent(),
                    flags = MessageFlags(MessageFlag.EPHEMERAL),
                    components = emptyList(),
                )
            )
        } else {
            api.patchInteractionResponse(
                interactionToken = interactionToken,
                response = failureResponse(env, oAuthState.booth),
            )
        }

        return Responses.responseReturnToDiscord
    }

    private fun failureResponse(env: Env, booth: String) = DiscordInteractionPatchResponse(
        content = """
            ## Verification failed
            Make sure that your Discord account has one of the social media accounts listed under your table ($booth) at ${env.ARTIST_ALLEY_URL} and try again
        """.trimIndent(),
        flags = MessageFlags(MessageFlag.EPHEMERAL),
    )

    private suspend fun buildOAuthUrl(
        env: Env,
        userId: String,
        dataYear: DataYear,
        booth: String,
    ): String {
        val encryptedState = AlleyCryptography.symmetricEncrypt(
            key = env.ENCRYPTION_KEY,
            payload = OAuthState(
                userId = userId,
                dataYear = dataYear,
                booth = booth,
            ).encode(),
        )
        return "${env.DISCORD_BOT_VERIFY_URL}&state=$encryptedState"
    }
}
